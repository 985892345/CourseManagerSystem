package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 11:10
 */
@TableName("course_class_stu")
data class CourseClassStuEntity(
  @TableId
  val classNum: String,
  val stuNum: String,
)
