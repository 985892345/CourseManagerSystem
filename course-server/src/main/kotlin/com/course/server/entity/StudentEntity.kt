package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:22
 */
@TableName("student")
data class StudentEntity(
  @TableId
  val stuNum: String,
  val name: String,
  val userId: Int,
  val major: String,
  val entryYear: Int,
)