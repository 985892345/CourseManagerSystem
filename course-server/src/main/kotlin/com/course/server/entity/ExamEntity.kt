package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName
import com.course.shared.time.MinuteTimeDate
import com.course.shared.time.MinuteTimeDateSerializer

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 21:41
 */
@TableName("exam")
data class ExamEntity(
  val courseNum: String,
  val examType: String,
  @TableField(value = "start_time")
  val startTimeStr: String,
  val minuteDuration: Int,
) {
  @TableField(exist = false)
  val startTime: MinuteTimeDate = MinuteTimeDateSerializer.deserialize(startTimeStr)
}