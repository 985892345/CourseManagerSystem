package com.course.components.view.calendar.month

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Date
import com.course.components.view.calendar.CalendarDateShowValue
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState
import kotlin.math.abs

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
  private val showIndexSet: Set<Int>,
  private val itemContent: @Composable (date: Date, showState: CalendarDateShowValue) -> Unit,
) : LazyLayoutItemProvider {

  override val itemCount: Int
    get() {
      val finalDate = endDateState.value.copy(dayOfMonth = 31, noOverflow = true)
        .run { plusDays(6 - dayOfWeekOrdinal) }
      return getIndex(finalDate, false) + 1
    }

  @Composable
  override fun Item(index: Int, key: Any) {
    val dateKey by rememberUpdatedState(key as Int)
    val date by rememberUpdatedState(Date(abs(dateKey)))
    val show by remember {
      derivedStateOfStructure {
        getDateShowState(date, dateKey > 0)
      }
    }
    itemContent(date, show)
  }

  override fun getIndex(key: Any): Int {
    key as Int
    val date = Date(key)
    val isNowMonth = key > 0
    val index = getIndex(date, isNowMonth)
    if (index !in showIndexSet) return -1
    return index
  }

  override fun getKey(index: Int): Any {
    // 虽然我们将月份视图以 6 行来记，但是对于多出来的那一行，并不会回调该方法，所以可以不考虑
    val monthDiff = index / (6 * 7)
    val dayDiff = index % (6 * 7)
    val firstDate = startDateState.value.plusMonths(monthDiff).copy(dayOfMonth = 1)
    val nowDate = firstDate.plusDays(dayDiff - firstDate.dayOfWeekOrdinal)
    return if (nowDate.monthNumber != firstDate.monthNumber) {
      -nowDate.time // nowDate 不在自身表示的月份中显示，返回负的 time 来进行区分
    } else nowDate.time
  }

  private fun getDateShowState(date: Date, isNowMonth: Boolean): CalendarDateShowValue {
    // 处于 MonthOutside 的 item 不会存在点击状态
    // 如果当前 MonthOutside 的 item 在第一行，则点击后自动跳转到上一个月的同日期 item，所以不存在点击状态
    if (!isNowMonth) return CalendarDateShowValue.MonthOutside
    return if (verticalScrollState.value == VerticalScrollState.Collapsed) {
      if (horizontalScrollState.value == HorizontalScrollState.Idle) {
        // 对应 CalendarMonthMeasurePolicy.collapsedHorizontalIdleMeasure
        val showLine =
          clickDateState.value.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
        val dateLine = date.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
        if (date.dayOfWeekOrdinal == clickDateState.value.dayOfWeekOrdinal) {
          if (dateLine in showLine - 1..showLine + 1 || date.monthNumber != clickDateState.value.monthNumber) {
            CalendarDateShowValue.Clicked
          } else {
            CalendarDateShowValue.Normal
          }
        } else {
          CalendarDateShowValue.Normal
        }
      } else {
        // 对应 CalendarMonthMeasurePolicy.collapsedHorizontalScrollingMeasure
        if (date.dayOfWeekOrdinal == clickDateState.value.dayOfWeekOrdinal) {
          CalendarDateShowValue.Clicked
        } else {
          CalendarDateShowValue.Normal
        }
      }
    } else {
      // 对应 CalendarMonthMeasurePolicy.verticalScrollingMeasure 和 expandedMeasure
      if (date.dayOfMonth == clickDateState.value.dayOfMonth) {
        CalendarDateShowValue.Clicked
      } else {
        CalendarDateShowValue.Normal
      }
    }
  }

  /**
   * 用于获取 date 对应的位置
   * ```
   * 以月视图来进行计算，为了更方便的从 index -> date 的反向推算，每个月我们以 6 行来记 (因为有些月会占 6 行)
   *
   * 举个例子: 2023-11
   * 30  31  1   2   3   4   5
   * 6   7   8   9   10  11  12
   * 13  14  15  16  17  18  19
   * 20  21  22  23  24  25  26
   * 27  28  29  30  1   2   3
   *
   * 其中开头的 30 31 是上一个月，但是 30 的索引被计算为 0，31 为 1，后面依次类推
   * 而对于 30 和 31 在只传递日期时我们是无法得知他显示在 10 月还是显示在 11 月，所以需要额外传递 isNowMonth 参数
   * ```
   * @param isNowMonth 当前日期是否显示在当前月，如上图例子中第一行的 30 31 和最后一行的 1 2 3，不在自身表示的月份中显示，则传递 false; 其他情况传递 false 则无效
   */
  fun getIndex(date: Date, isNowMonth: Boolean): Int {
    val startDate = startDateState.value
    val firstDate = date.copy(dayOfMonth = 1)
    val lastDate = date.copy(dayOfMonth = date.lengthOfMonth)
    val isInFirstLine = date in firstDate.minusDays(firstDate.dayOfWeekOrdinal)..firstDate.plusDays(6 - firstDate.dayOfWeekOrdinal)
    val isInLastLine = date in lastDate.minusDays(lastDate.dayOfWeekOrdinal)..lastDate.plusDays(6 - lastDate.dayOfWeekOrdinal)
    // 判断当前周显示的一行中是否存在上一个月或者下一个月的日期
    val weekLineHasOtherMonth =
      (firstDate.dayOfWeekOrdinal != 0 && isInFirstLine) ||
          (lastDate.dayOfWeekOrdinal != 6 && isInLastLine)
    if (!weekLineHasOtherMonth || isNowMonth) {
      return ((date.year - startDate.year) * 12 + date.monthNumber - startDate.monthNumber) * 6 * 7 +
          date.copy(dayOfMonth = 1).dayOfWeekOrdinal + date.dayOfMonth - 1
    } else {
      // 当前周行存在另一个月的日期并且 isNowMonth = false，即不在自身表示的月份中显示
      if (date.dayOfMonth > 15) {
        // 月视图的第一行
        val nextDate = date.plusMonths(1).copy(dayOfMonth = 1)
        val index = ((nextDate.year - startDate.year) * 12 + nextDate.monthNumber - startDate.monthNumber) * 6 * 7 +
            nextDate.dayOfWeekOrdinal + nextDate.dayOfMonth - 1
        return index - (nextDate.dayOfWeekOrdinal - date.dayOfWeekOrdinal)
      } else {
        // 月视图的最后一行
        val prevDate = date.minusMonths(1).run { copy(dayOfMonth = lengthOfMonth) }
        val index = ((prevDate.year - startDate.year) * 12 + prevDate.monthNumber - startDate.monthNumber) * 6 * 7 +
            prevDate.copy(dayOfMonth = 1).dayOfWeekOrdinal + prevDate.dayOfMonth - 1
        return index + date.dayOfWeekOrdinal - prevDate.dayOfWeekOrdinal
      }
    }
  }
}