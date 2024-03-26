package com.course.source.app.web.source.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.serializable.ObjectSerializable
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.request.RequestContent.RequestContentStatus.Empty
import com.course.source.app.web.request.RequestContent.RequestContentStatus.Failure
import com.course.source.app.web.request.RequestContent.RequestContentStatus.FailureButHitCache
import com.course.source.app.web.request.RequestContent.RequestContentStatus.HitCache
import com.course.source.app.web.request.RequestContent.RequestContentStatus.None
import com.course.source.app.web.request.RequestContent.RequestContentStatus.Requesting
import com.course.source.app.web.request.RequestContent.RequestContentStatus.Success
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 18:33
 */
@Serializable
@ObjectSerializable
class SourceScreen : Screen {

  @Composable
  override fun Content() {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      ListCompose()
    }
  }

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "数据源",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      val navigator = LocalNavigator.current
      if (navigator?.canPop == true) {
        Box(
          modifier = Modifier.align(Alignment.CenterStart)
            .padding(start = 12.dp)
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              navigator.pop()
            },
          contentAlignment = Alignment.Center,
        ) {
          Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(DrawableResource("drawable/ic_back.xml")),
            contentDescription = null,
          )
        }
      }
      Spacer(
        modifier = Modifier.align(Alignment.BottomStart)
          .background(Color(0xDDDEDEDE))
          .fillMaxWidth()
          .height(1.dp)
      )
    }
  }

  @Composable
  private fun ListCompose() {
    val requestContents = remember { RequestContent.RequestMap.values.toList() }
    LazyColumn(
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        items = requestContents,
        key = { it.name }
      ) {
        ListItemCompose(it)
      }
    }
  }

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  private fun ListItemCompose(requestContent: RequestContent<*>) {
    Card(
      modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
        .fillMaxWidth()
        .wrapContentHeight(),
      elevation = 2.dp,
    ) {
      val navigator = LocalNavigator.current
      Box(
        modifier = Modifier.fillMaxWidth()
          .clickable {
            navigator?.push(RequestContentScreen(requestContent.name))
          }
      ) {
        Column(
          modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 18.dp)
        ) {
          Text(
            text = requestContent.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.tvLv1,
          )
          Text(
            modifier = Modifier.padding(top = 10.dp, start = 2.dp),
            text = AnnotatedString.Builder().append(
              AnnotatedString(
                "状态: ",
                SpanStyle(color = LocalAppColors.current.tvLv3),
              ),
              getRequestStatue(requestContent)
            ).toAnnotatedString(),
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
          painter = painterResource(DrawableResource("drawable/ic_arrow_right.xml")),
          contentDescription = null,
        )
      }
    }
  }

  @Stable
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
}