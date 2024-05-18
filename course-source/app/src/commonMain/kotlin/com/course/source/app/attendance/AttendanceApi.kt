package com.course.source.app.attendance

import com.course.shared.time.Date
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 19:07
 */
interface AttendanceApi {

  // 老师

  suspend fun publishAttendance(
    classPlanId: Int,
    code: String,
    duration: Int, // 单位秒
    lateDuration: Int, // 迟到时长，单位秒
  ): ResponseWrapper<Unit>

  suspend fun getAttendanceStudent(
    classPlanId: Int,
  ): ResponseWrapper<List<AttendanceStudentList>>

  suspend fun changeAttendanceStatus(
    classPlanId: Int,
    code: String,
    stuNum: String,
    status: AttendanceStatus,
  ): ResponseWrapper<Unit>


  // 学生

  suspend fun postAttendanceCode(
    classPlanId: Int,
    code: String
  ): ResponseWrapper<AttendanceCodeStatus>

  suspend fun getAttendanceHistory(
    classNum: String,
    stuNum: String,
  ): ResponseWrapper<List<AttendanceHistory>>

  suspend fun getAskForLeaveHistory(
    classNum: String,
    stuNum: String,
  ): ResponseWrapper<List<AskForLeaveHistory>>

  suspend fun postAskForLeave(
    classPlanId: Int,
    reason: String,
  ): ResponseWrapper<Unit>
}

@Serializable
data class AttendanceHistory(
  val date: Date,
  val beginLesson: Int,
  val length: Int,
  val publishTimestamp: Long,
  val timestamp: Long,
  val status: AttendanceStatus,
  val isModified: Boolean,
)

@Serializable
data class AskForLeaveHistory(
  val date: Date,
  val beginLesson: Int,
  val length: Int,
  val reason: String,
  val status: AskForLeaveStatus,
)

@Serializable
data class AttendanceStudentList(
  val publishTimestamp: Long,
  val code: String,
  val students: List<AttendanceStudent>,
)

@Serializable
data class AttendanceStudent(
  val stuNum: String,
  val name: String,
  val status: AttendanceStatus,
)

enum class AttendanceCodeStatus {
  Success,
  Late,
  Absent,
  Invalid,
}

enum class AttendanceStatus(val chinese: String) {
  Attendance("出勤"),
  Absent("缺勤"),
  Late("迟到"),
  AskForLeave("请假"),
}

enum class AskForLeaveStatus {
  Pending,
  Approved,
  Rejected,
}