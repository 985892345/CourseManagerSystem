package com.course.pages.exam.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.time.Num2CN
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.exam.ExamBean
import com.course.source.app.exam.ExamTermBean
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 15:23
 */
sealed interface IExamListItem {

  val key: String

  @Composable
  fun LazyItemScope.Content()

  companion object {
    fun transform(termBeans: List<ExamTermBean>): List<IExamListItem> {
      val result = mutableListOf<IExamListItem>()
      termBeans.forEach { term ->
        result.add(ExamTermListHeader(term))
        term.exams.forEach {
          result.add(ExamListItem(term, it))
        }
      }
      return result
    }
  }
}

data class ExamListItem(
  val termBean: ExamTermBean,
  val bean: ExamBean
) : IExamListItem {

  override val key: String
    get() = termBean.term + bean.courseNum

  @Composable
  override fun LazyItemScope.Content() {
    Row(modifier = Modifier.padding(start = 18.dp, end = 12.dp).height(IntrinsicSize.Min)) {
      Spacer(modifier = Modifier.width(20.dp).fillMaxHeight().drawBehind {
        val stroke = 4.dp.toPx()
        drawCircle(
          color = Color(0xFF904EF5),
          radius = size.width / 2 - stroke,
          center = Offset(x = size.width / 2, y = size.width / 2),
          style = Stroke(width = stroke),
        )
        drawLine(
          color = Color(0xFF904EF5),
          start = Offset(x = size.width / 2, y = size.width),
          end = Offset(x = size.width / 2, y = size.height),
          strokeWidth = 3F,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(15F, 15F)),
        )
      })
      Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
            modifier = Modifier.padding(start = 24.dp),
            text = getWeekStr(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.tvLv2,
          )
          Text(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp),
            text = getRemain(),
            fontSize = 13.sp,
            color = Color(0xFF904EF5),
          )
        }
        Card(
          modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
          elevation = 0.dp,
          backgroundColor = Color(0xFFE8F1FC),
          shape = RoundedCornerShape(10.dp),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row {
              Text(
                modifier = Modifier.weight(1F),
                text = bean.course,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = bean.type,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.tvLv2,
              )
            }
            Row(
              modifier = Modifier.padding(top = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val (date, time) = getTime()
              Text(
                modifier = Modifier,
                text = date,
                fontSize = 14.sp,
                color = Color(0xFF666666),
              )
              Spacer(
                modifier = Modifier.align(Alignment.CenterVertically)
                  .padding(horizontal = 6.dp)
                  .size(3.dp, 3.dp)
                  .background(Color(0xFF666666), CircleShape)
              )
              Text(
                modifier = Modifier,
                text = time,
                fontSize = 14.sp,
                color = Color(0xFF666666),
              )
            }
            Row(
              modifier = Modifier.padding(top = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                modifier = Modifier,
                text = bean.classroom,
                fontSize = 14.sp,
                color = Color(0xFF666666),
              )
              Spacer(
                modifier = Modifier.padding(horizontal = 6.dp)
                  .size(3.dp, 3.dp)
                  .background(Color(0xFF666666), CircleShape)
              )
              Text(
                modifier = Modifier,
                text = bean.seat,
                fontSize = 14.sp,
                color = Color(0xFF666666),
              )
            }
          }
        }
      }
    }
  }

  private fun getWeekStr(): String {
    val week = termBean.beginDate.daysUntil(bean.startTime.date) / 7 + 1
    val weekNum = when (bean.startTime.date.dayOfWeek) {
      DayOfWeek.MONDAY -> "一"
      DayOfWeek.TUESDAY -> "二"
      DayOfWeek.WEDNESDAY -> "三"
      DayOfWeek.THURSDAY -> "四"
      DayOfWeek.FRIDAY -> "五"
      DayOfWeek.SATURDAY -> "六"
      DayOfWeek.SUNDAY -> "七"
    }
    return if (week < 10) {
      "第" + Num2CN.transform(week.toLong()) + "周周$weekNum"
    } else {
      Num2CN.transform(week.toLong()) + "周周$weekNum"
    }
  }

  @Composable
  private fun getRemain(): String {
    var nowMinuteTimeDate by remember { mutableStateOf(MinuteTimeDate.now()) }
    val daysUntil = nowMinuteTimeDate.date.daysUntil(bean.startTime.date)
    return when {
      daysUntil == 1 -> "明天考试"
      daysUntil == 2 -> "后天考试"
      daysUntil < 0 -> "考试已结束"
      daysUntil > 0 -> "还剩${daysUntil}天考试"
      else -> {
        val now = nowMinuteTimeDate.time
        val start = bean.startTime.time
        val end = start.plusMinutes(bean.minuteDuration)
        if (now >= end) {
          "考试已结束"
        } else if (now >= start) {
          "考试进行中"
        } else {
          LaunchedEffect(Unit) {
            val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            delay(1.minutes - time.second.seconds)
            nowMinuteTimeDate = nowMinuteTimeDate.plusMinutes(1)
            while (true) {
              delay(1.minutes)
              nowMinuteTimeDate = nowMinuteTimeDate.plusMinutes(1)
            }
          }
          val minutesUntil = now.minutesUntil(start)
          val h = (minutesUntil / 60).let { if (it != 0) "${it}小时" else "" }
          val m = (minutesUntil % 60).let { if (it != 0) "${it}分钟" else "" }
          "还有${h}${m}后考试"
        }
      }
    }
  }

  private fun getTime(): Pair<String, String> {
    val date = "${bean.startTime.date.monthNumber}月${bean.startTime.date.dayOfMonth}号"
    val start = bean.startTime.time
    val end = start.plusMinutes(bean.minuteDuration)
    return date to "${start.hour}:${start.minute}-${end.hour}:${end.minute}"
  }
}

data class ExamTermListHeader(
  val termBean: ExamTermBean
) : IExamListItem {

  override val key: String
    get() = termBean.term

  @Composable
  override fun LazyItemScope.Content() {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background)) {
      Text(
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
        text = termBean.term,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2,
      )
    }
  }
}