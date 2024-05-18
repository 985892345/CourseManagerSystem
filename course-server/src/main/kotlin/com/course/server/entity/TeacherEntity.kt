package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:35
 */
@TableName("teacher")
data class TeacherEntity(
  @TableId
  val teaNum: String,
  val name: String,
  val userId: Int,
  val major: String,
)