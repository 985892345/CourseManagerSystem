package com.course.server.service.impl

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.course.server.entity.AttendanceLeaveEntity
import com.course.server.entity.NotificationEntity
import com.course.server.entity.NotificationServerContent
import com.course.server.entity.NotificationStatusEntity
import com.course.server.mapper.AttendanceLeaveMapper
import com.course.server.mapper.NotificationMapper
import com.course.server.mapper.NotificationStatusMapper
import com.course.server.service.AttendanceService
import com.course.server.service.NotificationService
import com.course.server.utils.ResponseException
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.attendance.AskForLeaveStatus
import com.course.source.app.notification.DecisionBtn
import com.course.source.app.notification.Notification
import com.course.source.app.notification.NotificationContent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 16:04
 */
@Service
class NotificationServiceImpl(
  private val notificationMapper: NotificationMapper,
  private val notificationStatusMapper: NotificationStatusMapper,
  private val attendanceLeaveMapper: AttendanceLeaveMapper,
) : NotificationService {

  override fun getNotifications(userId: Int): List<Notification> {
    val now = System.currentTimeMillis()
    setNotificationStatusHasNew(userId, false)
    return notificationMapper.selectList(
      KtQueryWrapper(NotificationEntity::class.java)
        .eq(NotificationEntity::userId, userId)
    ).map {
      Notification(
        notificationId = it.notificationId,
        time = it.time,
        content = checkNotificationDecisionExpired(it, now)?.clientContent ?: it.serverContent.clientContent,
      )
    }
  }

  override fun hasNewNotification(userId: Int): Boolean {
    return notificationStatusMapper.selectById(userId)?.hasNew ?: false
  }

  override fun addNotification(userId: Int, time: MinuteTimeDate, content: NotificationServerContent): Int {
    val entity = NotificationEntity(
      notificationId = 0,
      userId = userId,
      time = time,
      contentStr = Json.encodeToString<NotificationServerContent>(content),
    )
    notificationMapper.insert(entity)
    setNotificationStatusHasNew(userId, true)
    return entity.notificationId
  }

  override fun decision(userId: Int, notificationId: Int, isAgree: Boolean) {
    val notification = notificationMapper.selectOne(
      KtQueryWrapper(NotificationEntity::class.java)
        .eq(NotificationEntity::notificationId, notificationId)
        .eq(NotificationEntity::userId, userId)
    )
    if (notification == null) throw ResponseException("不存在该通知")
    val content = notification.serverContent
    if (content !is NotificationServerContent.Decision) throw ResponseException("该通知不为决议通知")
    val clientContentBtn = content.clientContent.btn
    if (clientContentBtn is DecisionBtn.Expired) throw ResponseException("该通知已经过期")
    if (clientContentBtn !is DecisionBtn.Pending) throw ResponseException("该通知已经处理")
    val expiredText = checkNotificationDecisionExpired(notification)?.expiredText
    if (expiredText != null) {
      // 通知客户端消息已经过期，约定 code = 11000
      throw ResponseException(expiredText, 11000)
    }
    // 更新通知
    notificationMapper.update(
      KtUpdateWrapper(NotificationEntity::class.java)
        .eq(NotificationEntity::notificationId, notificationId)
        .eq(NotificationEntity::userId, userId)
        .set(
          NotificationEntity::contentStr,
          Json.encodeToString<NotificationServerContent>(
            content.copy(
              clientContent = content.clientContent.copy(
                btn = if (isAgree) DecisionBtn.Agree(clientContentBtn.positiveText)
                else DecisionBtn.Disagree(clientContentBtn.negativeText)
              )
            )
          )
        )
    )
    // 回复发送人
    addNotification(
      userId = content.responseUserId,
      time = MinuteTimeDate.now(),
      content = if (isAgree) content.positiveResponse else content.negativeResponse,
    )
    // 暂时以这种 trick 的方式解决请假申请
    if (content.clientContent.title == "学生请假申请") {
      attendanceLeaveMapper.update(
        KtUpdateWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::notificationId, notificationId)
          .set(AttendanceLeaveEntity::status, if (isAgree) AskForLeaveStatus.Approved else AskForLeaveStatus.Rejected)
      )
    }
  }

  private fun checkNotificationDecisionExpired(
    notification: NotificationEntity,
    now: Long = System.currentTimeMillis(),
  ): NotificationServerContent.Decision? {
    val clientContent = notification.serverContent.clientContent
    if (notification.serverContent is NotificationServerContent.Decision && clientContent is NotificationContent.Decision) {
      val btn = clientContent.btn
      if (btn is DecisionBtn.Pending) {
        // 检查是否过期
        if (now >= notification.serverContent.expiredTimestamp) {
          val newServerContent = notification.serverContent.copy(
            clientContent = clientContent.copy(
              btn = DecisionBtn.Expired(notification.serverContent.expiredText)
            )
          )
          // 过期了，修改数据库
          notificationMapper.update(
            KtUpdateWrapper(NotificationEntity::class.java)
              .eq(NotificationEntity::notificationId, notification.notificationId)
              .set(
                NotificationEntity::contentStr,
                Json.encodeToString<NotificationServerContent>(newServerContent)
              )
          )
          return newServerContent
        }
      }
    }
    return null
  }

  private fun setNotificationStatusHasNew(userId: Int, hasNew: Boolean) {
    val status = notificationStatusMapper.selectById(userId)
    if (status != null) {
      notificationStatusMapper.update(
        KtUpdateWrapper(NotificationStatusEntity::class.java)
          .eq(NotificationStatusEntity::userId, userId)
          .set(NotificationStatusEntity::hasNew, hasNew)
      )
    } else {
      notificationStatusMapper.insert(
        NotificationStatusEntity(
          userId = userId,
          hasNew = hasNew,
        )
      )
    }
  }
}