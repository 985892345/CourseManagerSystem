package com.course.components.view.calendar.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Date
import com.course.components.utils.time.Today
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/18 18:43
 */
@Stable
class CalendarState(
  internal val coroutineScope: CoroutineScope,
  val startDateState: State<Date>,
  val endDateState: State<Date>,
  var onClick: CalendarState.(Date) -> Unit
) {

  val showBeginDate: Date
    get() = if (currentIsCollapsed) {
      startDateState.value.run { minusDays(dayOfWeekOrdinal) }
    } else {
      startDateState.value.copy(dayOfMonth = 1).run { minusDays(dayOfWeekOrdinal) }
    }

  val showFinalDate: Date
    get() = if (currentIsCollapsed) {
      endDateState.value.run { plusDays(6 - dayOfWeekOrdinal) }
    } else {
      endDateState.value.run { copy(dayOfMonth = lengthOfMonth).plusDays(6 - dayOfWeekOrdinal) }
    }

  // 用于在拖动期间修改 clickDate 时临时保存，拖动后才设置
  internal var tempClickDate: Date? = null

  internal val clickDateState = mutableStateOf(
    Today.coerceIn(startDateState.value, endDateState.value)
  ).apply {
    snapshotFlow { Today }
      .onEach { if (value.plusDays(1) == it) updateClickDate(it) }
      .launchIn(coroutineScope)
  }

  val clickDate by derivedStateOfStructure {
    if (layoutWidth == 0) {
      clickDateState.value
    } else {
      val diffPage = (horizontalScrollState.value.offset / layoutWidth).roundToInt()
      if (currentIsCollapsed) {
        clickDateState.value.minusWeeks(diffPage)
      } else {
        clickDateState.value.minusMonths(diffPage)
      }.coerceIn(startDateState.value, endDateState.value)
    }
  }


  /**
   * 更新选中的日期，如果不在当前页则会自动跳转
   */
  fun updateClickDate(date: Date) {
    if (date in startDateState.value..endDateState.value) {
      if (verticalScrollState.value is VerticalScrollState.Scrolling ||
        horizontalScrollState.value is HorizontalScrollState.Scrolling
      ) {
        tempClickDate = date
      } else {
        clickDateState.value = date
        tempClickDate = null
      }
    }
  }

  val pageCount: Int
    get() = getPage(endDateState.value) + 1

  /**
   * 得到当前 date 对应的页数，注意在折叠和展开时页数不一致，如果超出范围则返回 -1
   */
  fun getPage(date: Date): Int {
    val start = startDateState.value
    val end = endDateState.value
    if (date !in start..end) return -1
    return if (currentIsCollapsed) {
      showBeginDate.daysUntil(date) / 7
    } else {
      (date.year - start.year) * 12 + date.monthNumber - start.monthNumber
    }
  }

  val currentIsExpanded: Boolean by derivedStateOfStructure {
    verticalScrollState.value.offset == maxVerticalScrollOffset
  }

  val currentIsCollapsed: Boolean by derivedStateOfStructure {
    verticalScrollState.value.offset == 0F
  }

  val verticalIsScrolling: Boolean by derivedStateOfStructure {
    verticalScrollState.value is VerticalScrollState.Scrolling
  }

  val horizontalIsScrolling: Boolean by derivedStateOfStructure {
    horizontalScrollState.value is HorizontalScrollState.Scrolling
  }

  // 手势滚动中的上下滑状态，在滚动未结束时，即使当前显示状态是折叠/展开的，仍处于 Scrolling 状态
  val verticalScrollState: MutableState<VerticalScrollState> =
    mutableStateOf(VerticalScrollState.Collapsed)

  val horizontalScrollState: MutableState<HorizontalScrollState> =
    mutableStateOf(HorizontalScrollState.Idle)

  internal var layoutWidth by mutableIntStateOf(0)

  val lineHeightState = mutableFloatStateOf(0F)

  val maxVerticalScrollOffset: Float
    get() = lineHeightState.value * 5

  /**
   * 当前滑动的值
   */
  val verticalScrollOffset: Float by derivedStateOfStructure {
    verticalScrollState.value.offset
  }

  /**
   * 当前滑动的比例，[0.0, 1.0]
   */
  val fraction: Float by derivedStateOfStructure {
    verticalScrollState.value.offset / maxVerticalScrollOffset
  }
}

@Composable
fun rememberCalendarState(
  startDate: Date = Date(1901, 1, 1),
  endDate: Date = Date(2099, 12, 31),
  onClick: CalendarState.(Date) -> Unit = {
    if (it in startDate..endDate) {
      updateClickDate(it)
    }
  },
): CalendarState {
  val coroutineScope = rememberCoroutineScope()
  val startDateState = rememberUpdatedState(startDate)
  val endDateState = rememberUpdatedState(endDate)
  return remember {
    CalendarState(
      coroutineScope = coroutineScope,
      startDateState = startDateState,
      endDateState = endDateState,
      onClick = onClick,
    )
  }.apply { this.onClick = onClick }
}
