package com.course.source.app.web.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.longState
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.web.request.RequestContentStatus.Empty
import com.course.source.app.web.request.RequestContentStatus.Failure
import com.course.source.app.web.request.RequestContentStatus.FailureButHitCache
import com.course.source.app.web.request.RequestContentStatus.HitCache
import com.course.source.app.web.request.RequestContentStatus.None
import com.course.source.app.web.request.RequestContentStatus.Requesting
import com.course.source.app.web.request.RequestContentStatus.Success
import com.course.source.app.web.source.page.RequestContentScreen
import com.russhwolf.settings.long
import com.russhwolf.settings.string
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 15:44
 */
@Stable
data class RequestContent<T : Any>(
  override val key: String,
  val name: String,
  val parameterWithHint: LinkedHashMap<String, String>,
  val resultSerializer: KSerializer<T?>,
  val format: String,
  val editable: Boolean = false,
) : IRequestSource {

  companion object {
    fun find(key: String): RequestContent<*>? {
      return SourceRequest.AllImpl.firstNotNullOfOrNull { source ->
        source.requestSource.firstNotNullOfOrNull { entry ->
          when (val request = entry.value) {
            is RequestGroup<*> -> request.requestContents.find { it.key == key }
            is RequestContent<*> -> if (request.key == key) request else null
          }
        }
      }
    }
  }

  private val settings = createSettings("RequestContent-$key")

  val requestUnits: SnapshotStateList<RequestUnit> = settings.getStringOrNull("requestUnits")
    ?.let { Json.decodeFromString<List<RequestUnit>>(it).toMutableStateList() }
    ?: SnapshotStateList()

  val title = mutableStateOf(name)

  var requestTimestamp: Long by settings.longState("requestTimestamp", 0L)
    private set

  private var responseTimestamp: Long by settings.longState("responseTimestamp", 0L)
    private set

  var cacheExpiration: Long by settings.long("cacheExpiration", 12.hours.inWholeMilliseconds)

  private var prevRequestValues: String by settings.string("prevRequestValues", "[]")

  var requestContentStatus: RequestContentStatus by mutableStateOf(
    when {
      requestUnits.isEmpty() -> Empty
      requestTimestamp == 0L -> None
      responseTimestamp >= requestTimestamp -> Success
      else -> Failure
    }
  )
    private set

  suspend fun request(isForce: Boolean, vararg values: String): T? {
    if (values.size != parameterWithHint.size)
      throw IllegalArgumentException("参数数量不匹配，应有 ${parameterWithHint.size}, 实有: ${values.size}")
    if (requestUnits.isEmpty()) throw IllegalStateException("未设置请求")
    val nowTime = Clock.System.now().toEpochMilliseconds()
    val newValues = Json.encodeToString(values)
    val isCacheValid = nowTime - responseTimestamp < cacheExpiration &&
        newValues == prevRequestValues
    prevRequestValues = newValues
    requestContentStatus = Requesting
    requestTimestamp = nowTime
    if (!isForce && isCacheValid) {
      for (unit in requestUnits.toList()) {
        if (unit.requestUnitStatus == RequestUnit.RequestUnitStatus.Success) {
          try {
            return Json.decodeFromString(resultSerializer, unit.response!!).apply {
              requestContentStatus = HitCache
            }
          } catch (e: Exception) {
            unit.error = e.stackTraceToString()
            unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
          }
        }
      }
    }
    var index = 0
    val parameters = parameterWithHint.mapValues { values[index++] }
    for (unit in requestUnits.toList()) {
      try {
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Requesting
        val response: String
        unit.duration = measureTime {
          response = withTimeout(10.seconds) {
            unit.request(parameters)
          }
        }.inWholeMilliseconds
        unit.response = response
        unit.error = null
        val result = Json.decodeFromString(resultSerializer, response)
        // 如果 result 反序列化异常，则认为请求失败
        responseTimestamp = Clock.System.now().toEpochMilliseconds()
        requestContentStatus = Success
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Success
        return result
      } catch (e: Exception) {
        // nothing
        unit.error = e.stackTraceToString()
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
      }
    }
    if (isCacheValid) {
      for (unit in requestUnits.toList()) {
        if (unit.requestUnitStatus == RequestUnit.RequestUnitStatus.Success) {
          try {
            return Json.decodeFromString(resultSerializer, unit.response!!).apply {
              requestContentStatus = FailureButHitCache
            }
          } catch (e: Exception) {
            unit.error = e.stackTraceToString()
            unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
          }
        }
      }
    }
    requestContentStatus = Failure
    throw IllegalStateException("请求全部失败")
  }

  init {
    AppComposeCoroutineScope.launch {
      snapshotFlow { requestUnits.toList() }.drop(1).collect {
        settings.putString("requestUnits", Json.encodeToString(it))
        if (requestUnits.isNotEmpty() && requestContentStatus == Empty) {
          requestContentStatus = None
        } else if (requestUnits.isEmpty()) {
          requestContentStatus = Empty
        }
      }
    }
  }

  @Composable
  override fun CardContent() {
    CardContent(this)
  }
}

@Composable
private fun CardContent(requestContent: RequestContent<*>) {
  Card(
    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    elevation = 2.dp,
  ) {
    val navigator = LocalNavigator.current
    Box(
      modifier = Modifier.fillMaxWidth()
        .clickable {
          navigator?.push(RequestContentScreen(requestContent.key))
        }
    ) {
      Column(
        modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 18.dp)
      ) {
        if (requestContent.editable) {
          EditTextCompose(
            text = requestContent.title,
            textStyle = TextStyle(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = LocalAppColors.current.tvLv1,
            ),
            isShowIndicatorLine = false,
          )
        } else {
          Text(
            text = requestContent.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.tvLv1,
          )
        }
        Text(
          modifier = Modifier.padding(top = 10.dp, start = 2.dp),
          text = buildAnnotatedString {
            append(
              AnnotatedString(
                "状态: ",
                SpanStyle(color = LocalAppColors.current.tvLv3),
              )
            )
            append(getRequestStatue(requestContent))
          },
          fontSize = 14.sp,
        )
        Text(
          modifier = Modifier.padding(top = 10.dp, start = 2.dp),
          text = if (requestContent.requestTimestamp == 0L) "未请求过" else {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = now - requestContent.requestTimestamp
            if (diff < 60 * 1000) {
              "刚刚已请求"
            } else {
              val minute = diff / 1000 / 60
              when {
                minute < 60 -> "$minute 分钟"
                minute < 24 * 60 -> "${minute / 60} 小时 ${minute % 60} 分钟"
                else -> "${minute / 60 / 24} 天"
              } + "前请求"
            }
          },
          fontSize = 14.sp,
          color = Color.Gray
        )
      }
      Image(
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
        painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowForwardIos),
        contentDescription = null,
      )
    }
  }
}

private fun getRequestStatue(requestContent: RequestContent<*>): AnnotatedString {
  return when (requestContent.requestContentStatus) {
    Empty -> AnnotatedString("未设置请求", SpanStyle(Color.Gray))
    None -> AnnotatedString("未触发请求", SpanStyle(Color(0xFF0099CC)))
    HitCache -> AnnotatedString("命中缓存", SpanStyle(Color(0xFF9DAC00)))
    Requesting -> AnnotatedString("请求中", SpanStyle(Color(0xFFFF8800)))
    Success -> AnnotatedString("成功", SpanStyle(Color(0xFF669900)))
    Failure -> AnnotatedString("失败", SpanStyle(Color(0xFFCC0000)))
    FailureButHitCache -> AnnotatedString.Builder().append(
      AnnotatedString("命中缓存(", SpanStyle(Color(0xFF9DAC00))),
      AnnotatedString("请求失败", SpanStyle(Color(0xFFCC0000))),
      AnnotatedString(")", SpanStyle(Color(0xFF9DAC00))),
    ).toAnnotatedString()
  }
}

enum class RequestContentStatus {
  Empty, // 未设置请求
  None, // 设置了请求但未触发过请求
  HitCache, // 命中缓存
  Requesting, // 请求中
  Success, // 请求成功
  Failure, // 请求失败
  FailureButHitCache, // 请求失败但缓存可用
}