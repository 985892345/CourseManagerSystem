package com.course.pages.schedule.api

import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 23:29
 */
interface IScheduleService {

  fun getScheduleCourseItemGroup(
    onCreate: suspend (ScheduleBean) -> Unit,
    onUpdate: suspend (ScheduleBean) -> Unit,
    onDelete: suspend (ScheduleBean) -> Unit,
  ): IScheduleItemGroup
}