package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 13:08
 */
@TableName("attendance_code")
class AttendanceCodeEntity(
  val classPlanId: Int,
  val code: String,
  val publishTimestamp: Long,
  val lateTimestamp: Long,
  val finishTimestamp: Long,
)