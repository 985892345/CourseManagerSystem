package com.course.pages.team.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamNotification
import com.course.source.app.team.TeamNotificationContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 13:33
 */
@Serializable
@ObjectSerializable
class TeamNotificationScreen : BaseScreen() {

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
    var list by remember { mutableStateOf(emptyList<TeamNotification>()) }
    LazyColumn(
      modifier = Modifier.fillMaxWidth()
        .weight(1F)
        .padding(top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
      contentPadding = PaddingValues(bottom = 64.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(list, key = { it.id }) {
        ListContentCompose(it)
      }
    }
    LaunchedEffect(Unit) {
      runCatching {
        Source.api(TeamApi::class)
          .getTeamNotification()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        list = it
      }.onFailure {
        toast("网络异常")
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(notification: TeamNotification) {
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
          is TeamNotificationContent.Normal -> NormalContentCompose(content)
          is TeamNotificationContent.AddSchedule -> AddScheduleContentCompose(content)
          is TeamNotificationContent.InviteJoinTeam -> InviteJoinTeamContentCompose(content)
        }
      }
    }
  }

  @Composable
  private fun NormalContentCompose(content: TeamNotificationContent.Normal) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
      Text(
        modifier = Modifier,
        text = content.title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = content.content,
        fontSize = 13.sp,
        color = Color.Gray,
      )
    }
  }

  @Composable
  private fun AddScheduleContentCompose(content: TeamNotificationContent.AddSchedule) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
      Text(
        modifier = Modifier,
        text = "新的日程: ${content.scheduleTitle}",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = "时间: ${content.scheduleStartTime}-${
          content.scheduleStartTime.time.plusMinutes(
            content.scheduleMinuteDuration
          )
        }",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = content.scheduleDescription,
        fontSize = 13.sp,
        color = Color.Gray,
      )
      Text(
        modifier = Modifier.padding(top = 8.dp).align(Alignment.End),
        text = "由${content.teamName}—${content.teamSenderName}创建",
        fontSize = 11.sp,
        color = Color.LightGray,
      )
    }
  }

  @Composable
  private fun InviteJoinTeamContentCompose(content: TeamNotificationContent.InviteJoinTeam) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
        text = "${content.teamAdministratorName}邀请你加入${content.teamName}",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        text = "团队简介：${content.teamDescription.ifEmpty { "无" }}",
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
                showRefuseJoinTeamDialog(coroutineScope, content) {
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
                submitAcceptJoinTeam(coroutineScope, content) {
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
              text = if (it) "已接受" else "已拒绝",
              color = if (it) LocalAppColors.current.green else LocalAppColors.current.red,
              fontSize = 14.sp,
            )
          }
        }
      }
    }
  }

  private fun showRefuseJoinTeamDialog(
    coroutineScope: CoroutineScope,
    content: TeamNotificationContent.InviteJoinTeam,
    onRefuseSuccess: () -> Unit,
  ) {
    showChooseDialog(onClickPositionBtn = {
      coroutineScope.launch(Dispatchers.IO) {
        runCatching {
          Source.api(TeamApi::class)
            .refuseJoinTeam(content.teamId)
        }.tryThrowCancellationException().onSuccess {
          onRefuseSuccess.invoke()
          hide()
        }.onFailure {
          toast("网络异常")
        }
      }
    }) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "确定取消加入${content.teamName}吗？")
      }
    }
  }

  private fun submitAcceptJoinTeam(
    coroutineScope: CoroutineScope,
    content: TeamNotificationContent.InviteJoinTeam,
    onSuccess: () -> Unit
  ) {
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .acceptJoinTeam(content.teamId)
      }.tryThrowCancellationException().onSuccess {
        onSuccess.invoke()
      }.onFailure {
        toast("网络异常")
      }
    }
  }
}