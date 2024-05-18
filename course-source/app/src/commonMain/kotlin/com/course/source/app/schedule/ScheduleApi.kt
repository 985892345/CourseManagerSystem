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
    body: LocalScheduleBody
  ): ResponseWrapper<List<ScheduleBean>>

  suspend fun addSchedule(
    bean: ScheduleBean
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
  val textColor: String,
  val backgroundColor: String,
)

@Serializable
data class LocalScheduleBody(
  val addBeans: List<ScheduleBean> = emptyList(),
  val updateBeans: List<ScheduleBean> = emptyList(),
  val removeIds: Set<Int> = emptySet(),
)

