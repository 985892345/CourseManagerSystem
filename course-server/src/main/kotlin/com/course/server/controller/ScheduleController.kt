package com.course.server.controller

import com.course.server.service.ScheduleService
import com.course.server.utils.TokenUtils
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.LocalScheduleBody
import com.course.source.app.schedule.ScheduleBean
import kotlinx.serialization.json.Json
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 15:25
 */
@RestController
@RequestMapping("/schedule")
class ScheduleController(
  private val scheduleService: ScheduleService,
) {

  @PostMapping("/get")
  fun getSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    @RequestBody body: String, // 构造器不唯一，这里暂时这样解决
  ): ResponseWrapper<List<ScheduleBean>> {
    val info = TokenUtils.parseToken(token)
    val localScheduleBody = Json.decodeFromString<LocalScheduleBody>(body)
    val result = scheduleService.getSchedule(
      userId = info.userId,
      addBeans = localScheduleBody.addBeans,
      updateBeans = localScheduleBody.updateBeans,
      removeIds = localScheduleBody.removeIds,
    )
    return ResponseWrapper.success(result)
  }

  @PostMapping("/add")
  fun addSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    @RequestBody body: String,
  ): ResponseWrapper<Int> {
    val info = TokenUtils.parseToken(token)
    val scheduleBean = Json.decodeFromString<ScheduleBean>(body)
    val result = scheduleService.addSchedule(info.userId, scheduleBean)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/update")
  fun updateSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    @RequestBody body: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    val scheduleBean = Json.decodeFromString<ScheduleBean>(body)
    scheduleService.addSchedule(info.userId, scheduleBean)
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/remove")
  fun removeSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    id: Int,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    scheduleService.removeSchedule(info.userId, id)
    return ResponseWrapper.success(Unit)
  }
}