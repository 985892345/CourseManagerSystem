package com.course.pages.team.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.team.utils.TeamDetailStateSerializer
import com.course.pages.team.utils.TeamMemberStateSerializer
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamDetail
import com.course.source.app.team.TeamMember
import com.course.source.app.team.TeamRank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
  private var teamDetailState = mutableStateOf<TeamDetail?>(null)

  @Serializable(TeamMemberStateSerializer::class)
  private val adminList = mutableStateOf(emptyList<TeamMember>())

  @Serializable(TeamMemberStateSerializer::class)
  private val managerList = mutableStateOf(emptyList<TeamMember>())

  @Serializable(TeamMemberStateSerializer::class)
  private val memberList = mutableStateOf(emptyList<TeamMember>())

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      MemberListCompose()
    }
    LaunchedEffect(Unit) {
      launch(Dispatchers.IO) {
        runCatching {
          Source.api(TeamApi::class)
            .getTeamDetail(teamBean.id)
            .getOrThrow()
        }.tryThrowCancellationException().onSuccess { detail ->
          val admin = mutableListOf<TeamMember>()
          val manager = mutableListOf<TeamMember>()
          val member = mutableListOf<TeamMember>()
          detail.members.sortedBy { it.name }.fastForEach {
            when (it.rank) {
              TeamRank.Administrator -> admin.add(it)
              TeamRank.Manager -> manager.add(it)
              TeamRank.Member -> member.add(it)
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
            when (teamBean.rank) {
              TeamRank.Administrator -> navigator?.push(
                TeamSettingScreen(teamBean, managerList.value, memberList.value)
              )

              TeamRank.Manager, TeamRank.Member -> showExitDialog(coroutineScope, navigator)
            }
          },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = when (teamBean.rank) {
              TeamRank.Administrator -> Icons.Outlined.Settings
              TeamRank.Manager, TeamRank.Member -> Icons.AutoMirrored.Rounded.ExitToApp
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
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
      modifier = Modifier.animateItemPlacement(),
      text = text,
      fontSize = 14.sp,
    )
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(member: TeamMember) {
    Card(
      modifier = Modifier.animateItemPlacement(),
      elevation = 0.5.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
          .clickable {
            MemberCourseBottomSheet(member).also {
              it.showCourseBottomSheet()
            }
          }.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
      ) {
        Icon(
          modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
          imageVector = Icons.Outlined.AccountCircle,
          contentDescription = null,
        )
        Column(modifier = Modifier.padding(start = 8.dp, top = 2.dp).weight(1F)) {
          Text(
            text = member.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LocalAppColors.current.tvLv2,
          )
          Text(
            text = member.num,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
      onClickPositionBtn = {
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(TeamApi::class)
              .deleteTeam(teamBean.id)
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
}