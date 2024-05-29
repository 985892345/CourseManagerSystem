package com.course.pages.team.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.BooleanStateSerializable
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:10
 */
@Serializable
@ObjectSerializable
class TeamListScreen(
  val backable: Boolean,
) : BaseScreen() {

  @Composable
  override fun ScreenContent() {
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      Column(modifier = Modifier.fillMaxSize()) {
        ToolbarCompose()
        ListCompose()
      }
      FloatingActionButtonCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "团队管理",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      val navigator = LocalNavigator.current
      if (backable) {
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
  private fun BoxScope.FloatingActionButtonCompose() {
    val navigator = LocalNavigator.current
    FloatingActionButton(
      modifier = Modifier.align(Alignment.BottomEnd)
        .padding(end = 40.dp, bottom = 60.dp),
      onClick = {
        navigator?.push(AddTeamScreen())
      },
    ) {
      Image(
        modifier = Modifier,
        imageVector = Icons.Rounded.Add,
        contentDescription = null
      )
    }
  }

  private var adminTeamList by mutableStateOf(emptyList<TeamBean>())
  private var managerTeamList by mutableStateOf(emptyList<TeamBean>())
  private var memberTeamList by mutableStateOf(emptyList<TeamBean>())

  @Serializable(BooleanStateSerializable::class)
  private val isFoldAdmin = mutableStateOf(false)

  @Serializable(BooleanStateSerializable::class)
  private val isFoldManager = mutableStateOf(false)

  @Serializable(BooleanStateSerializable::class)
  private val isFoldMember = mutableStateOf(false)

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun ListCompose() {
    LazyColumn(
      modifier = Modifier.fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (adminTeamList.isNotEmpty()) {
        stickyHeader(key = "admin header", contentType = "header") {
          ListHeaderCompose("我创建的团队", isFoldAdmin)
        }
        if (!isFoldAdmin.value) {
          items(adminTeamList, key = { it.teamId }, contentType = { "content" }) {
            ListContentCompose(it)
          }
        }
      }
      if (managerTeamList.isNotEmpty()) {
        stickyHeader(key = "manager header", contentType = "header") {
          ListHeaderCompose("我管理的团队", isFoldManager)
        }
        if (!isFoldManager.value) {
          items(managerTeamList, key = { it.teamId }, contentType = { "content" }) {
            ListContentCompose(it)
          }
        }
      }
      if (memberTeamList.isNotEmpty()) {
        stickyHeader(key = "member header", contentType = "header") {
          ListHeaderCompose("我加入的团队", isFoldMember)
        }
        if (!isFoldMember.value) {
          items(memberTeamList, key = { it.teamId }, contentType = { "content" }) {
            ListContentCompose(it)
          }
        }
      }
    }
    LaunchedEffect(Unit) {
      requestTeamList()
    }
  }

  suspend fun requestTeamList() {
    withContext(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .getTeamList()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess { list ->
        val adminList = mutableListOf<TeamBean>()
        val managerList = mutableListOf<TeamBean>()
        val memberList = mutableListOf<TeamBean>()
        list.forEach {
          when (it.role) {
            TeamRole.Administrator -> adminList.add(it)
            TeamRole.Manager -> managerList.add(it)
            TeamRole.Member -> memberList.add(it)
          }
        }
        adminTeamList = adminList
        managerTeamList = managerList
        memberTeamList = memberList
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListHeaderCompose(text: String, isFold: MutableState<Boolean>) {
    Box(
      modifier = Modifier.animateItemPlacement()
        .fillParentMaxWidth()
        .background(MaterialTheme.colors.background)
    ) {
      Row(
        modifier = Modifier.clickableCardIndicator {
          isFold.value = !isFold.value
        }.padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          imageVector = if (isFold.value) Icons.Rounded.ChevronRight else Icons.Rounded.ExpandMore,
          contentDescription = null,
        )
        Text(
          text = text,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(bean: TeamBean) {
    val navigator = LocalNavigator.current
    Card(
      modifier = Modifier.animateItemPlacement().padding(end = 1.dp),
      shape = RoundedCornerShape(8.dp),
      elevation = 0.5.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
          .clickable {
            navigator?.push(TeamContentScreen(bean))
          }.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
      ) {
        Column(modifier = Modifier.weight(1F)) {
          Text(
            text = bean.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LocalAppColors.current.tvLv2,
          )
          Text(
            modifier = Modifier.padding(top = 4.dp),
            text = bean.description,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            color = Color(0xFF666666),
          )
        }
        Text(
          modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            .align(Alignment.CenterVertically),
          text = bean.identity,
          fontSize = 12.sp,
          textAlign = TextAlign.End,
        )
      }
    }
  }
}