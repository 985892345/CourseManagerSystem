package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 20:24
 */
@TableName("course")
data class CourseEntity(
  @TableId
  val courseNum: String,
  val courseName: String,
  val courseType: String,
)
