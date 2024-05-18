package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 16:08
 */
@TableName("notification_status")
data class NotificationStatusEntity(
  @TableId
  val userId: Int,
  val hasNew: Boolean,
)