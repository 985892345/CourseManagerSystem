package com.course.server.controller

import com.course.server.service.AttendanceService
import com.course.server.utils.TokenUtils
import com.course.source.app.account.AccountType
import com.course.source.app.attendance.*
import com.course.source.app.response.ResponseWrapper
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:36
 */
@RestController
@RequestMapping("/attendance")
class AttendanceController(
  private val attendanceService: AttendanceService,
) {

  // 教师

  @PostMapping("/publish")
  fun publishAttendance(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
    code: String,
    duration: Int, // 单位秒
    lateDuration: Int, // 迟到时长，单位秒
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    if (info.type != AccountType.Teacher) return ResponseWrapper.failure(10001, "权限不足")
    attendanceService.publishAttendanceCode(info.num, classPlanId, code, duration, lateDuration)
    return ResponseWrapper.success(Unit)
  }

  @GetMapping("/students")
  fun getAttendanceStudent(
    classPlanId: Int,
  ): ResponseWrapper<List<AttendanceStudentList>> {
    val result = attendanceService.getAttendanceStudent(classPlanId)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/change")
  fun changeAttendanceStatus(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
    code: String,
    stuNum: String,
    status: AttendanceStatus,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    if (info.type != AccountType.Teacher) return ResponseWrapper.failure(10001, "权限不足")
    attendanceService.changeAttendanceStatus(info.num, classPlanId, code, stuNum, status)
    return ResponseWrapper.success(Unit)
  }

  // 学生

  @PostMapping("/code")
  fun postAttendanceCode(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
    code: String
  ): ResponseWrapper<AttendanceCodeStatus> {
    val info = TokenUtils.parseToken(token)
    val result = attendanceService.postAttendanceCode(classPlanId, code, info.num)
    return ResponseWrapper.success(result)
  }

  @GetMapping("/history")
  fun getAttendanceHistory(
    classNum: String,
    stuNum: String,
  ): ResponseWrapper<List<AttendanceHistory>> {
    val result = attendanceService.getAttendanceHistory(classNum, stuNum)
    return ResponseWrapper.success(result)
  }

  @GetMapping("/askForLeave")
  fun getAskForLeaveHistory(
    classNum: String,
    stuNum: String,
  ): ResponseWrapper<List<AskForLeaveHistory>> {
    val result = attendanceService.getAskForLeaveHistory(classNum, stuNum)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/askForLeave")
  fun postAskForLeave(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
    reason: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    attendanceService.postAskForLeave(classPlanId, info.num, reason)
    return ResponseWrapper.success(Unit)
  }
}