package com.course.pages.team.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.course.source.app.team.TeamRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:18
 */
@Serializable
@ObjectSerializable
class AddTeamScreen : BaseScreen() {

  private val detailEditPage = TeamDetailEditPage(
    initialName = "",
    initialIdentity = "",
    initialDescription = "",
    initialManagerList = emptyList(),
    initialMemberList = emptyList(),
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
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "新建团队",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      Box(
        modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp).size(32.dp)
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
        modifier = Modifier.align(Alignment.BottomStart).background(Color(0xDDDEDEDE))
          .fillMaxWidth().height(1.dp)
      )
    }
  }

  @Composable
  private fun EditContentCompose() {
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    detailEditPage.Content(this@AddTeamScreen) {
      coroutineScope.launch(Dispatchers.IO) {
        runCatching {
          Source.api(TeamApi::class).createTeam(
            name = detailEditPage.editName.value,
            identity = detailEditPage.editIdentity.value,
            description = detailEditPage.editDescription.value,
            members = detailEditPage.managerList.value.map {
              it.toMember(TeamRole.Manager)
            } + detailEditPage.memberList.value.map {
              it.toMember(TeamRole.Member)
            },
          ).getOrThrow()
        }.tryThrowCancellationException().onSuccess {
          toast("添加成功")
          navigator?.pop()
        }.onFailure {
          toast("网络异常")
        }
      }
    }
  }

  private fun tryShowCloseDialog() {
    if (detailEditPage.hasChange()) {
      showChooseDialog(
        onClickPositiveBtn = {
          navigator?.pop()
          hide()
        }
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "放弃新建团队吗？"
          )
        }
      }
    } else {
      navigator?.pop()
    }
  }
}