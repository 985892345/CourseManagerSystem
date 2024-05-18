package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.course.server.entity.ScheduleEntity
import com.course.server.mapper.ScheduleMapper
import com.course.server.service.ScheduleService
import com.course.source.app.schedule.ScheduleBean
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 15:24
 */
@Service
class ScheduleServiceImpl(
  private val scheduleMapper: ScheduleMapper,
) : ScheduleService {

  override fun getSchedule(
    userId: Int,
    addBeans: List<ScheduleBean>,
    updateBeans: List<ScheduleBean>,
    removeIds: Set<Int>
  ): List<ScheduleBean> {
    addBeans.forEach { addSchedule(userId, it) }
    updateBeans.forEach { updateSchedule(userId, it) }
    removeIds.forEach { removeSchedule(userId, it) }
    return scheduleMapper.selectList(
      KtQueryWrapper(ScheduleEntity::class.java)
        .eq(ScheduleEntity::userId, userId)
    ).map { it.toBean() }
  }

  override fun addSchedule(userId: Int, bean: ScheduleBean): Int {
    val entity = bean.toEntity(userId, 0)
    scheduleMapper.insert(entity)
    return entity.scheduleId
  }

  override fun updateSchedule(userId: Int, bean: ScheduleBean) {
    scheduleMapper.update(
      bean.toEntity(userId),
      KtQueryWrapper(ScheduleEntity::class.java)
        .eq(ScheduleEntity::scheduleId, bean.id)
        .eq(ScheduleEntity::userId, userId)
    )
  }

  override fun removeSchedule(userId: Int, id: Int) {
    scheduleMapper.delete(
      KtQueryWrapper(ScheduleEntity::class.java)
        .eq(ScheduleEntity::scheduleId, id)
        .eq(ScheduleEntity::userId, userId)
    )
  }

  private fun ScheduleBean.toEntity(userId: Int, id: Int = this.id): ScheduleEntity {
    return ScheduleEntity(
      scheduleId = id,
      userId = userId,
      title = title,
      description = description,
      startTimeStr = startTime.toString(),
      minuteDuration = minuteDuration,
      repeatStr = Json.encodeToString(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
  }

  private fun ScheduleEntity.toBean(): ScheduleBean {
    return ScheduleBean(
      id = scheduleId,
      title = title,
      description = description,
      startTime = startTime,
      minuteDuration = minuteDuration,
      repeat = repeat,
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
  }
}