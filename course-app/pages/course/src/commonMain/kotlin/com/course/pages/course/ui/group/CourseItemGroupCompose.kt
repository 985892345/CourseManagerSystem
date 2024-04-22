package com.course.pages.course.ui.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.ui.pager.CoursePagerState
import com.course.pages.course.ui.pager.WeekItemsProvider
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:41
 */
@Composable
fun CoursePagerState.CourseItemGroupCompose(
  modifier: Modifier = Modifier.fillMaxSize(),
) {
  Box(
    modifier = Modifier.then(modifier)
  ) {
    // 使用 toList 避免并发修改
    weekItems.toList().fastForEach {
      key(it.itemKey) {
        CourseItemCompose(
          date = WeekItemsProvider.getItemWhichDate(it, timeline),
          item = it,
          timeline = timeline,
        )
      }
    }
  }
}

@Composable
private fun CoursePagerState.CourseItemCompose(
  date: Date,
  item: ICourseItem,
  timeline: CourseTimeline,
) {
  Box(modifier = Modifier.layout { measurable, constraints ->
    var startWeight = 0F
    var endWeight = 0F
    var allWeight = 0F
    val startTime = item.startTime
    val endTime = item.startTime.plusMinutes(item.minuteDuration)
    timeline.data.fastForEach {
      allWeight += it.nowWeight
      val start: MinuteTimeDate
      val end: MinuteTimeDate
      if (it.hasTomorrow) {
        if (it.startTime > it.endTime) {
          start = MinuteTimeDate(date, it.startTime)
          end = MinuteTimeDate(date.plusDays(1), it.endTime)
        } else {
          val tomorrow = date.plusDays(1)
          start = MinuteTimeDate(tomorrow, it.startTime)
          end = MinuteTimeDate(tomorrow, it.endTime)
        }
      } else {
        start = MinuteTimeDate(date, it.startTime)
        end = MinuteTimeDate(date, it.endTime)
      }
      if (startTime > end) {
        startWeight += it.nowWeight
      } else if (startTime > start) {
        startWeight += start.minutesUntil(startTime) /
            start.minutesUntil(end).toFloat() * it.nowWeight
      }
      if (endTime > end) {
        endWeight += it.nowWeight
      } else if (endTime > start) {
        endWeight += start.minutesUntil(endTime) /
            start.minutesUntil(end).toFloat() * it.nowWeight
      }
    }
    val placeable = measurable.measure(
      constraints.copy(
        minWidth = 0,
        maxWidth = constraints.maxWidth / 7,
        minHeight = 0,
        maxHeight = ((endWeight - startWeight) / allWeight * constraints.maxHeight).roundToInt()
      )
    )
    layout(constraints.maxWidth, constraints.maxHeight) {
      placeable.placeRelativeWithLayer(
        x = date.dayOfWeekOrdinal * (constraints.maxWidth / 7),
        y = (startWeight / allWeight * constraints.maxHeight).roundToInt()
      )
    }
  }) {
    item.Content(
      date,
      timeline,
      scrollState,
      itemClickShow,
    )
  }
}



