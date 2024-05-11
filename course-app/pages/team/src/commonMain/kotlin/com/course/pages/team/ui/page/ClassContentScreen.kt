package com.course.pages.team.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.pages.team.ui.course.ClassCourseBottomSheet
import com.course.pages.team.utils.ClassMemberStateSerializer
import com.course.source.app.account.AccountType
import com.course.source.app.team.ClassApi
import com.course.source.app.team.ClassMember
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:40
 */
@Serializable
@ObjectSerializable
class ClassContentScreen(
  val data: LessonItemData,
) : BaseScreen() {

  @Serializable(ClassMemberStateSerializer::class)
  private val members = mutableStateOf(emptyList<ClassMember>())

  @Composable
  override fun ScreenContent() {
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      Column(modifier = Modifier) {
        ToolbarCompose()
        MemberListCompose()
      }
      FloatingActionButtonCompose()
    }
    requestMembers()
  }

  @Composable
  private fun requestMembers() {
    LaunchedEffect(Unit) {
      runCatching {
        Source.api(ClassApi::class)
          .getClassMembers(data.lesson.courseNum)
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        members.value = it
      }
    }
  }

  @Composable
  private fun ToolbarCompose() {
    val navigator = LocalNavigator.current
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = data.lesson.course,
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
      val teachers = mutableListOf<ClassMember>()
      val students = mutableListOf<ClassMember>()
      members.value.fastForEach {
        when (it.type) {
          AccountType.Student -> students.add(it)
          AccountType.Teacher -> teachers.add(it)
        }
      }
      if (teachers.isNotEmpty()) {
        stickyHeader(key = "teacher header", contentType = "header") {
          ListHeaderCompose("教师")
        }
        items(teachers, key = { it.num }, contentType = { "content" }) {
          ListContentCompose(it)
        }
      }
      if (students.isNotEmpty()) {
        stickyHeader(key = "manager header", contentType = "header") {
          ListHeaderCompose("学生")
        }
        items(students, key = { it.num }, contentType = { "content" }) {
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
  private fun LazyItemScope.ListContentCompose(member: ClassMember) {
    Card(
      modifier = Modifier.animateItemPlacement()
        .padding(end = 1.dp), // end padding 为解决 stickyHeader 右边界线问题
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
          Text(
            text = member.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LocalAppColors.current.tvLv2,
          )
          Text(
            text = member.num,
            fontSize = 12.sp,
            color = Color(0xFF666666),
          )
        }
      }
    }
  }

  @Composable
  private fun BoxScope.FloatingActionButtonCompose() {
    FloatingActionButton(
      modifier = Modifier.align(Alignment.BottomEnd)
        .padding(end = 40.dp, bottom = 60.dp),
      onClick = {
        ClassCourseBottomSheet(data, members.value).showCourseBottomSheet()
      },
    ) {
      Image(
        modifier = Modifier,
        imageVector = Icons.Rounded.CalendarMonth,
        contentDescription = null
      )
    }
  }
}