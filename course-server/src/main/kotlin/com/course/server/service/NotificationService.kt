package com.course.server.service

import com.course.server.entity.NotificationServerContent
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.notification.Notification

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 16:04
 */
interface NotificationService {

  fun getNotifications(userId: Int): List<Notification>

  fun hasNewNotification(userId: Int): Boolean

  fun addNotification(userId: Int, time: MinuteTimeDate, content: NotificationServerContent)

  fun decision(userId: Int, notificationId: Int, isAgree: Boolean)
}