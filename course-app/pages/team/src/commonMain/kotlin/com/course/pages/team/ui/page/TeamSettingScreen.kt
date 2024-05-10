package com.course.pages.team.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.GroupOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamMember
import com.course.source.app.team.TeamRank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 13:10
 */
@Serializable
@ObjectSerializable
class TeamSettingScreen(
  val teamBean: TeamBean,
  val managerList: List<TeamMember>,
  val memberList: List<TeamMember>,
) : BaseScreen() {

  private val detailEditPage = TeamDetailEditPage(
    initialName = teamBean.name,
    initialIdentity = teamBean.identity,
    initialDescription = teamBean.description,
    initialManagerList = managerList.map { member ->
      TeamDetailEditPage.MemberData(
        name = member.name,
        num = member.num,
        type = member.type,
      ).also { it.identity.value = member.identity }
    },
    initialMemberList = memberList.map { member ->
      TeamDetailEditPage.MemberData(
        name = member.name,
        num = member.num,
        type = member.type,
      ).also { it.identity.value = member.identity }
    }
  )

  @Transient
  private var navigator: Navigator? = null

  private val closeBackHandleEnable by registerBackHandle {
    tryShowCloseDialog()
  }

  @Composable
  override fun ScreenContent() {
    navigator = LocalNavigator.current
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      EditContentCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "团队设置",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      Box(
        modifier = Modifier.align(Alignment.CenterStart)
          .padding(start = 12.dp)
          .size(32.dp)
          .clickableCardIndicator {
            tryShowCloseDialog()
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
      if (teamBean.dissolvable) {
        Box(
          modifier = Modifier.align(Alignment.CenterEnd)
            .padding(end = 12.dp)
            .size(32.dp)
            .clickableCardIndicator {
              showDissolveDialog(coroutineScope)
            },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.GroupOff,
            contentDescription = null,
          )
        }
      }
    }
  }

  @Composable
  private fun EditContentCompose() {
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    detailEditPage.Content(this) {
      coroutineScope.launch(Dispatchers.IO) {
        runCatching {
          Source.api(TeamApi::class).updateTeam(
            teamId = teamBean.teamId,
            name = detailEditPage.editName.value,
            identity = detailEditPage.editIdentity.value,
            description = detailEditPage.editDescription.value,
            members = detailEditPage.managerList.value.map {
              it.toMember(TeamRank.Manager)
            } + detailEditPage.memberList.value.map {
              it.toMember(TeamRank.Member)
            },
          ).getOrThrow()
        }.tryThrowCancellationException().onSuccess {
          toast("修改成功")
          val contentScreen = navigator?.items?.let { it.getOrNull(it.size - 2) }
          if (contentScreen is TeamContentScreen) {
            contentScreen.teamBean = contentScreen.teamBean.copy(
              name = detailEditPage.editName.value,
              identity = detailEditPage.editIdentity.value,
              description = detailEditPage.editDescription.value,
            )
          }
          navigator?.pop()
        }.onFailure {
          toast("网络异常")
        }
      }
    }
  }

  private fun showDissolveDialog(coroutineScope: CoroutineScope) {
    showChooseDialog(
      onClickPositionBtn = {
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(TeamApi::class)
              .deleteTeam(teamBean.teamId)
              .getOrThrow()
          }.tryThrowCancellationException().onSuccess {
            toast("解散成功")
            hide()
            val navigator = navigator
            if (navigator?.items?.let { it.getOrNull(it.size - 2) } is TeamContentScreen) {
              // 这里要连跳两个
              navigator.pop()
              navigator.pop()
            } else {
              navigator?.pop()
            }
          }.onFailure {
            toast("网络异常")
          }
        }
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "确定要解散该团队吗?"
        )
      }
    }
  }

  private fun tryShowCloseDialog() {
    if (detailEditPage.hasChange()) {
      showChooseDialog(
        onClickPositionBtn = {
          navigator?.pop()
          hide()
        }
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "放弃编辑吗？"
          )
        }
      }
    } else {
      navigator?.pop()
    }
  }
}