package com.course.source.app.local.attendance

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.attendance.AskForLeaveHistory
import com.course.source.app.attendance.AskForLeaveStatus
import com.course.source.app.attendance.AttendanceApi
import com.course.source.app.attendance.AttendanceCodeStatus
import com.course.source.app.attendance.AttendanceHistory
import com.course.source.app.attendance.AttendanceStatus
import com.course.source.app.response.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek

/**
 * .
 *
 * @author 985892345
 * 2024/5/7 11:59
 */
@ImplProvider
object AttendanceApiImpl : AttendanceApi {

  override suspend fun postAttendanceCode(
    courseNum: String,
    code: String
  ): ResponseWrapper<AttendanceCodeStatus> {
    return ResponseWrapper.success(AttendanceCodeStatus.Success)
  }

  override suspend fun getAttendanceHistory(courseNum: String): ResponseWrapper<List<AttendanceHistory>> {
    delay(200)
    return ResponseWrapper.success(
      listOf(
        AttendanceHistory(
          week = 1,
          time = MinuteTimeDate(2023, 5, 6, 10),
          status = AttendanceStatus.Attendance,
          classroomSimplify = "1111"
        ),
        AttendanceHistory(
          week = 2,
          time = MinuteTimeDate(2023, 5, 13, 10),
          status = AttendanceStatus.Attendance,
          classroomSimplify = "2222"
        ),
        AttendanceHistory(
          week = 3,
          time = MinuteTimeDate(2023, 5, 20, 10),
          status = AttendanceStatus.AskForLeave,
          classroomSimplify = "3333"
        ),
      )
    )
  }

  override suspend fun getAskForLeaveHistory(courseNum: String): ResponseWrapper<List<AskForLeaveHistory>> {
    delay(200)
    return ResponseWrapper.success(
      listOf(
        AskForLeaveHistory(
          week = 3,
          dayOfWeek = DayOfWeek.MONDAY,
          beginLesson = 9,
          length = 4,
          status = AskForLeaveStatus.Pending,
          description = "123kfjsadjfasfdsfasdfasdfdasfasdfasdfsdafja123kfjsadjfasfdsfasdfasdfdasfasdfasdfsdafjalfjlfj"
        ),
        AskForLeaveHistory(
          week = 2,
          dayOfWeek = DayOfWeek.MONDAY,
          beginLesson = 5,
          length = 3,
          status = AskForLeaveStatus.Approved,
          description = "123kfjsadjfasfdsfasdfasdfdasfasdfas123kfjsadjfasfdsfasdfasdfdasfasdfasdfsdafjalfjdfsdafjalfj"
        ),
        AskForLeaveHistory(
          week = 1,
          dayOfWeek = DayOfWeek.MONDAY,
          beginLesson = 1,
          length = 2,
          status = AskForLeaveStatus.Rejected,
          description = "123kfjsadjfasfdsfas123kfjsadjfasfdsfasdfasdfdasfasdfasdfsdafjalfjdfasdfdasfasdfasdfsdafjalfj"
        ),
      )
    )
  }

  override suspend fun postAskForLeave(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int,
    description: String
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }
}