package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.course.shared.time.MinuteTimeDate
import com.course.shared.time.MinuteTimeDateSerializer
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:29
 */
@TableName("schedule")
data class ScheduleEntity(
  @TableId(type = IdType.AUTO)
  var scheduleId: Int,
  val userId: Int,
  val title: String,
  val description: String,
  @TableField(value = "start_time")
  val startTimeStr: String,
  val minuteDuration: Int,
  val repeatContent: String,
  val textColor: String,
  val backgroundColor: String,
) {
  @TableField(exist = false)
  val startTime: MinuteTimeDate = MinuteTimeDateSerializer.deserialize(startTimeStr)
  @TableField(exist = false)
  val scheduleRepeat: ScheduleRepeat = Json.decodeFromString(repeatContent)
}