package com.course.pages.course.ui.pager.scroll

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.ui.pager.CoursePagerState
import com.course.pages.course.ui.pager.scroll.timeline.CourseTimelineData
import kotlinx.collections.immutable.ImmutableList
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
    modifier = Modifier.fillMaxSize().then(modifier)
  ) {
    items.fastForEach {
      key(it) {
        CourseItemsCompose(it, timeline)
      }
    }
  }
}

@Composable
fun CourseItemsCompose(items: SnapshotStateList<ICourseItem>, timeline: ImmutableList<CourseTimelineData>) {
  // 使用 toList 避免并发修改
  items.toList().fastForEach {
    key(it.itemKey) {
      CourseItemCompose(it, timeline)
    }
  }
}

@Composable
private fun CourseItemCompose(item: ICourseItem, timeline: ImmutableList<CourseTimelineData>) {
  Box(modifier = Modifier.layout { measurable, constraints ->
    var wholeWeight = 0F
    var startWeight = 0F
    var endWeight = 0F
    timeline.fastForEach {
      wholeWeight += it.nowWeight
      val startTime = item.startTime.time
      if (startTime <= it.endTime) {
        startWeight += if (startTime < it.startTime) it.nowWeight else {
          it.startTime.minutesUntil(startTime, true) /
              it.startTime.minutesUntil(it.endTime, true).toFloat()
        }
      }
      val endTime = item.startTime.plusMinutes(item.minuteDuration).time
      if (endTime <= it.endTime) {
        endWeight += if (endTime < it.startTime) it.nowWeight else {
          it.startTime.minutesUntil(endTime, true) /
              it.startTime.minutesUntil(it.endTime, true).toFloat()
        }
      }
    }
    val placeable = measurable.measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxWidth / 7,
        minHeight = 0,
        maxHeight = ((endWeight - startWeight) * constraints.maxHeight).roundToInt()
      )
    )
    layout(placeable.width, placeable.height) {
      placeable.placeRelative(
        x = item.startTime.date.dayOfWeekOrdinal * (constraints.maxWidth / 7),
        y = (startWeight * constraints.maxHeight).roundToInt()
      )
    }
  }) {
    item.Content()
  }
}



