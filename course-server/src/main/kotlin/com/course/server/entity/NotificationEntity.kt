package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.notification.NotificationContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:29
 */
@TableName("notification")
data class NotificationEntity(
  @TableId(type = IdType.AUTO)
  var notificationId: Int,
  val userId: Int,
  val time: MinuteTimeDate,
  @TableField(value = "content")
  val contentStr: String,
) {
  @TableField(exist = false)
  val serverContent: NotificationServerContent = Json.decodeFromString(contentStr)
}

@Serializable
sealed interface NotificationServerContent {
  val clientContent: NotificationContent
  @Serializable
  data class Normal(override val clientContent: NotificationContent.Normal) : NotificationServerContent {
    constructor(
      title: String,
      content: String,
      subtitle: String? = null,
      bottomEnd: String? = null,
    ): this(NotificationContent.Normal(title, content, subtitle, bottomEnd))
  }

  @Serializable
  data class Decision(
    override val clientContent: NotificationContent.Decision,
    val responseUserId: Int,
    val positiveResponse: Normal,
    val negativeResponse: Normal,
    val expiredText: String,
    val expiredTimestamp: Long,
    val decisionType: DecisionType,
  ) : NotificationServerContent
}

@Serializable
sealed interface DecisionType {
  @Serializable
  data class AskForLeave(
    val leaveId: Int,
  ): DecisionType

  @Serializable
  data class TeamInvite(
    val teamId: Int,
    val userId: Int,
  ): DecisionType
}
