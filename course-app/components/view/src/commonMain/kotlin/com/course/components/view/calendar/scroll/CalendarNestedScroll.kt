package com.course.components.view.calendar.scroll

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.course.components.view.calendar.state.CalendarSheetValue
import com.course.components.view.calendar.state.CalendarState

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 19:27
 */
class CalendarNestedScroll(
  val state: CalendarState,
) : NestedScrollConnection {

  private var childScrollOffset = 0F

  override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
    if (childScrollOffset == 0F || state.isScrolling) {
      return Offset(x = 0F, y = scrollBy(available.y))
    }
    return Offset.Zero
  }

  override fun onPostScroll(
    consumed: Offset,
    available: Offset,
    source: NestedScrollSource
  ): Offset {
    if (available.y > 0F && consumed.y == 0F) {
      childScrollOffset = 0F // 此时一定滚动到顶部
    } else {
      childScrollOffset += consumed.y
    }
    return super.onPostScroll(consumed, available, source)
  }

  override suspend fun onPreFling(available: Velocity): Velocity {
    if (state.isScrolling && (available.y != 0F || available == Velocity.Zero)) {
      val target = if (available.y > 1000) state.maxScrollOffset
      else if (available.y < -1000) 0F
      else if (state.fraction > 0.5F) state.maxScrollOffset
      else 0F
      animate(
        initialValue = state.scrollOffset,
        targetValue = target,
        initialVelocity = available.y,
      ) { value, _ ->
        scrollBy(value - state.scrollOffset)
      }
      return available
    }
    return super.onPreFling(available)
  }

  @OptIn(ExperimentalFoundationApi::class)
  private fun scrollBy(y: Float): Float {
    if (y == 0F) return 0F
    if (state.pagerState.isScrollInProgress) return 0F
    val scrollOffset = state.scrollOffset
    val maxScrollOffset = state.maxScrollOffset
    if (y > 0) {
      // 向下滑动
      if (scrollOffset == maxScrollOffset) return 0F
      if (scrollOffset + y >= maxScrollOffset) {
        val result = maxScrollOffset - scrollOffset
        state.scrollOffset = maxScrollOffset
        updateSheetValue(CalendarSheetValue.Expanded)
        return result
      }
    } else {
      if (scrollOffset == 0F) return 0F
      if (scrollOffset + y <= 0) {
        val result = -scrollOffset
        state.scrollOffset = 0F
        updateSheetValue(CalendarSheetValue.Collapsed)
        return result
      }
    }
    state.scrollOffset += y
    updateSheetValue(null)
    return y
  }

  private fun updateSheetValue(newValue: CalendarSheetValue?) {
    val oldValue = state.currentSheetValue
    state.currentSheetValue = newValue
    if (newValue != null) {
      state.lastSheetValue = newValue
    }
  }
}