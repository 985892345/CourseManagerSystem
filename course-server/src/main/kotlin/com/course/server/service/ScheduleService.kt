package com.course.server.service

import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 15:24
 */
interface ScheduleService {

  fun getSchedule(
    userId: Int,
    addBeans: List<ScheduleBean>,
    updateBeans: List<ScheduleBean>,
    removeIds: Set<Int>,
  ): List<ScheduleBean>

  fun addSchedule(
    userId: Int,
    bean: ScheduleBean,
  ): Int

  fun updateSchedule(userId: Int, bean: ScheduleBean)

  fun removeSchedule(userId: Int, id: Int)
}