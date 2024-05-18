package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName
import com.course.source.app.attendance.AttendanceStatus

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:13
 */
@TableName("attendance_stu")
data class AttendanceStuEntity(
  val classPlanId: Int,
  val code: String,
  val stuNum: String,
  val timestamp: Long,
  val status: String,
  val isModified: Boolean,
) {
  @TableField(exist = false)
  val attendanceStatus: AttendanceStatus = AttendanceStatus.valueOf(status)
}