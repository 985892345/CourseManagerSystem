package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName
import com.course.source.app.attendance.AskForLeaveStatus

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:16
 */
@TableName("attendance_leave")
data class AttendanceLeaveEntity(
  val classPlanId: Int,
  val stuNum: String,
  val timestamp: Long,
  val reason: String,
  val status: String,
  val notificationId: Int,
) {
  @TableField(exist = false)
  val askForLeaveStatus: AskForLeaveStatus = AskForLeaveStatus.valueOf(status)
}