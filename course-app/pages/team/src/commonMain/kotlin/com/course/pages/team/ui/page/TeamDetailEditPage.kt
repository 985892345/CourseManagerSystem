package com.course.pages.team.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.account.Account
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.StringStateSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.view.edit.EditTextCompose
import com.course.components.view.text.drawRequiredPoint
import com.course.source.app.account.AccountType
import com.course.source.app.team.SearchMember
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamMember
import com.course.source.app.team.TeamRole
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 22:03
 */
@Serializable
class TeamDetailEditPage(
  val initialName: String,
  val initialIdentity: String,
  val initialDescription: String,
  val initialManagerList: List<MemberData>,
  val initialMemberList: List<MemberData>,
) {

  @Composable
  fun Content(screen: BaseScreen, submit: () -> Unit) {
    Layout(modifier = Modifier, content = {
      EditContentCompose(screen)
      SubmitBtnCompose(submit)
    }, measurePolicy = { measurables, constraints ->
      val submitPlaceable = measurables[1].measure(
        Constraints(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight,
        )
      )
      val editPlaceable = measurables[0].measure(
        Constraints(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight - submitPlaceable.height,
        )
      )
      layout(constraints.maxWidth, constraints.maxHeight) {
        editPlaceable.place(0, 0)
        submitPlaceable.place(0, editPlaceable.height)
      }
    })
  }

  fun hasChange(): Boolean {
    return editName.value != initialName ||
        editIdentity.value != initialIdentity ||
        editDescription.value != initialDescription ||
        managerList.value != initialManagerList ||
        memberList.value != initialMemberList
  }

  @Serializable(StringStateSerializable::class)
  val editName = mutableStateOf(initialName)

  @Serializable(StringStateSerializable::class)
  val editIdentity = mutableStateOf(initialIdentity)

  @Serializable(StringStateSerializable::class)
  val editDescription = mutableStateOf(initialDescription)

  @Serializable(MembersDataStateSerializable::class)
  val managerList = mutableStateOf(initialManagerList.toPersistentList())

  @Serializable(MembersDataStateSerializable::class)
  val memberList = mutableStateOf(initialMemberList.toPersistentList())

  @Composable
  private fun EditContentCompose(screen: BaseScreen) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item(key = "DetailEdit", contentType = "DetailEdit") {
        DetailEditCompose()
      }
      memberList(screen)
    }
  }

  @Composable
  private fun DetailEditCompose() {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          modifier = Modifier.drawRequiredPoint(),
          text = "名字：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
        EditTextCompose(
          modifier = Modifier.fillMaxWidth(),
          text = editName,
          hint = "请输入团队名",
          singleLine = true,
          textStyle = TextStyle(
            fontSize = 16.sp,
          )
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "职位：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
        EditTextCompose(
          modifier = Modifier.fillMaxWidth(),
          text = editIdentity,
          hint = "队长的职位",
          singleLine = true,
          textStyle = TextStyle(
            fontSize = 16.sp,
          )
        )
      }
      Card(
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        elevation = 0.5.dp,
      ) {
        EditTextCompose(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth().height(100.dp),
          text = editDescription,
          hint = "请输入团队简介",
          isShowIndicatorLine = false,
          textStyle = TextStyle(
            fontSize = 14.sp,
          )
        )
      }
    }
  }

  private fun LazyListScope.memberList(screen: BaseScreen) {
    item(key = "manager header", contentType = "header") {
      MemberListHeaderCompose(screen = screen, text = "管理", isManager = true)
    }
    items(managerList.value, key = { it.num }, contentType = { "content" }) {
      MemberListContentCompose(it, true)
    }
    item(key = "member header", contentType = "header") {
      MemberListHeaderCompose(screen = screen, text = "成员", isManager = false)
    }
    items(memberList.value, key = { it.num }, contentType = { "content" }) {
      MemberListContentCompose(it, false)
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.MemberListHeaderCompose(
    screen: BaseScreen,
    text: String,
    isManager: Boolean,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        modifier = Modifier.animateItemPlacement(),
        text = text,
        fontSize = 14.sp,
      )
      Icon(
        modifier = Modifier.padding(start = 8.dp).size(20.dp).clickableCardIndicator {
          screen.showSearchWindow {
            val member = MemberData(
              name = it.name,
              userId = it.userId,
              num = it.num,
              type = it.type,
            )
            if (isManager) {
              managerList.value = managerList.value.add(0, member)
              memberList.value = memberList.value.remove(member)
            } else {
              memberList.value = memberList.value.add(0, member)
              managerList.value = managerList.value.remove(member)
            }
          }
        },
        imageVector = Icons.Rounded.Add,
        contentDescription = null,
      )
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.MemberListContentCompose(
    member: MemberData,
    isManager: Boolean
  ) {
    Card(
      modifier = Modifier.animateItemPlacement(), elevation = 0.5.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
      ) {
        Box(
          modifier = Modifier.size(24.dp).align(Alignment.CenterVertically).clickableCardIndicator {
            if (isManager) {
              managerList.value = managerList.value.remove(member)
            } else {
              memberList.value = memberList.value.remove(member)
            }
          },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            tint = LocalAppColors.current.red,
          )
        }
        Icon(
          modifier = Modifier.padding(start = 4.dp).size(32.dp).align(Alignment.CenterVertically),
          imageVector = Icons.Outlined.AccountCircle,
          contentDescription = null,
        )
        Column(modifier = Modifier.padding(start = 2.dp, top = 2.dp).weight(1F)) {
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
        if (isManager) {
          EditTextCompose(
            modifier = Modifier.size(100.dp, 18.dp).padding(start = 8.dp, end = 16.dp)
              .align(Alignment.CenterVertically),
            text = member.identity,
            hint = "职位",
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
            ),
          )
        }
      }
    }
  }

  private fun BaseScreen.showSearchWindow(addMember: (SearchMember) -> Unit) {
    showWindow { dismiss ->
      Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6F))
          .systemBarsPadding().clickableNoIndicator {
            dismiss()
          },
      ) {
        Card(
          modifier = Modifier.fillMaxWidth()
            .padding(top = 15.dp)
            .align(Alignment.BottomCenter)
            .imePadding(),
          shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
          Layout(modifier = Modifier.padding(top = 12.dp), content = {
            val searchResult = remember { mutableStateOf(emptyList<SearchMember>()) }
            SearchResultCompose(searchResult, dismiss, addMember)
            SearchEditCompose(searchResult)
          }, measurePolicy = { measurables, constraints ->
            val editPlaceable = measurables[1].measure(
              Constraints(
                maxWidth = constraints.maxWidth,
                maxHeight = constraints.maxHeight,
              )
            )
            val resultPlaceable = measurables[0].measure(
              Constraints(
                maxWidth = constraints.maxWidth,
                maxHeight = constraints.maxHeight - editPlaceable.height,
              )
            )
            val height = editPlaceable.height + resultPlaceable.height
            layout(constraints.maxWidth, height) {
              resultPlaceable.place(0, 0)
              editPlaceable.place(0, resultPlaceable.height)
            }
          })
        }
      }
    }
  }

  @Composable
  private fun SearchResultCompose(
    searchResult: State<List<SearchMember>>,
    dismiss: () -> Unit,
    addMember: (SearchMember) -> Unit,
  ) {
    AnimatedContent(
      targetState = searchResult.value,
    ) {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(it, key = { it.num }) { member ->
          Row(modifier = Modifier.fillMaxWidth().clickable {
            addMember.invoke(member)
            dismiss.invoke()
          }.padding(start = 12.dp, end = 16.dp, top = 8.dp)) {
            Icon(
              modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
              imageVector = Icons.Outlined.AccountCircle,
              contentDescription = null,
            )
            Column(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
              Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                  modifier = Modifier.align(Alignment.CenterStart),
                  text = member.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = LocalAppColors.current.tvLv2,
                )
                Text(
                  modifier = Modifier.align(Alignment.CenterEnd),
                  text = member.major,
                  fontSize = 12.sp,
                  color = Color(0xFF666666),
                )
              }
              Text(
                modifier = Modifier,
                text = member.num,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = Color(0xFF666666),
              )
              Spacer(
                modifier = Modifier.background(Color(0xDDDEDEDE)).fillMaxWidth().height(1.dp)
              )
            }
          }
        }
      }
    }
  }

  @Composable
  private fun SearchEditCompose(searchResult: MutableState<List<SearchMember>>) {
    val searchChanel = remember {
      Channel<String>(
        capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
      )
    }
    val editSearch = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    EditTextCompose(
      modifier = Modifier.fillMaxWidth()
        .focusRequester(focusRequester)
        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 16.dp)
        .height(24.dp),
      text = editSearch,
      hint = "输入学号或者姓名",
      singleLine = true,
      textStyle = TextStyle(textAlign = TextAlign.Center),
      onValueChange = {
        if (it.isNotBlank()) {
          searchChanel.trySend(it)
        } else {
          searchResult.value = emptyList()
        }
        editSearch.value = it
      }
    )
    LaunchedEffect(Unit) {
      while (true) {
        delay(500)
        val receive = searchChanel.receive()
        if (receive.length > 1) {
          runCatching {
            Source.api(TeamApi::class).searchMember(receive).getOrThrow()
          }.tryThrowCancellationException().onSuccess { members ->
            searchResult.value = members.filter { it.num != Account.value?.num }
          }
        } else {
          searchResult.value = emptyList()
        }
      }
    }
  }

  @Composable
  private fun SubmitBtnCompose(submit: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 32.dp)) {
      Card(
        modifier = Modifier.align(Alignment.BottomCenter),
        backgroundColor = Color(0xFF4A44E4),
        shape = RoundedCornerShape(16.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            if (editName.value.isEmpty()) {
              toast("团队名不能为空")
            } else if (editDescription.value.isEmpty()) {
              toast("团队简介不能为空")
            } else if (managerList.value.isEmpty() && memberList.value.isEmpty()) {
              toast("未添加团队成员")
            } else {
              submit()
            }
          }.padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center
        ) {
          Text(text = "提交", color = Color.White)
        }
      }
    }
  }

  @Serializable
  data class MemberData(
    val name: String,
    val userId: Int,
    val num: String,
    val type: AccountType,
  ) {
    @Serializable(StringStateSerializable::class)
    val identity = mutableStateOf("")

    fun toMember(role: TeamRole): TeamMember {
      return TeamMember(
        userId = userId,
        name = name,
        num = num,
        identity = identity.value,
        type = type,
        role = role,
        isConfirmed = true, // 该字段由 server 控制
      )
    }
  }
}

class MembersDataStateSerializable :
  KSerializer<MutableState<List<TeamDetailEditPage.MemberData>>> {

  private val listSerializable =
    ListSerializer(TeamDetailEditPage.MemberData.serializer())

  override val descriptor: SerialDescriptor
    get() = listSerializable.descriptor

  override fun deserialize(decoder: Decoder): MutableState<List<TeamDetailEditPage.MemberData>> {
    return mutableStateOf(listSerializable.deserialize(decoder))
  }

  override fun serialize(
    encoder: Encoder,
    value: MutableState<List<TeamDetailEditPage.MemberData>>
  ) {
    listSerializable.serialize(encoder, value.value)
  }
}