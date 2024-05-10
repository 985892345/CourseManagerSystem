package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.IScheduleItemGroup
import com.course.pages.schedule.ui.item.ScheduleItemGroup
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:44
 */
class ScheduleCourseItemGroup(
  val onCreate: suspend (ScheduleBean) -> Unit,
  val onUpdate: suspend (ScheduleBean) -> Unit,
  val onDelete: suspend (ScheduleBean) -> Unit,
) : IScheduleItemGroup {

  private val scheduleItemGroups = mutableStateOf(mutableMapOf<Int, ScheduleItemGroup>())
  private val placeholderScheduleCourseItemGroup = PlaceholderScheduleCourseItemGroup(onCreate)

  override fun resetData(data: Collection<ScheduleBean>) {
    val oldMap = scheduleItemGroups.value
    val newMap = mutableMapOf<Int, ScheduleItemGroup>()
    data.forEach {
      val oldItemGroup = oldMap[it.id]
      if (oldItemGroup != null) {
        oldItemGroup.changeBean(it)
        newMap[it.id] = oldItemGroup
      } else {
        newMap[it.id] = ScheduleItemGroup(it, onUpdate, onDelete)
      }
    }
    scheduleItemGroups.value = newMap
  }

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    placeholderScheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
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