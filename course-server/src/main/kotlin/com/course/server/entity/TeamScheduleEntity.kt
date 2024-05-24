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
 * 2024/5/23 21:36
 */
@TableName("team_schedule")
class TeamScheduleEntity(
  @TableId(type = IdType.AUTO)
  var teamScheduleId: Int,
  val teamId: Int,
  val senderId: Int,
  val title: String,
  val content: String,
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
  val repeat: ScheduleRepeat = Json.decodeFromString(repeatContent)
}