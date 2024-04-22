package com.course.source.app.local.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.dialog.showChooseDialog
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.longState
import com.course.components.utils.preferences.stringState
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.local.request.RequestContentStatus.Empty
import com.course.source.app.local.request.RequestContentStatus.Failure
import com.course.source.app.local.request.RequestContentStatus.FailureButHitCache
import com.course.source.app.local.request.RequestContentStatus.HitCache
import com.course.source.app.local.request.RequestContentStatus.None
import com.course.source.app.local.request.RequestContentStatus.Requesting
import com.course.source.app.local.request.RequestContentStatus.Success
import com.course.source.app.local.source.page.RequestContentScreen
import com.russhwolf.settings.long
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
  val initialCacheExpiration: Duration = 12.hours,
) : IRequestSource {

  companion object {

    val testRequestContent = RequestContent(
      key = "test",
      name = "测试",
      parameterWithHint = linkedMapOf(),
      resultSerializer = kotlinx.serialization.json.Json.serializersModule.serializer<String?>(),
      format = "",
    )

    fun find(key: String): RequestContent<*>? {
      if (key == "test") return testRequestContent
      return SourceRequest.AllImpl.firstNotNullOfOrNull { source ->
        source.requestSource.firstNotNullOfOrNull { entry ->
          when (val request = entry.value) {
            is RequestGroup<*> -> request.requestContents.find { it.key == key }
            is RequestContent<*> -> if (request.key == key) request else null
          }
        }
      }
    }

    val Json = Json {
      ignoreUnknownKeys = true
      isLenient = true
      coerceInputValues = true
    }
  }

  private val settings = createSettings("RequestContent-$key")

  val requestUnits: SnapshotStateList<RequestUnit> = settings.getStringOrNull("requestUnits")
    ?.let {
      runCatching { Json.decodeFromString<List<RequestUnit>>(it) }.onFailure {
        settings.remove("requestUnits")
      }.getOrNull()?.toMutableStateList()
    } ?: SnapshotStateList()

  val title: MutableState<String> by lazy { settings.stringState("title", name) }

  var requestTimestamp: Long by settings.longState("requestTimestamp", 0L)
    private set

  private var responseTimestamp: Long by settings.longState("responseTimestamp", 0L)

  var cacheExpiration: Long by settings.long(
    "cacheExpiration",
    initialCacheExpiration.inWholeMilliseconds
  )

  private val cacheMap: MutableMap<String, RequestCache> = settings.getStringOrNull("cacheMap")
    ?.let {
      runCatching { Json.decodeFromString<Map<String, RequestCache>>(it) }.onFailure {
        settings.remove("cacheMap")
      }.getOrNull()?.toMutableMap()
    } ?: SnapshotStateMap()

  var requestContentStatus: RequestContentStatus by mutableStateOf(
    when {
      requestUnits.isEmpty() -> Empty
      requestTimestamp == 0L -> None
      responseTimestamp >= requestTimestamp -> Success
      else -> Failure
    }
  )
    private set

  suspend fun request(isForce: Boolean, cacheable: Boolean, vararg values: String): T? {
    if (values.size != parameterWithHint.size)
      throw IllegalArgumentException("参数数量不匹配，应有 ${parameterWithHint.size}, 实有: ${values.size}")
    if (requestUnits.isEmpty()) throw IllegalStateException("未设置请求")
    requestContentStatus = Requesting
    val nowTime = Clock.System.now().toEpochMilliseconds()
    val cacheKey = Json.encodeToString(values)
    // 如果不强制请求则尝试从缓存中获取数据
    if (!isForce) {
      val cache = cacheMap[cacheKey]
      if (cache != null && nowTime - cache.responseTimestamp < cacheExpiration) {
        val response = cache.response ?: return null
        runCatching {
          Json.decodeFromString(resultSerializer, response)
        }.onSuccess {
          requestContentStatus = HitCache
          return it
        }.onFailure {
          cache.clear()
        }
      }
    }
    // 请求开始
    requestTimestamp = nowTime
    var index = 0
    val parameters = parameterWithHint.mapValues { values[index++] }
    for (unit in requestUnits.toList()) {
      try {
        return requestUnit(unit, parameters, cacheable, cacheKey)
      } catch (e: CancellationException) {
        unit.error = e.stackTraceToString()
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
        requestContentStatus = Failure
        throw e
      } catch (e: Exception) {
        unit.error = e.stackTraceToString()
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
      }
    }
    // 请求失败则尝试从缓存中拿数据
    val cache = cacheMap[cacheKey]
    if (cache != null && nowTime - cache.responseTimestamp < cacheExpiration) {
      val response = cache.response ?: return null
      runCatching {
        Json.decodeFromString(resultSerializer, response)
      }.onSuccess {
        requestContentStatus = FailureButHitCache
        return it
      }.onFailure {
        cache.clear()
      }
    }
    requestContentStatus = Failure
    throw IllegalStateException("请求全部失败")
  }

  private suspend fun requestUnit(
    unit: RequestUnit,
    parameters: Map<String, String>,
    cacheable: Boolean,
    cacheKey: String,
  ): T? {
    unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Requesting
    val response: String = withTimeout(10.seconds) {
      unit.request(parameters)
    }
    unit.error = null
    // 如果 result 反序列化异常，则认为请求失败
    val result = Json.decodeFromString(resultSerializer, response)
    if (result != null) {
      // requestUnit 不记录 null 值
      unit.response = response
    }
    responseTimestamp = Clock.System.now().toEpochMilliseconds()
    requestContentStatus = Success
    unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Success
    if (cacheable) {
      // 保存缓存
      val cache = cacheMap.getOrPut(cacheKey) { RequestCache(key, cacheKey) }
      cache.response = response
      cache.responseTimestamp = responseTimestamp
    }
    return result
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
    AppComposeCoroutineScope.launch {
      snapshotFlow { cacheMap.toMap() }.drop(1).collect {
        settings.putString("cacheMap", Json.encodeToString(it))
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
    val title = remember { mutableStateOf(requestContent.title.value) }
    val navigator = LocalNavigator.current
    Box(
      modifier = Modifier.fillMaxWidth()
        .clickable {
          if (requestContent.editable) {
            requestContent.title.value = title.value
          }
          navigator?.push(RequestContentScreen(requestContent.key))
        }
    ) {
      Column(
        modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 18.dp)
      ) {
        if (requestContent.editable) {
          DisposableEffect(Unit) {
            onDispose { requestContent.title.value = title.value }
          }
          EditTextCompose(
            text = title,
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
          text = getTimeStr(requestContent),
          fontSize = 14.sp,
          color = Color.Gray
        )
      }
      if (requestContent.editable) {
        Box(
          modifier = Modifier.align(Alignment.CenterEnd)
            .padding(end = 40.dp)
            .clickableCardIndicator {
              clickDelete(requestContent)
            }
        ) {
          Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = Icons.Default.Delete,
            contentDescription = null,
          )
        }
      }
      Image(
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
        painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowForwardIos),
        contentDescription = null,
      )
    }
  }
}

@Composable
private fun getTimeStr(requestContent: RequestContent<*>): AnnotatedString {
  return if (requestContent.requestTimestamp == 0L) {
    AnnotatedString("未请求过", SpanStyle(Color.Gray))
  } else {
    var now by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
      while (true) {
        delay(1.minutes)
        now += 1.minutes.inWholeMilliseconds
      }
    }
    val diff = (now - requestContent.requestTimestamp).milliseconds
    if (diff < 1.minutes) {
      AnnotatedString("刚刚已请求", SpanStyle(Color.Gray))
    } else {
      AnnotatedString(
        when {
          diff < 1.hours -> "${diff.inWholeMinutes} 分钟"
          diff < 1.days -> "${diff.inWholeHours} 小时 ${diff.inWholeMinutes % 60} 分钟"
          else -> "${diff.inWholeDays} 天"
        } + "前请求", SpanStyle(
          // 如果缓存已经过期了，则显示为红色进行提醒
          if (now > requestContent.cacheExpiration + requestContent.requestTimestamp) Color.Red
          else Color.Gray
        )
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

private fun clickDelete(
  requestContent: RequestContent<*>,
) {
  showChooseDialog(
    onClickPositionBtn = {
      val requestGroupKey = requestContent.key.substringBeforeLast("-")
      RequestGroup.find(requestGroupKey)?.requestContents?.remove(requestContent)
      hide()
    }
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = "确认删除吗？",
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center
      )
    }
  }
}