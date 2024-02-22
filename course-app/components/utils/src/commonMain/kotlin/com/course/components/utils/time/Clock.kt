package com.course.components.utils.time

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/25 20:08
 */

val Today by mutableStateOf(
  Clock.System.todayIn(TimeZone.currentSystemDefault())
).apply {
  @OptIn(DelicateCoroutinesApi::class)
  GlobalScope.launch(Dispatchers.Main) {
    while (true) {
      val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .toMillisecondOfDay()
      delay(24 * 60 * 60 * 1_000L - now)
      value = value.plus(1, DateTimeUnit.DAY)
    }
  }
}

fun LocalDate.copy(
  year: Int = this.year,
  monthNumber: Int = this.monthNumber,
  dayOfMonth: Int = this.dayOfMonth,
  noOverflow: Boolean = false, // 防止溢出，如果日溢出则改为该月最后一天，如果月溢出则年 + 1
) = LocalDate(
  year + if(noOverflow) (monthNumber - 1) / 12 else 0,
  if(noOverflow) (monthNumber - 1) % 12 + 1 else monthNumber,
  if (!noOverflow) dayOfMonth else {
    minOf(
      dayOfMonth, when ((monthNumber - 1) % 12 + 1) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
      }
    )
  }
)

val LocalDate.dayOfWeekNum: Int
  get() = dayOfWeek.ordinal + 1

fun isLeapYear(year: Int): Boolean {
  return year and 3 == 0 && (year % 100 != 0 || year % 400 == 0)
}