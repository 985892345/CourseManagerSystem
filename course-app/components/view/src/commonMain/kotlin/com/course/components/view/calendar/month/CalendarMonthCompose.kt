package com.course.components.view.calendar.month

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import com.course.components.utils.time.Date
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState
import com.course.components.view.calendar.state.CalendarState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/24 19:12
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarState.CalendarMonthCompose(
  modifier: Modifier = Modifier,
  weekContent: @Composable (begin: Date, show: Date) -> Unit,
) {
  val prefetchState = remember { LazyLayoutPrefetchState() }
  val showIndexSet = remember { mutableSetOf<Int>() }
  val itemProvider = remember {
    CalendarMonthItemProvider(
      verticalScrollState = verticalScrollState,
      horizontalScrollState = horizontalScrollState,
      startDateState = startDateState,
      endDateState = endDateState,
      clickDateState = clickDateState,
      weekContent = weekContent,
      showIndexSet = showIndexSet,
    )
  }
  LazyLayout(
    modifier = modifier.clipToBounds()
      .dragPager(this)
      .onSizeChanged {
        layoutWidth = it.width
      },
    itemProvider = remember<() -> LazyLayoutItemProvider> { { itemProvider } },
    prefetchState = prefetchState,
    measurePolicy = remember {
      CalendarMonthMeasurePolicy(
        verticalScrollState = verticalScrollState,
        horizontalScrollState = horizontalScrollState,
        lineHeightState = lineHeightState,
        startDate = startDateState,
        endDate = endDateState,
        clickDate = clickDateState,
        showIndexSet = showIndexSet,
      )
    }
  )
}

@Stable
@Composable
private fun Modifier.dragPager(
  calendar: CalendarState
): Modifier {
  val calendarState by rememberUpdatedState(calendar)
  var isInDraggable by remember { mutableStateOf(false) }
  // 当前页面偏移量，在页面拖动越界时与 horizontalScrollState 不一致以实现越界阻尼
  var newOffset by remember { mutableStateOf(0f) }
  val draggableState = rememberDraggableState {
    with(calendarState) {
      if (verticalScrollState.value is VerticalScrollState.Scrolling) return@rememberDraggableState
      val diffPage = (horizontalScrollState.value.offset / layoutWidth).roundToInt()
      val nowDate =
        if (currentIsCollapsed) clickDateState.value.minusWeeks(diffPage)
        else clickDateState.value.minusMonths(diffPage)
      val beginDate = startDateState.value.copy(dayOfMonth = 1)
      val finalDate = endDateState.value.copy(dayOfMonth = 1)
      val diffOffset = newOffset - diffPage * layoutWidth
      if (nowDate.copy(dayOfMonth = 1) == beginDate && diffOffset >= 0
        || nowDate.copy(dayOfMonth = 1) == finalDate && diffOffset <= 0
      ) {
        // 拖动越界
        newOffset += it
        val dx = diffOffset / 2
        horizontalScrollState.value = HorizontalScrollState.Scrolling(diffPage * layoutWidth + dx)
      } else {
        newOffset = horizontalScrollState.value.offset + it
        horizontalScrollState.value = HorizontalScrollState.Scrolling(newOffset)
      }
    }
  }
  val coroutineScope = rememberCoroutineScope()
  var animateJob: Job? by remember { mutableStateOf(null) }
  return draggable(
    state = draggableState,
    orientation = Orientation.Horizontal,
    startDragImmediately = isInDraggable,
    onDragStarted = {
      isInDraggable = true
      newOffset = calendar.horizontalScrollState.value.offset
      animateJob?.cancel()
    },
    onDragStopped = { velocity ->
      isInDraggable = false
      with(calendarState) {
        val sign = horizontalScrollState.value.offset.sign
        val nowOffset = abs(horizontalScrollState.value.offset)
        val mod = nowOffset % layoutWidth
        var targetValue = sign *
            if (sign * velocity > 1000) nowOffset + layoutWidth - mod
            else if (sign * velocity < -1000) nowOffset - mod
            else if (mod < layoutWidth / 2) nowOffset - mod
            else nowOffset + layoutWidth - mod
        // 计算最终页的 date，如果 date 超过范围则进行修正
        val diffPage = (targetValue / layoutWidth).roundToInt()
        var newDate =
          if (currentIsCollapsed) clickDateState.value.minusWeeks(diffPage)
          else clickDateState.value.minusMonths(diffPage)
        val beginDate = startDateState.value.copy(dayOfMonth = 1)
        val finalDate = endDateState.value.copy(dayOfMonth = 31, noOverflow = true)
        if (newDate < beginDate) {
          newDate = if (currentIsCollapsed) newDate.plusWeeks(1) else newDate.plusMonths(1)
          targetValue = (diffPage - 1F) * layoutWidth
        } else if (newDate > finalDate) {
          newDate = if (currentIsCollapsed) newDate.minusWeeks(1) else newDate.minusMonths(1)
          targetValue = (diffPage + 1F) * layoutWidth
        }
        if (horizontalScrollState.value.offset == targetValue) {
          clickDateState.value = newDate
          horizontalScrollState.value = HorizontalScrollState.Idle
        } else {
          // 这里如果使用 onDragStopped 开启协程则会导致后续触摸事件延迟
          coroutineScope.launch {
            animate(
              initialValue = horizontalScrollState.value.offset,
              targetValue = targetValue,
              initialVelocity = velocity,
            ) { value, _ ->
              horizontalScrollState.value = HorizontalScrollState.Scrolling(value)
            }
            clickDateState.value = newDate
            horizontalScrollState.value = HorizontalScrollState.Idle
          }.also { animateJob = it }
        }
      }
    }
  )
}

internal fun Date.monthLineCount(): Int {
  val sum = copy(dayOfMonth = 1).dayOfWeekOrdinal + lengthOfMonth
  return sum / 7 + if (sum % 7 == 0) 0 else 1
}

internal fun Date.indexUntil(date: Date): Int {
  return ((date.year - year) * 12 + date.monthNumber - monthNumber) * 6 +
      (date.copy(dayOfMonth = 1).dayOfWeekOrdinal + date.dayOfMonth - 1) / 7
}

