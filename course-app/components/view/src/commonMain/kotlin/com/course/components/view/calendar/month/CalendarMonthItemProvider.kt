package com.course.components.view.calendar.month

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Date
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/25 22:12
 */
@OptIn(ExperimentalFoundationApi::class)
internal class CalendarMonthItemProvider(
  private val verticalScrollState: State<VerticalScrollState>,
  private val horizontalScrollState: State<HorizontalScrollState>,
  private val startDateState: State<Date>,
  private val endDateState: State<Date>,
  private val clickDateState: State<Date>,
  private val weekContent: @Composable (begin: Date, end: Date) -> Unit,
  private val showIndexSet: Set<Int>,
) : LazyLayoutItemProvider {

  override val itemCount: Int
    get() = startDateState.value
      .indexUntil(endDateState.value.copy(dayOfMonth = 31, noOverflow = true)) + 1

  @Composable
  override fun Item(index: Int, key: Any) {
    val date by rememberUpdatedState(Date(key as Int))
    val showDate by derivedStateOfStructure {
      if (verticalScrollState.value == VerticalScrollState.Collapsed) {
        val begin = date.minusDays(date.dayOfWeekOrdinal)
        if (horizontalScrollState.value == HorizontalScrollState.Idle) {
          val showLine = clickDateState.value.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
          val dateLine = date.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
          if (dateLine in showLine - 1..showLine + 1) {
            begin.plusDays(clickDateState.value.dayOfWeekOrdinal)
          } else if (date.monthNumber != clickDateState.value.monthNumber) {
            begin.plusDays(clickDateState.value.dayOfWeekOrdinal)
          } else {
            date.copy(dayOfMonth = clickDateState.value.dayOfMonth, noOverflow = true)
          }
        } else {
          begin.plusDays(clickDateState.value.dayOfWeekOrdinal)
        }
      } else {
        date.copy(dayOfMonth = clickDateState.value.dayOfMonth, noOverflow = true)
      }.coerceIn(startDateState.value, endDateState.value)
    }
    weekContent(date.minusDays(date.dayOfWeekOrdinal), showDate)
  }

  override fun getIndex(key: Any): Int {
    if (key == Int.MIN_VALUE) return -1
    val date = Date(key as Int)
    val index = startDateState.value.indexUntil(date)
    if (index !in showIndexSet) return -1
    return index
  }

  override fun getKey(index: Int): Any {
    val monthDiff = index / 6
    return when (val weekDiff = index % 6) {
      0 -> startDateState.value
        .copy(dayOfMonth = 1)
        .plusMonths(monthDiff)

      5 -> startDateState.value
        .copy(dayOfMonth = 1)
        .plusMonths(monthDiff)
        .let {
          // 如果能显示 6 行，则正常返回最后一行的第一天，否则返回 Int.MIN_VALUE
          if (it.monthLineCount() == 6) it.minusDays(it.dayOfWeekOrdinal).plusWeeks(5)
          else return Int.MIN_VALUE
        }

      -1, -2 -> startDateState.value
        .copy(dayOfMonth = 1)
        .run { minusDays(dayOfWeekOrdinal) }

      else -> startDateState.value
        .copy(dayOfMonth = 1)
        .plusMonths(monthDiff)
        .let { it.minusDays(it.dayOfWeekOrdinal).plusWeeks(weekDiff) }
    }.time
  }
}