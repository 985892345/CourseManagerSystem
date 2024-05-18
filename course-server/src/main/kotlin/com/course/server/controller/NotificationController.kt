package com.course.server.controller

import com.course.server.service.NotificationService
import com.course.server.utils.TokenUtils
import com.course.source.app.notification.Notification
import com.course.source.app.response.ResponseWrapper
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 16:03
 */
@RestController
@RequestMapping("/notification")
class NotificationController(
  private val notificationService: NotificationService,
) {

  @GetMapping("/get")
  fun getNotifications(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<List<Notification>> {
    val info = TokenUtils.parseToken(token)
    val result = notificationService.getNotifications(info.userId)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/hasNew")
  fun hasNewNotification(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<Boolean> {
    val info = TokenUtils.parseToken(token)
    val result = notificationService.hasNewNotification(info.userId)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/decision")
  fun decision(
    @RequestHeader(TokenUtils.header) token: String,
    notificationId: Int,
    isAgree: Boolean,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    notificationService.decision(info.userId, notificationId, isAgree)
    return ResponseWrapper.success(Unit)
  }
}