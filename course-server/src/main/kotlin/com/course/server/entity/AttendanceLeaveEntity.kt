package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
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
  @TableId(type = IdType.AUTO)
  var leaveId: Int,
  val classPlanId: Int,
  val stuNum: String,
  val timestamp: Long,
  val reason: String,
  @TableField(value = "status")
  val statusStr: String,
) {
  @TableField(exist = false)
  val status: AskForLeaveStatus = AskForLeaveStatus.valueOf(statusStr)
}