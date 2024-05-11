package com.course.source.app.local.notification

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.notification.Notification
import com.course.source.app.notification.NotificationApi
import com.course.source.app.notification.NotificationContent
import com.course.source.app.response.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:15
 */
@ImplProvider
object NotificationApiImpl : NotificationApi {

  override suspend fun getNotifications(): ResponseWrapper<List<Notification>> {
    return ResponseWrapper.success(
      listOf(
        Notification(
          time = MinuteTimeDate(2024, 5, 1, 11),
          content = NotificationContent.AddSchedule(
            teamName = "测试",
            teamSenderName = "甲",
            scheduleTitle = "123测试",
            scheduleDescription = "123测试123测试123测试123测试123测试123测试123测试",
            scheduleStartTime = MinuteTimeDate(2024, 5, 1, 12),
            scheduleMinuteDuration = 30,
          ),
        ),
        Notification(
          time = MinuteTimeDate(2024, 5, 2, 11),
          content = NotificationContent.Decision(
            id = 1,
            title = "某某某邀请你加入团队1",
            content = "团队简介：这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述",
            positiveText = "已同意",
            negativeText = "已拒绝",
            negativeDialog = "确定取消加入该团队吗？",
            agreeOrNot = null,
          ),
        ),
        Notification(
          time = MinuteTimeDate(2024, 5, 2, 11),
          content = NotificationContent.Normal(
            title = "这是一段标题",
            content = "这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述"
          ),
        ),
      )
    )
  }

  override suspend fun hasNewNotification(): ResponseWrapper<Boolean> {
    return ResponseWrapper.success(true)
  }
}