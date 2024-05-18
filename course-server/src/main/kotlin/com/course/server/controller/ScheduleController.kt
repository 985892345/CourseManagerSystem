package com.course.server.controller

import com.course.server.service.ScheduleService
import com.course.server.utils.TokenUtils
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.LocalScheduleBody
import com.course.source.app.schedule.ScheduleBean
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
  private val scheduleService: ScheduleService
) {

  @GetMapping("/get")
  fun getSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    body: LocalScheduleBody
  ): ResponseWrapper<List<ScheduleBean>> {
    val info = TokenUtils.parseToken(token)
   val result = scheduleService.getSchedule(
      userId = info.userId,
      addBeans = body.addBeans,
      updateBeans = body.updateBeans,
      removeIds = body.removeIds,
    )
    return ResponseWrapper.success(result)
  }

  @PostMapping("/add")
  fun addSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    bean: ScheduleBean,
  ): ResponseWrapper<Int> {
    val info = TokenUtils.parseToken(token)
    val result = scheduleService.addSchedule(info.userId, bean)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/update")
  fun updateSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    bean: ScheduleBean,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    scheduleService.addSchedule(info.userId, bean)
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