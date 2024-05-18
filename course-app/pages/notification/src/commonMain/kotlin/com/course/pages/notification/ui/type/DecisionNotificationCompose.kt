package com.course.pages.notification.ui.type

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.onFailure
import com.course.components.utils.source.onSuccess
import com.course.source.app.notification.DecisionBtn
import com.course.source.app.notification.NotificationApi
import com.course.source.app.notification.NotificationContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:03
 */
@Composable
fun DecisionNotificationCompose(notificationId: Int, content: NotificationContent.Decision) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
      text = content.title,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
      text = content.content,
      fontSize = 13.sp,
      color = Color.Gray,
    )
    var btn by remember { mutableStateOf(content.btn) }
    AnimatedContent(
      targetState = btn,
    ) {
      when (it) {
        is DecisionBtn.Agree -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = it.positiveText,
              color = LocalAppColors.current.green,
              fontSize = 14.sp,
            )
          }
        }
        is DecisionBtn.Disagree -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = it.negativeText,
              color = LocalAppColors.current.red,
              fontSize = 14.sp,
            )
          }
        }
        is DecisionBtn.Expired -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = it.text,
              color = Color.Gray,
              fontSize = 14.sp,
            )
          }
        }
        is DecisionBtn.Pending -> Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          val coroutineScope = rememberCoroutineScope()
          Box(
            modifier = Modifier.weight(1F).height(40.dp).clickableCardIndicator(12.dp) {
              showRefuseDecisionDialog(
                coroutineScope,
                notificationId,
                it.negativeDialog,
                onRefuseSuccess = {
                  btn = DecisionBtn.Disagree(it.negativeText)
                  content.btn = btn
                },
                onExpired = { expired ->
                  btn = expired
                  content.btn = btn
                }
              )
            },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = null,
              tint = LocalAppColors.current.red,
            )
          }
          Box(
            modifier = Modifier.weight(1F).height(40.dp).clickableCardIndicator(12.dp) {
              submitAcceptDecision(
                coroutineScope,
                notificationId,
                onSuccess = {
                  btn = DecisionBtn.Agree(it.positiveText)
                  content.btn = btn
                },
                onExpired = { expired ->
                  btn = expired
                  content.btn = btn
                }
              )
            },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = LocalAppColors.current.green,
            )
          }
        }
      }
    }
  }
}

private fun showRefuseDecisionDialog(
  coroutineScope: CoroutineScope,
  notificationId: Int,
  text: String,
  onRefuseSuccess: () -> Unit,
  onExpired: (DecisionBtn.Expired) -> Unit,
) {
  showChooseDialog(onClickPositiveBtn = {
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(NotificationApi::class)
          .decision(notificationId, false)
      }.tryThrowCancellationException().onSuccess { wrapper ->
        wrapper.onSuccess {
          onRefuseSuccess.invoke()
        }.onFailure {
          if (it.code == 11000) {
            toast("已过期")
            onExpired.invoke(DecisionBtn.Expired(it.info))
          }
        }
        hide()
      }.onFailure {
        toast("网络异常")
      }
    }
  }) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = text)
    }
  }
}

private fun submitAcceptDecision(
  coroutineScope: CoroutineScope,
  notificationId: Int,
  onSuccess: () -> Unit,
  onExpired: (DecisionBtn.Expired) -> Unit,
) {
  coroutineScope.launch(Dispatchers.IO) {
    runCatching {
      Source.api(NotificationApi::class)
        .decision(notificationId, true)
    }.tryThrowCancellationException().onSuccess { wrapper ->
      wrapper.onSuccess {
        onSuccess.invoke()
      }.onFailure {
        if (it.code == 11000) {
          toast("已过期")
          onExpired.invoke(DecisionBtn.Expired(it.info))
        }
      }
    }.onFailure {
      toast("网络异常")
    }
  }
}