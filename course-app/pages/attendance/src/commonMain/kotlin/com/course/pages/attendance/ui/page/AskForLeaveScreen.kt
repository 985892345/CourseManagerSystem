package com.course.pages.attendance.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.serializable.StringStateSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.time.Num2CN
import com.course.components.view.edit.EditTextCompose
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.source.app.attendance.AttendanceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 23:08
 */
@Serializable
@ObjectSerializable
class AskForLeaveScreen(
  val date: LessonItemData,
) : BaseScreen() {

  @Serializable(StringStateSerializable::class)
  private val editDescription = mutableStateOf("")

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxWidth().systemBarsPadding()) {
      ToolbarCompose()
      LessonInformationCompose()
      DescriptionCompose()
      SubmitBtnCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "请假申请",
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
      Box(
        modifier = Modifier.align(Alignment.CenterEnd)
          .padding(end = 12.dp)
          .size(32.dp)
          .clickableCardIndicator {
            navigator?.push(AskForLeaveHistoryScreen(date))
          },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier,
          imageVector = Icons.Rounded.History,
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
  private fun LessonInformationCompose() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "课程：",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = date.lesson.course,
          fontSize = 18.sp,
        )
      }
      Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "时间：",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = buildAnnotatedString {
            val period = List(date.lesson.length) { Num2CN.transform(date.lesson.beginLesson + it) }
              .joinToString("", postfix = "节")
            append(AnnotatedString(period, SpanStyle(fontSize = 18.sp)))
            val time = "（${date.startTime.time}-${date.startTime.time.plusMinutes(date.minuteDuration)}）"
            append(AnnotatedString(time, SpanStyle(fontSize = 14.sp)))
          },
        )
      }
    }
  }

  @Composable
  private fun DescriptionCompose() {
    Card(
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
      elevation = 0.5.dp,
    ) {
      Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
        EditTextCompose(
          modifier = Modifier.fillMaxWidth().height(200.dp),
          text = editDescription,
          hint = "请输入请假原因",
          isShowIndicatorLine = false,
          textStyle = TextStyle(
            fontSize = 14.sp,
          )
        )
      }
    }
  }

  @Composable
  private fun SubmitBtnCompose() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
      Card(
        modifier = Modifier.align(Alignment.BottomCenter),
        backgroundColor = Color(0xFF4A44E4),
        shape = RoundedCornerShape(16.dp),
      ) {
        val navigator = LocalNavigator.current
        val coroutineScope = rememberCoroutineScope()
        Box(
          modifier = Modifier.clickable {
            if (editDescription.value.isEmpty()) {
              toast("请假原因不能为空")
            } else {
              coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                  Source.api(AttendanceApi::class)
                    .postAskForLeave(
                      courseNum = date.lesson.courseNum,
                      week = date.week,
                      dayOfWeek = date.startTime.date.dayOfWeek,
                      beginLesson = date.lesson.beginLesson,
                      description = editDescription.value,
                    ).getOrThrow()
                }.tryThrowCancellationException().onSuccess {
                  toast("提交成功")
                  navigator?.pop()
                }.onFailure {
                  toast("提交失败")
                }
              }
            }
          }.padding(horizontal = 16.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "提交", color = Color.White)
        }
      }
    }
  }
}