package com.course.source.app.attendance

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.response.ResponseWrapper
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 19:07
 */
interface AttendanceApi {

  suspend fun postAttendanceCode(
    courseNum: String,
    code: String
  ): ResponseWrapper<AttendanceCodeStatus>

  suspend fun getAttendanceHistory(
    courseNum: String,
  ): ResponseWrapper<List<AttendanceHistory>>

  suspend fun getAskForLeaveHistory(
    courseNum: String,
  ): ResponseWrapper<List<AskForLeaveHistory>>

  suspend fun postAskForLeave(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int,
    description: String,
  ): ResponseWrapper<Unit>
}

@Serializable
data class AttendanceHistory(
  val week: Int,
  val time: MinuteTimeDate,
  val status: AttendanceStatus,
  val classroomSimplify: String,
)

@Serializable
data class AskForLeaveHistory(
  val week: Int,
  val dayOfWeek: DayOfWeek,
  val beginLesson: Int,
  val length: Int,
  val description: String,
  val status: AskForLeaveStatus,
)

enum class AttendanceCodeStatus {
  Success,
  Late,
  Invalid,
}

enum class AttendanceStatus {
  Attendance,
  Absent,
  Late,
  AskForLeave,
}

enum class AskForLeaveStatus {
  Pending,
  Approved,
  Rejected,
}