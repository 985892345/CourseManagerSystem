package com.course.server.service

import com.course.source.app.attendance.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:51
 */
interface AttendanceService {

  // 教师

  fun publishAttendanceCode(
    teaNum: String,
    classPlanId: Int,
    code: String,
    duration: Int, // 单位秒
    lateDuration: Int, // 迟到时长，单位秒
  )

  fun getAttendanceStudent(
    classPlanId: Int,
  ): List<AttendanceStudentList>

  fun changeAttendanceStatus(
    teaNum: String,
    classPlanId: Int,
    code: String,
    stuNum: String,
    status: AttendanceStatus,
  )

  // 学生

  fun postAttendanceCode(
    classPlanId: Int,
    code: String,
    stuNum: String,
  ): AttendanceCodeStatus

  fun getAttendanceHistory(
    classNum: String,
    stuNum: String,
  ): List<AttendanceHistory>

  fun getAskForLeaveHistory(
    classNum: String,
    stuNum: String,
  ): List<AskForLeaveHistory>

  fun postAskForLeave(
    classPlanId: Int,
    stuNum: String,
    reason: String,
  )
}