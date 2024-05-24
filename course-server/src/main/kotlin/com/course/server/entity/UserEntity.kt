package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.course.source.app.account.AccountType

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 18:01
 */
@TableName(value = "user")
data class UserEntity(
  @TableId(type = IdType.AUTO)
  var userId: Int,
  val password: String,
  @TableField(value = "type")
  val typeStr: String,
  val token: String?,
) {
  @TableField(exist = false)
  val type: AccountType = AccountType.valueOf(typeStr)
}