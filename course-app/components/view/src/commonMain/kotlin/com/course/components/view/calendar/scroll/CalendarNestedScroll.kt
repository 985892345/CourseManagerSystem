package com.course.components.view.calendar.scroll

import androidx.compose.animation.core.animate
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.course.components.view.calendar.state.CalendarState

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 19:27
 */
class CalendarNestedScroll(
  private val state: CalendarState,
) : NestedScrollConnection {

  private var childScrollOffset = 0F

  override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
    if (childScrollOffset == 0F || state.verticalIsScrolling) {
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
    if (state.verticalIsScrolling && (available.y != 0F || available == Velocity.Zero)) {
      val target = if (available.y > 1000) state.maxVerticalScrollOffset
      else if (available.y < -1000) 0F
      else if (state.fraction > 0.5F) state.maxVerticalScrollOffset
      else 0F
      animate(
        initialValue = state.verticalScrollOffset,
        targetValue = target,
        initialVelocity = available.y,
      ) { value, _ ->
        scrollBy(value - state.verticalScrollOffset)
      }
      return available
    }
    return super.onPreFling(available)
  }

  override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
    val vertical = state.verticalScrollState.value
    if (vertical is VerticalScrollState.Scrolling) {
      Snapshot.withMutableSnapshot {
        state.verticalScrollState.value =
          if (vertical.offset == 0F) VerticalScrollState.Collapsed
          else VerticalScrollState.Expanded(state.maxVerticalScrollOffset)
        state.tempClickDate?.let { state.updateClickDate(it) }
      }
    }
    return super.onPostFling(consumed, available)
  }

  private fun scrollBy(y: Float): Float {
    if (y == 0F) return 0F
    // 翻页中不允许上下滑
    if (state.horizontalScrollState.value is HorizontalScrollState.Scrolling) return 0F
    val scrollOffset = state.verticalScrollOffset
    val maxScrollOffset = state.maxVerticalScrollOffset
    if (y > 0) {
      // 向下滑动
      if (scrollOffset == maxScrollOffset) return 0F
      if (scrollOffset + y >= maxScrollOffset) {
        val result = maxScrollOffset - scrollOffset
        state.verticalScrollState.value = VerticalScrollState.Scrolling(maxScrollOffset, 1F)
        return result
      }
    } else {
      if (scrollOffset == 0F) return 0F
      if (scrollOffset + y <= 0) {
        val result = -scrollOffset
        state.verticalScrollState.value = VerticalScrollState.Scrolling(0F, 0F)
        return result
      }
    }
    val newOffset = scrollOffset + y
    state.verticalScrollState.value = VerticalScrollState.Scrolling(newOffset, newOffset / maxScrollOffset)
    return y
  }
}