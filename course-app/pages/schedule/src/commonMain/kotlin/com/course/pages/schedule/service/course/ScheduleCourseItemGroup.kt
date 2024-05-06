package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.ui.item.ScheduleItemGroup
import com.course.shared.time.Date

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:44
 */
class ScheduleCourseItemGroup(
  val scheduleItemGroups: State<Map<Int, ScheduleItemGroup>>,
) : ICourseItemGroup {

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    scheduleItemGroups.value.forEach {
      key(it.key) {
        with(it.value) {
          ItemGroupContent(
            weekBeginDate = weekBeginDate,
            timeline = timeline,
            scrollState = scrollState,
          )
        }
      }
    }
  }
}