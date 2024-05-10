package com.course.pages.schedule.api

import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.item.BottomSheetScheduleItem
import com.course.pages.schedule.api.item.IScheduleCourseItemGroup
import com.course.pages.schedule.api.item.showAddScheduleBottomSheet
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 23:29
 */
interface IScheduleService {

  fun getScheduleCourseItemGroup(
    onCreate: (suspend (ScheduleBean) -> Unit)? = null,
    onUpdate: (suspend (ScheduleBean) -> Unit)? = null,
    onDelete: (suspend (ScheduleBean) -> Unit)? = null,
    onClick: (
      item: BottomSheetScheduleItem,
      repeatCurrent: Int,
      weekBeginDate: Date,
      timeline: CourseTimeline,
    ) -> Unit = { item, repeatCurrent, weekBeginDate, timeline ->
      showAddScheduleBottomSheet(item, repeatCurrent, weekBeginDate, timeline)
    },
  ): IScheduleCourseItemGroup
}