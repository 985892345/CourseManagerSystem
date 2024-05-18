package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 11:07
 */
@TableName("course_class")
data class CourseClassEntity(
  @TableId
  val classNum: String,
  val courseNum: String,
)
