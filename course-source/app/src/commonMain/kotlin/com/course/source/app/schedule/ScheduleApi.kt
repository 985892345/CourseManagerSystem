package com.course.source.app.schedule

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/1 10:55
 */
interface ScheduleApi {

  suspend fun getSchedule(
    addBeans: List<ScheduleBean> = emptyList(),
    updateBeans: List<ScheduleBean> = emptyList(),
    removeIds: Set<Int> = emptySet(),
  ): ResponseWrapper<List<ScheduleBean>>

  suspend fun addSchedule(
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
  ): ResponseWrapper<Int>

  suspend fun updateSchedule(bean: ScheduleBean): ResponseWrapper<Unit>

  suspend fun removeSchedule(id: Int): ResponseWrapper<Unit>
}

@Serializable
data class ScheduleBean(
  val id: Int,
  val title: String,
  val description: String,
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  val repeat: ScheduleRepeat,
)

