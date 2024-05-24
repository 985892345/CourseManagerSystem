package com.course.pages.notification.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.notification.ui.type.AddScheduleNotificationCompose
import com.course.pages.notification.ui.type.DecisionNotificationCompose
import com.course.pages.notification.ui.type.NormalNotificationCompose
import com.course.source.app.notification.Notification
import com.course.source.app.notification.NotificationApi
import com.course.source.app.notification.NotificationContent
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:00
 */
@Serializable
@ObjectSerializable
class NotificationScreen(
  val backable: Boolean
) : BaseScreen() {

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      ListCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "消息通知",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      if (backable) {
        val navigator = LocalNavigator.current
        Box(
          modifier = Modifier.align(Alignment.CenterStart)
            .padding(start = 12.dp)
            .size(32.dp)
            .clickableCardIndicator {
              navigator?.pop()
            },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
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
  private fun ColumnScope.ListCompose() {
    var list by remember { mutableStateOf(emptyList<Notification>()) }
    LazyColumn(
      modifier = Modifier.fillMaxWidth()
        .weight(1F)
        .padding(top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
      contentPadding = PaddingValues(bottom = 64.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(list, contentType = { it.content::class }) {
        ListContentCompose(it)
      }
    }
    LaunchedEffect(Unit) {
      runCatching {
        Source.api(NotificationApi::class)
          .getNotifications()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess { notificationList ->
        list = notificationList
      }.onFailure {
        toast("网络异常")
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(notification: Notification) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).animateItemPlacement()) {
      Text(
        modifier = Modifier.padding(start = 2.dp),
        text = notification.time.toString(),
        fontSize = 11.sp,
        color = Color.Gray,
      )
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.5.dp,
      ) {
        when (val content = notification.content) {
          is NotificationContent.Normal -> NormalNotificationCompose(content)
          is NotificationContent.Decision -> DecisionNotificationCompose(notification.notificationId, content)
        }
      }
    }
  }
}