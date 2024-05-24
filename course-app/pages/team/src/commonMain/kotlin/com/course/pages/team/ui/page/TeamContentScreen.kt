package com.course.pages.team.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ForwardToInbox
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.FloatStateSerializable
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.view.edit.EditTextCompose
import com.course.pages.team.ui.course.TeamCourseBottomSheet
import com.course.pages.team.utils.TeamDetailStateSerializer
import com.course.pages.team.utils.TeamMemberStateSerializer
import com.course.source.app.team.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:33
 */
@Serializable
@ObjectSerializable
class TeamContentScreen(
  var teamBean: TeamBean,
) : BaseScreen() {

  @Serializable(TeamDetailStateSerializer::class)
  private val teamDetailState = mutableStateOf<TeamDetail?>(null)

  @Serializable(TeamMemberStateSerializer::class)
  private val adminList = mutableStateOf(emptyList<TeamMember>())

  @Serializable(TeamMemberStateSerializer::class)
  private val managerList = mutableStateOf(emptyList<TeamMember>())

  @Serializable(TeamMemberStateSerializer::class)
  private val memberList = mutableStateOf(emptyList<TeamMember>())

  @Composable
  override fun ScreenContent() {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding().pointerInput(Unit) {
      awaitEachGesture {
        awaitFirstDown(false, PointerEventPass.Initial)
        // 接收每次 ACTION_DOWN 事件
        if (floatBtnAnimFraction.value == 1F) {
          coroutineScope.launch { closeFloatBtn() }
        }
      }
    }) {
      Column(modifier = Modifier) {
        ToolbarCompose()
        MemberListCompose()
      }
      FloatingActionButtonCompose()
    }
    requestTeaDetail()
  }

  @Composable
  private fun requestTeaDetail() {
    LaunchedEffect(Unit) {
      launch(Dispatchers.IO) {
        runCatching {
          Source.api(TeamApi::class)
            .getTeamDetail(teamBean.teamId)
            .getOrThrow()
        }.tryThrowCancellationException().onSuccess { detail ->
          val admin = mutableListOf<TeamMember>()
          val manager = mutableListOf<TeamMember>()
          val member = mutableListOf<TeamMember>()
          detail.members.fastForEach {
            when (it.role) {
              TeamRole.Administrator -> admin.add(it)
              TeamRole.Manager -> manager.add(it)
              TeamRole.Member -> member.add(it)
            }
          }
          adminList.value = admin
          managerList.value = manager
          memberList.value = member
          teamDetailState.value = detail
        }.onFailure {
          toast("网络异常")
        }
      }
    }
  }

  @Composable
  private fun ToolbarCompose() {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = teamBean.name,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
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
      AnimatedVisibility(
        modifier = Modifier.align(Alignment.CenterEnd)
          .padding(end = 12.dp)
          .size(32.dp),
        visible = teamDetailState.value != null,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Box(
          modifier = Modifier.clickableCardIndicator {
            when (teamBean.role) {
              TeamRole.Administrator -> navigator?.push(
                TeamSettingScreen(teamBean, managerList.value, memberList.value)
              )

              TeamRole.Manager, TeamRole.Member -> showExitDialog(coroutineScope, navigator)
            }
          },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = when (teamBean.role) {
              TeamRole.Administrator -> Icons.Outlined.Settings
              TeamRole.Manager, TeamRole.Member -> Icons.AutoMirrored.Rounded.ExitToApp
            },
            contentDescription = null,
          )
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun MemberListCompose() {
    LazyColumn(
      modifier = Modifier.fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (adminList.value.isNotEmpty()) {
        stickyHeader(key = "admin header", contentType = "header") {
          ListHeaderCompose("队长")
        }
        items(adminList.value, key = { it.num }, contentType = { "content" }) {
          ListContentCompose(it)
        }
      }
      if (managerList.value.isNotEmpty()) {
        stickyHeader(key = "manager header", contentType = "header") {
          ListHeaderCompose("管理")
        }
        items(managerList.value, key = { it.num }, contentType = { "content" }) {
          ListContentCompose(it)
        }
      }
      if (memberList.value.isNotEmpty()) {
        stickyHeader(key = "member header", contentType = "header") {
          ListHeaderCompose("成员")
        }
        items(memberList.value, key = { it.num }, contentType = { "content" }) {
          ListContentCompose(it)
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListHeaderCompose(text: String) {
    Text(
      modifier = Modifier.fillMaxWidth().animateItemPlacement()
        .background(MaterialTheme.colors.background),
      text = text,
      fontSize = 14.sp,
    )
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(member: TeamMember) {
    Card(
      modifier = Modifier.animateItemPlacement().padding(end = 1.dp), // end padding 为解决 stickyHeader 右边界线问题
      shape = RoundedCornerShape(8.dp),
      elevation = 0.5.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
          .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
      ) {
        Icon(
          modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
          imageVector = Icons.Outlined.AccountCircle,
          contentDescription = null,
        )
        Column(modifier = Modifier.padding(start = 8.dp, top = 2.dp).weight(1F)) {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = member.name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = LocalAppColors.current.tvLv2,
            )
            if (!member.isConfirmed) {
              Text(
                modifier = Modifier,
                text = "（未回复邀请）",
                fontSize = 12.sp,
                color = Color(0xFF666666),
              )
            }
          }
          Text(
            text = member.num,
            fontSize = 12.sp,
            color = Color(0xFF666666),
          )
        }
        Text(
          modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            .align(Alignment.CenterVertically),
          text = member.identity,
          fontSize = 12.sp,
          textAlign = TextAlign.End,
        )
      }
    }
  }

  private fun showExitDialog(coroutineScope: CoroutineScope, navigator: Navigator?) {
    showChooseDialog(
      onClickPositiveBtn = {
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(TeamApi::class)
              .deleteTeam(teamBean.teamId)
              .getOrThrow()
          }.tryThrowCancellationException().onSuccess {
            toast("退出成功")
            hide()
            navigator?.pop()
          }.onFailure {
            toast("网络异常")
          }
        }
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "确定要退出该团队吗?"
        )
      }
    }
  }

  @Serializable(FloatStateSerializable::class)
  private val floatBtnAnimFraction = mutableFloatStateOf(0F)

  @Transient
  private val floatBtnActions = listOf(
    FloatBtnAction(Icons.Rounded.CalendarMonth) { _, _ ->
      TeamCourseBottomSheet(
        teamBean = teamBean,
        members = teamDetailState.value?.members
          ?.filter { it.isConfirmed }
          ?: emptyList()
      ).showCourseBottomSheet()
    },
    FloatBtnAction(Icons.AutoMirrored.Outlined.ForwardToInbox) { coroutineScope, _ ->
      showSendNotificationDialog(coroutineScope)
    },
  )

  @Composable
  private fun BoxScope.FloatingActionButtonCompose() {
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    floatBtnActions.fastForEachIndexed { index, btnAction ->
      FloatingActionButton(
        modifier = Modifier.align(Alignment.BottomEnd)
          .padding(end = 46.dp, bottom = 66.dp)
          .size(44.dp)
          .graphicsLayer {
            translationY =
              (-(index + 1) * 56.dp.toPx() - 8.dp.toPx()) * floatBtnAnimFraction.value
            alpha = minOf(floatBtnAnimFraction.value * 1.25F, 1F) // 因阴影会失效，所以 alpha 特殊设置
          },
        onClick = { btnAction.onClick(coroutineScope, navigator) }
      ) {
        Box(
          modifier = Modifier.fillMaxSize().graphicsLayer {
            rotationZ = -90F * (1 - floatBtnAnimFraction.value)
          },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = btnAction.icon,
            contentDescription = null,
          )
        }
      }
    }
    FloatingActionButton(
      modifier = Modifier.align(Alignment.BottomEnd)
        .padding(end = 40.dp, bottom = 60.dp),
      onClick = {
        if (floatBtnAnimFraction.value == 0F) {
          coroutineScope.launch { openFloatBtn() }
        }
      },
    ) {
      Box(
        modifier = Modifier.size(50.dp).graphicsLayer {
          rotationZ = -45F * floatBtnAnimFraction.value
        },
        contentAlignment = Alignment.Center,
      ) {
        Image(
          modifier = Modifier.padding(start = 4.dp, bottom = 1.dp),
          imageVector = Icons.AutoMirrored.Rounded.Send,
          contentDescription = null
        )
      }
    }
  }

  private suspend fun openFloatBtn() {
    if (floatBtnAnimFraction.value == 0F) {
      animate(0F, 1F, animationSpec = tween()) { value, _ ->
        floatBtnAnimFraction.value = value
      }
    }
  }

  private suspend fun closeFloatBtn() {
    if (floatBtnAnimFraction.value == 1F) {
      animate(1F, 0F, animationSpec = tween()) { value, _ ->
        floatBtnAnimFraction.value = value
      }
    }
  }

  private fun showSendNotificationDialog(coroutineScope: CoroutineScope) {
    val editTitle = mutableStateOf("")
    val editDescription = mutableStateOf("")
    showChooseDialog(
      height = 300.dp,
      positiveBtnText = "发送",
      onDismissRequest = {},
      onClickPositiveBtn = {
        if (editTitle.value.isBlank()) {
          toast("标题不能为空")
          return@showChooseDialog
        }
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(TeamApi::class)
              .sendTeamNotification(teamBean.teamId, editTitle.value, editDescription.value)
              .getOrThrow()
          }.tryThrowCancellationException().onSuccess {
            toast("发送成功")
            hide()
          }.onFailure {
            toast("网络异常")
          }
        }
      }
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        EditTextCompose(
          modifier = Modifier.fillMaxWidth().height(20.dp),
          text = editTitle,
          hint = "请输入标题",
          textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
          )
        )
        Card(
          modifier = Modifier.padding(top = 8.dp).weight(1F),
          elevation = 0.5.dp,
        ) {
          EditTextCompose(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
              .fillMaxSize(),
            text = editDescription,
            hint = "请输入内容",
            isShowIndicatorLine = false,
            textStyle = TextStyle(
              fontSize = 12.sp,
            )
          )
        }
      }
    }
  }

  private inner class FloatBtnAction(
    val icon: ImageVector,
    val onClick: (coroutineScope: CoroutineScope, navigator: Navigator?) -> Unit
  )
}