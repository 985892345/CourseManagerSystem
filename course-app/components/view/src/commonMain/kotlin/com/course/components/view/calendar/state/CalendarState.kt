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
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Date
import com.course.components.utils.time.Today
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState
import kotlinx.coroutines.CoroutineScope
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
  val onClick: CalendarState.(Date) -> Unit
) {

  internal var clickDateState = mutableStateOf(Today)

  val clickDate by derivedStateOfStructure {
    if (layoutWidth == 0) {
      clickDateState.value
    } else {
      val diff = (horizontalScrollState.value.offset / layoutWidth).roundToInt()
      if (currentIsCollapsed) {
        clickDateState.value.minusWeeks(diff)
      } else {
        clickDateState.value.minusMonths(diff)
      }
    }
  }

  /**
   * 更新选中的日期，如果不在当前页则会自动跳转
   */
  fun updateClickDate(date: Date) {
    if (date in startDateState.value..endDateState.value) {
      clickDateState.value = date
    }
  }

  val currentIsExpanded: Boolean by derivedStateOfStructure {
    verticalScrollState.value.offset == maxVerticalScrollOffset
  }

  val currentIsCollapsed: Boolean by derivedStateOfStructure {
    verticalScrollState.value.offset == 0F
  }

  val isScrolling: Boolean by derivedStateOfStructure {
    verticalScrollState.value is VerticalScrollState.Scrolling
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
    } else toast("选择的日期不能超过 $startDate-$endDate")
  },
): CalendarState {
  val startDateState = rememberUpdatedState(startDate)
  val endDateState = rememberUpdatedState(endDate)
  val coroutineScope = rememberCoroutineScope()
  return remember {
    CalendarState(
      coroutineScope = coroutineScope,
      startDateState = startDateState,
      endDateState = endDateState,
      onClick = onClick,
    )
  }
}
