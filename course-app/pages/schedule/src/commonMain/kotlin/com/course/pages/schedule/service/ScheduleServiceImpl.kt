package com.course.pages.schedule.service

import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.IScheduleService
import com.course.pages.schedule.api.item.BottomSheetScheduleItem
import com.course.pages.schedule.api.item.IScheduleCourseItemGroup
import com.course.pages.schedule.api.item.edit.ScheduleColorData
import com.course.pages.schedule.service.course.ScheduleCourseItemGroup
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 23:31
 */
@ImplProvider
object ScheduleServiceImpl : IScheduleService {
  override fun getScheduleCourseItemGroup(
    colorData: ScheduleColorData?,
    onCreate: (suspend (ScheduleBean) -> Unit)?,
    onUpdate: (suspend (ScheduleBean) -> Unit)?,
    onDelete: (suspend (ScheduleBean) -> Unit)?,
    onClick: (
      item: BottomSheetScheduleItem,
      repeatCurrent: Int,
      weekBeginDate: Date,
      timeline: CourseTimeline,
    ) -> Unit,
  ): IScheduleCourseItemGroup {
    return ScheduleCourseItemGroup(
      colorData = colorData,
      onCreate = onCreate,
      onUpdate = onUpdate,
      onDelete = onDelete,
      onClick = onClick,
    )
  }
}