package com.course.pages.schedule.api.item

import com.course.pages.course.api.item.ICourseItemGroup
import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 23:49
 */
interface IScheduleCourseItemGroup : ICourseItemGroup {

  fun resetData(data: Collection<ScheduleBean>)
}