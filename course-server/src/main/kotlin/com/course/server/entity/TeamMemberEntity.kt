package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName
import com.course.source.app.team.TeamRole

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:35
 */
@TableName("team_member")
data class TeamMemberEntity(
  val teamId: Int,
  val userId: Int,
  @TableField(value = "role")
  val roleStr: String,
  val identity: String,
  val isConfirmed: Boolean,
  val inviteNotificationId: Int?,
) {

  @TableField(exist = false)
  val role: TeamRole = TeamRole.valueOf(roleStr)
}