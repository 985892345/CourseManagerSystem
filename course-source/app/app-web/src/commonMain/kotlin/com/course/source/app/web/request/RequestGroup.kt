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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.longState
import com.course.source.app.web.source.page.RequestGroupScreen
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/4/13 10:32
 */
class RequestGroup<T : Any>(
  override val key: String,
  val name: String,
  val parameterWithHint: LinkedHashMap<String, String>,
  val resultSerializer: KSerializer<T?>,
  val format: String,
) : IRequestSource {

  companion object {
    fun find(key: String): RequestGroup<*>? {
      return SourceRequest.AllImpl.firstNotNullOfOrNull { source ->
        source.requestSource[key] as? RequestGroup<*>
      }
    }
  }

  private val settings = createSettings("RequestGroup-$key")

  var requestTimestamp: Long by settings.longState("requestTimestamp", 0L)
    private set

  val requestContents: SnapshotStateList<RequestContent<T>> =
    settings.getStringOrNull("requestContents")
      ?.let { value ->
        Json.decodeFromString<Map<String, String>>(value).map {
          RequestContent(
            key = it.key,
            name = it.value,
            parameterWithHint = parameterWithHint,
            resultSerializer = resultSerializer,
            format = format,
            editable = true,
          )
        }.toMutableStateList()
      } ?: SnapshotStateList()

  fun addRequestContent() {
    requestContents.add(
      RequestContent(
        key = "$key-${Clock.System.now().toEpochMilliseconds()}",
        name = "点击重命名",
        parameterWithHint = parameterWithHint,
        resultSerializer = resultSerializer,
        format = format,
        editable = true,
      )
    )
  }

  suspend fun request(isForce: Boolean, vararg values: String): List<T> {
    if (values.size != parameterWithHint.size)
      throw IllegalArgumentException("参数数量不匹配，应有 ${parameterWithHint.size}, 实有: ${values.size}")
    if (requestContents.isEmpty()) throw IllegalStateException("未设置请求")
    return requestContents.toList().mapNotNull {
      it.request(isForce, *values)
    }
  }

  @Composable
  override fun CardContent() {
    CardContent(this)
  }
}

@Composable
private fun CardContent(requestGroup: RequestGroup<*>) {
  Card(
    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    elevation = 2.dp,
  ) {
    val navigator = LocalNavigator.current
    Box(
      modifier = Modifier.fillMaxWidth()
        .clickable {
          navigator?.push(RequestGroupScreen(requestGroup.key))
        }
    ) {
      Column(
        modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 18.dp)
      ) {
        Text(
          text = requestGroup.name,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = LocalAppColors.current.tvLv1,
        )
        Text(
          modifier = Modifier.padding(top = 10.dp, start = 2.dp),
          text = buildStateString(requestGroup),
          fontSize = 14.sp,
        )
        Text(
          modifier = Modifier.padding(top = 10.dp, start = 2.dp),
          text = if (requestGroup.requestTimestamp == 0L) "未请求过" else {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = now - requestGroup.requestTimestamp
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

@Composable
private fun buildStateString(requestGroup: RequestGroup<*>): AnnotatedString {
  return buildAnnotatedString {
    append(
      AnnotatedString(
        "状态: ",
        SpanStyle(color = LocalAppColors.current.tvLv3),
      )
    )
    val list = requestGroup.requestContents.toList()
    if (list.isEmpty()) {
      append(AnnotatedString("未设置请求", SpanStyle(Color.Gray)))
    } else if (list.all { it.requestContentStatus == RequestContentStatus.None }) {
      append(AnnotatedString("未触发请求", SpanStyle(Color(0xFF0099CC))))
    } else {
      val success = list.sumOf {
        if (it.requestContentStatus == RequestContentStatus.Success) 1L else 0
      }
      val failure = list.sumOf {
        if (it.requestContentStatus == RequestContentStatus.Failure) 1L else 0
      }
      val requesting = list.sumOf {
        if (it.requestContentStatus == RequestContentStatus.Requesting) 1L else 0
      }
      if (requesting != 0L) {
        append(AnnotatedString("请求中 $requesting ", SpanStyle(Color(0xFFFF8800))))
      }
      append(AnnotatedString("成功 $success ", SpanStyle(Color(0xFF669900))))
      append(AnnotatedString("失败 $failure ", SpanStyle(Color(0xFFCC0000))))
    }
  }
}