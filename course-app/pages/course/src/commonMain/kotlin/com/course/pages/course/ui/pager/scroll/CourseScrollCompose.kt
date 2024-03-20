package com.course.pages.course.ui.pager.scroll

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.pages.course.ui.pager.CoursePagerState
import com.course.pages.course.ui.pager.scroll.timeline.MutableTimelineData
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/27 16:25
 */
@Composable
fun CoursePagerState.CourseScrollCompose(
  modifier: Modifier = Modifier.fillMaxSize(),
  content: @Composable () -> Unit = {
    CourseTimelineCompose(modifier = Modifier.width(36.dp))
    CourseItemGroupCompose()
  }
) {
  Layout(
    modifier = modifier
      .reflexScrollableForMouse()
      .verticalScroll(state = scrollState)
      .padding(vertical = 6.dp),
    content = content,
    measurePolicy = remember(this) {
      { measurables, constraints ->
        var widthConsume = 0
        var initialWeight = 0F
        var nowWeight = 0F
        timeline.fastForEach {
          initialWeight += it.initialWeight
          nowWeight += it.nowWeight
        }
        // 因为有 verticalScroll，所以这里 minHeight 就是父布局的高度
        val height = (constraints.minHeight * (nowWeight / initialWeight)).roundToInt()
        val placeables = measurables.fastMapIndexed { index, measurable ->
          measurable.measure(
            constraints.copy(
              minWidth = if (index == measurables.lastIndex) constraints.maxWidth - widthConsume else 0,
              maxWidth = constraints.maxWidth - widthConsume,
              maxHeight = height
            )
          ).apply { widthConsume += width }
        }
        layout(constraints.maxWidth, height) {
          var start = 0
          placeables.fastForEach {
            it.placeRelative(x = start, y = 0)
            start += it.width
          }
        }
      }
    }
  )
  LaunchedEffect(this) {
    val lastTimeline = timeline.last()
    if (lastTimeline is MutableTimelineData) {
      // 最后一个展开时需要向上滚动
      snapshotFlow {
        if (scrollState.value == scrollState.maxValue) {
          lastTimeline.nowWeight
        }
      }.collect {
        scrollState.scrollTo(scrollState.maxValue)
      }
    }
  }
}