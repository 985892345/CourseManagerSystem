package com.course.pages.attendance.ui.page

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.course.components.utils.source.onFailure
import com.course.components.utils.source.onSuccess
import com.course.components.view.edit.EditTextCompose
import com.course.components.view.option.OptionSelectBackground
import com.course.components.view.option.OptionSelectCompose
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.utils.Num2CN
import com.course.source.app.attendance.AttendanceApi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 19:26
 */
@Serializable
@ObjectSerializable
class AttendancePostScreen(
  val data: LessonItemData
) : BaseScreen() {

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxWidth().systemBarsPadding()) {
      ToolbarCompose()
      LessonInformationCompose()
      SubmitBtnCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "发布考勤",
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

  @Serializable(StringStateSerializable::class)
  private val code = mutableStateOf("")

  @Transient
  private val timeLines = List(200) { (it + 1).toString() }.toImmutableList()
  @Transient
  private val unitLines = persistentListOf("秒", "分")

  @Transient
  private val validTimeLine = Animatable(initialValue = 59F)
  @Transient
  private val validUnitLine = Animatable(initialValue = 0F)

  @Transient
  private val lateTimeLine = Animatable(initialValue = 0F)
  @Transient
  private val lateUnitLine = Animatable(initialValue = 0F)



  @Composable
  private fun LessonInformationCompose() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "课程：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = data.lesson.courseName,
          fontSize = 16.sp,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "时间：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = buildAnnotatedString {
            val period = List(data.period.length) { Num2CN.transform(data.period.beginLesson + it) }
              .joinToString("", postfix = "节")
            append(AnnotatedString(period, SpanStyle(fontSize = 16.sp)))
            val time =
              "（${data.startTime.time}-${data.startTime.time.plusMinutes(data.minuteDuration)}）"
            append(AnnotatedString(time, SpanStyle(fontSize = 13.sp)))
          },
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "考勤码：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
        EditTextCompose(
          text = code,
          modifier = Modifier.weight(1F),
          hint = "请输入考勤码",
          textStyle = TextStyle(
            fontSize = 16.sp,
          )
        )
        Text(
          modifier = Modifier.clickable {
            code.value = Random.nextInt(1000, 9999).toString()
          },
          text = "随机生成",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "有效时长：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        OptionSelectBackground(
          modifier = Modifier.size(100.dp, 80.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            OptionSelectCompose(
              modifier = Modifier.weight(1F),
              selectedLine = validTimeLine,
              options = timeLines,
            )
            OptionSelectCompose(
              modifier = Modifier.weight(1F),
              selectedLine = validUnitLine,
              options = unitLines,
            )
          }
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "迟到时长：",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        OptionSelectBackground(
          modifier = Modifier.size(100.dp, 80.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            OptionSelectCompose(
              modifier = Modifier.weight(1F),
              selectedLine = lateTimeLine,
              options = timeLines,
            )
            OptionSelectCompose(
              modifier = Modifier.weight(1F),
              selectedLine = lateUnitLine,
              options = unitLines,
            )
          }
        }
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
        val coroutineScope = rememberCoroutineScope()
        Box(
          modifier = Modifier.clickable {
            if (code.value.isEmpty()) {
              toast("考勤码为空")
            } else if (code.value.length >= 10) {
              toast("考勤码过长")
            } else {
              submitAttendanceCode(coroutineScope)
            }
          }.padding(horizontal = 16.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "提交", color = Color.White)
        }
      }
    }
  }

  private fun submitAttendanceCode(coroutineScope: CoroutineScope) {
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(AttendanceApi::class)
          .publishAttendance(
            classPlanId = data.periodDate.classPlanId,
            code = code.value,
            duration = getDuration(validTimeLine, validUnitLine),
            lateDuration = getDuration(lateTimeLine, lateUnitLine),
          )
      }.tryThrowCancellationException().onSuccess { wrapper ->
        wrapper.onSuccess {
          toast("发布成功")
        }.onFailure {
          toast(it.info)
        }
      }.onFailure {
        toast("网络异常")
      }
    }
  }

  private fun getDuration(timeLine: Animatable<Float, *>, unitLine: Animatable<Float, *>): Int {
    val time = timeLine.value.toInt() + 1
    return when (unitLine.value.toInt()) {
      0 -> time
      1 -> time * 60
      else -> error("")
    }
  }
}