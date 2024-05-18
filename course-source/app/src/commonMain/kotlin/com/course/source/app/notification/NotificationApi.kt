package com.course.source.app.notification

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:05
 */
interface NotificationApi {

  suspend fun getNotifications(): ResponseWrapper<List<Notification>>

  suspend fun hasNewNotification(): ResponseWrapper<Boolean>

  suspend fun decision(notificationId: Int, isAgree: Boolean): ResponseWrapper<Unit>
}

@Serializable
data class Notification(
  val notificationId: Int,
  val time: MinuteTimeDate,
  val content: NotificationContent,
)

@Serializable
sealed interface NotificationContent {
  @Serializable
  data class Normal(
    val title: String,
    val content: String,
  ) : NotificationContent
  @Serializable
  data class AddSchedule(
    val teamName: String,
    val teamSenderName: String,
    val scheduleTitle: String,
    val scheduleDescription: String,
    val scheduleStartTime: MinuteTimeDate,
    val scheduleMinuteDuration: Int,
  ) : NotificationContent
  @Serializable
  data class Decision(
    val title: String,
    val content: String,
    var btn: DecisionBtn,
  ) : NotificationContent
}

@Serializable
sealed interface DecisionBtn {
  @Serializable
  data class Pending(
    val positiveText: String,
    val negativeText: String,
    val negativeDialog: String,
  ) : DecisionBtn
  @Serializable
  data class Agree(
    val positiveText: String,
  ) : DecisionBtn
  @Serializable
  data class Disagree(
    val negativeText: String,
  ) : DecisionBtn
  @Serializable
  data class Expired(
    val text: String,
  ) : DecisionBtn
}

