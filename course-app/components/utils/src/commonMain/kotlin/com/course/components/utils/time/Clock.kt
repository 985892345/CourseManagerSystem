package com.course.components.utils.time

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.shared.time.Date.Companion.lengthOfMonth
import com.course.shared.time.toDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/25 20:08
 */

val Today by mutableStateOf(
  Clock.System.todayIn(TimeZone.currentSystemDefault()).toDate()
).apply {
  AppCoroutineScope.launch(Dispatchers.Main) {
    while (true) {
      val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .toMillisecondOfDay()
      delay(24 * 60 * 60 * 1_000L - now)
      value = value.plusDays(1)
    }
  }
}

fun LocalDate.copy(
  year: Int = this.year,
  monthNumber: Int = this.monthNumber,
  dayOfMonth: Int = this.dayOfMonth,
  noOverflow: Boolean = false, // 防止溢出，如果日溢出则改为该月最后一天，如果月溢出则年 + 1
) = LocalDate(
  year + if (noOverflow) (monthNumber - 1) / 12 else 0,
  if (noOverflow) (monthNumber - 1) % 12 + 1 else monthNumber,
  if (!noOverflow) dayOfMonth else {
    minOf(
      dayOfMonth,
      lengthOfMonth(year + (monthNumber - 1) / 12, (monthNumber - 1) % 12 + 1)
    )
  }
)

val LocalDate.dayOfWeekNum: Int
  get() = dayOfWeek.ordinal + 1

