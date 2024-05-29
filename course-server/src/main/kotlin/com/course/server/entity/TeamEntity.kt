package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:33
 */
@TableName("team")
data class TeamEntity(
  @TableId(type = IdType.AUTO)
  var teamId: Int,
  val teamName: String,
  val description: String,
  val adminId: Int,
)