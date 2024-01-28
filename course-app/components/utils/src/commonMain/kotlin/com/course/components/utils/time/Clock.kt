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

fun LocalDate.diffDays(date: LocalDate): Int {
  return toEpochDays() - date.toEpochDays()
}