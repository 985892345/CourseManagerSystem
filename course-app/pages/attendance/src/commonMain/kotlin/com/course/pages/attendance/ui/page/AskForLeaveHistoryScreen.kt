package com.course.pages.attendance.ui.page

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.onSuccess
import com.course.components.utils.time.Num2CN
import com.course.components.utils.time.toChinese
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.source.app.attendance.AskForLeaveHistory
import com.course.source.app.attendance.AskForLeaveStatus
import com.course.source.app.attendance.AttendanceApi
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/7 11:06
 */
@Serializable
@ObjectSerializable
class AskForLeaveHistoryScreen(
  val date: LessonItemData,
) : BaseScreen() {

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      LessonNameCompose()
      ListCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "请假历史",
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
  private fun LessonNameCompose() {
    Text(
      modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
      text = date.lesson.course,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
    )
  }

  @Composable
  private fun ListCompose() {
    var history by remember { mutableStateOf<List<AskForLeaveHistory>>(emptyList()) }
    LaunchedEffect(Unit) {
      runCatching {
        Source.api(AttendanceApi::class)
          .getAskForLeaveHistory(date.lesson.courseNum)
      }.tryThrowCancellationException().onSuccess { wrapper ->
        wrapper.onSuccess {
          history = it
        }
      }
    }
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      history.fastForEach {
        Card(
          modifier = Modifier,
          elevation = 0.5.dp
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
          ) {
            Column(modifier = Modifier.weight(1F)) {
              Text(
                text = getTitle(it),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.padding(top = 4.dp),
                text = it.description,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = Color(0xFF666666),
              )
            }
            Text(
              modifier = Modifier.width(80.dp).padding(end = 16.dp).align(Alignment.CenterVertically),
              text = when (it.status) {
                AskForLeaveStatus.Pending -> ""
                AskForLeaveStatus.Approved -> "已批准"
                AskForLeaveStatus.Rejected -> "被驳回"
              },
              color = when (it.status) {
                AskForLeaveStatus.Pending -> Color.Unspecified
                AskForLeaveStatus.Approved -> LocalAppColors.current.green
                AskForLeaveStatus.Rejected -> LocalAppColors.current.red
              },
              fontSize = 15.sp,
              textAlign = TextAlign.End,
            )
          }
        }
      }
    }
  }

  private fun getTitle(history: AskForLeaveHistory): String {
    val week = "第${Num2CN.transform(history.week)}周"
    val dayOfWeek = history.dayOfWeek.toChinese()
    val period = List(history.length) { Num2CN.transform(history.beginLesson + it) }
      .joinToString("", postfix = "节")
    return "$week  $dayOfWeek  $period"
  }
}