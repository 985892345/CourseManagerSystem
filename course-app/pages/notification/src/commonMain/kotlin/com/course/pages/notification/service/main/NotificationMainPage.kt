package com.course.pages.notification.service.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.main.api.IMainPage
import com.course.pages.notification.ui.NotificationScreen
import com.course.source.app.notification.NotificationApi
import com.g985892345.provider.api.annotation.ImplProvider
import coursemanagersystem.course_app.pages.notification.generated.resources.Res
import coursemanagersystem.course_app.pages.notification.generated.resources.ic_notification_bottom_bar
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 10:57
 */
@ImplProvider(clazz = IMainPage::class, name = "NotificationMainPage")
class NotificationMainPage : IMainPage {

  override val priority: Int
    get() = 150

  private val notificationScreen = NotificationScreen(backable = false)

  @Composable
  override fun Content(appBarHeight: Dp) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = appBarHeight)) {
      notificationScreen.Content()
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selected: State<Boolean>, selectToPosition: () -> Unit) {
    val hasNewNotification = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
      runCatching {
        Source.api(NotificationApi::class)
          .hasNewNotification()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        hasNewNotification.value = it
      }
    }
    val red = LocalAppColors.current.red
    Box(
      modifier = Modifier.size(32.dp)
        .clickableCardIndicator {
          hasNewNotification.value = false
          selectToPosition()
        }
        .drawWithContent {
          drawContent()
          if (hasNewNotification.value) {
            val radius = 2.dp.toPx()
            drawCircle(
              color = red,
              radius = radius,
              center = Offset(size.width - radius - 8, radius + 8),
            )
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.padding(top = 2.dp),
        painter = painterResource(Res.drawable.ic_notification_bottom_bar),
        contentDescription = null,
        tint = if (selected.value) Color.Black else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
      )
    }
  }
}