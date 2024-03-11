package com.course.pages.course.ui.pager.scroll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.util.fastForEach
import com.course.components.utils.time.MinuteTime
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.CoursePagerState
import kotlinx.datetime.LocalTime
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:41
 */
@Composable
fun CoursePagerState.CourseItemGroupCompose(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = Modifier.then(modifier)
  ) {
    // 使用 toList 避免并发修改
    items.toList().fastForEach {
      key(it.itemKey) {
        CourseItemCompose(it)
      }
    }
  }
}

@Composable
private fun CourseItemCompose(item: ICourseItemBean) {
  val heightRatio by animateFloatAsState(item.heightRatio())
  val layoutOffsetRatio by animateOffsetAsState(item.offsetRatio())
  Box(modifier = Modifier.layout { measurable, constraints ->
    val placeable = measurable.measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxWidth / 7,
        minHeight = 0,
        maxHeight = (heightRatio * constraints.maxHeight).roundToInt()
      )
    )
    layout(placeable.width, placeable.height) {
      placeable.placeRelative(
        x = (layoutOffsetRatio.x * placeable.width).roundToInt(),
        y = (layoutOffsetRatio.y * constraints.maxHeight).roundToInt()
      )
    }
  }) {
    item.Content()
  }
}

@Stable
private fun ICourseItemBean.heightRatio(): Float {
  return startTime.plusMinutes(minutePeriod).time.yRatio() - startTime.time.yRatio()
}

@Stable
private fun ICourseItemBean.offsetRatio(): Offset {
  val x = startTime.date.dayOfWeekOrdinal.toFloat()
  val y = startTime.time.yRatio()
  return Offset(x, y)
}

@Stable
private fun MinuteTime.yRatio(): Float {
  TODO()
}

