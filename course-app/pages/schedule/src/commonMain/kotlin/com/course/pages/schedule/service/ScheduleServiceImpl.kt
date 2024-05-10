package com.course.pages.schedule.service

import com.course.pages.schedule.api.IScheduleItemGroup
import com.course.pages.schedule.api.IScheduleService
import com.course.pages.schedule.service.course.ScheduleCourseItemGroup
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
    onCreate: suspend (ScheduleBean) -> Unit,
    onUpdate: suspend (ScheduleBean) -> Unit,
    onDelete: suspend (ScheduleBean) -> Unit
  ): IScheduleItemGroup {
    return ScheduleCourseItemGroup(
      onCreate = onCreate,
      onUpdate = onUpdate,
      onDelete = onDelete
    )
  }
}