package com.course.pages.course.ui.content.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.utils.time.Today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:39
 */

@Composable
fun CoursePagerCombine.CourseWeekdayCompose(
  modifier: Modifier = Modifier
) {
  Row(
    modifier = Modifier.fillMaxWidth().height(52.dp).then(modifier)
  ) {
    Box(modifier = Modifier.weight(0.8F)) {
      if (monDate != null) {
        MonthCompose(month = monDate.month)
      }
    }
    var date = monDate ?: Today.minus(Today.dayOfWeek.ordinal, DateTimeUnit.DayBased(1))
    repeat(7) {
      Box(modifier = Modifier.weight(1F)) {
        WeekCompose(
          dayOfWeek = date.dayOfWeek,
          dayOfMonth = if (monDate == null) null else date.dayOfMonth,
          isToday = Today == date
        )
        date = date.plus(1, DateTimeUnit.DayBased(1))
      }
    }
  }
}

@Composable
private fun MonthCompose(month: Month) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "${month.number}月", textAlign = TextAlign.Center, fontSize = 16.sp)
  }
}

@Composable
private fun WeekCompose(dayOfWeek: DayOfWeek, dayOfMonth: Int?, isToday: Boolean) {
  val textColor = if (isToday) Color.White else Color.Black
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(
        color = if (isToday) Color(0x93E8F0FC) else Color.Unspecified,
        shape = if (isToday) RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp) else RectangleShape
      )
      .background(
        color = if (isToday) Color(0xFF2A4E84) else Color.Unspecified,
        shape = if (isToday) RoundedCornerShape(8.dp) else RectangleShape
      ),
    verticalArrangement = Arrangement.SpaceEvenly
  ) {
    Text(
      text = dayOfWeek.str,
      textAlign = TextAlign.Center,
      fontSize = 12.sp,
      color = textColor,
      modifier = Modifier.fillMaxWidth(),
    )
    Text(
      text = if (dayOfMonth != null) "${dayOfMonth}日" else "",
      textAlign = TextAlign.Center,
      fontSize = 12.sp,
      color = textColor,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

private val DayOfWeek.str: String
  get() = when (this) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周天"
    else -> error("???")
  }