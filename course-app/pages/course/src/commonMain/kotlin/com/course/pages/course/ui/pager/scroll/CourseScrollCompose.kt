package com.course.pages.course.ui.pager.scroll

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.pages.course.ui.pager.CoursePagerState

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
    modifier = Modifier.then(modifier).reflexScrollableForMouse().verticalScroll(state = scrollState),
    content = content,
    measurePolicy = remember {
      { measurables, constraints ->
        // 因为被 CourseScrollCompose 包裹，所以传递最小高度给子布局
        var consume = 0
        val placeables = measurables.fastMapIndexed { index, measurable ->
          measurable.measure(
            constraints.copy(
              minWidth = if (index == measurables.lastIndex) constraints.maxWidth - consume else 0,
              maxWidth = constraints.maxWidth - consume,
              maxHeight = constraints.minHeight
            )
          ).apply { consume += width }
        }
        layout(constraints.maxWidth, constraints.minHeight) {
          var start = 0
          placeables.fastForEach {
            it.placeRelative(x = start, y = 0)
            start += it.width
          }
        }
      }
    }
  )
}