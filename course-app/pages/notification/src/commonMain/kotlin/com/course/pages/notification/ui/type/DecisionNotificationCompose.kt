package com.course.pages.notification.ui.type

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.course.source.app.notification.NotificationContent
import com.course.source.app.team.TeamApi
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
fun DecisionNotificationCompose(content: NotificationContent.Decision) {
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
    var agreeOrNot by remember { mutableStateOf(content.agreeOrNot) }
    AnimatedContent(
      targetState = agreeOrNot,
    ) {
      when (it) {
        null -> Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          val coroutineScope = rememberCoroutineScope()
          Box(
            modifier = Modifier.weight(1F).height(40.dp).clickableCardIndicator(12.dp) {
              showRefuseDecisionDialog(coroutineScope, content) {
                content.agreeOrNot = false
                agreeOrNot = false
              }
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
              submitAcceptDecision(coroutineScope, content) {
                content.agreeOrNot = true
                agreeOrNot = true
              }
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

        true, false -> Box(
          modifier = Modifier.fillMaxWidth().height(40.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = if (it) content.positiveText else content.negativeText,
            color = if (it) LocalAppColors.current.green else LocalAppColors.current.red,
            fontSize = 14.sp,
          )
        }
      }
    }
  }
}

private fun showRefuseDecisionDialog(
  coroutineScope: CoroutineScope,
  content: NotificationContent.Decision,
  onRefuseSuccess: () -> Unit,
) {
  showChooseDialog(onClickPositiveBtn = {
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .refuseDecision(content.id)
      }.tryThrowCancellationException().onSuccess {
        onRefuseSuccess.invoke()
        hide()
      }.onFailure {
        toast("网络异常")
      }
    }
  }) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = content.negativeDialog)
    }
  }
}

private fun submitAcceptDecision(
  coroutineScope: CoroutineScope,
  content: NotificationContent.Decision,
  onSuccess: () -> Unit
) {
  coroutineScope.launch(Dispatchers.IO) {
    runCatching {
      Source.api(TeamApi::class)
        .acceptDecision(content.id)
    }.tryThrowCancellationException().onSuccess {
      onSuccess.invoke()
    }.onFailure {
      toast("网络异常")
    }
  }
}