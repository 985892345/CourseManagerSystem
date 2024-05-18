package com.course.server.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.course.shared.time.Date
import com.course.shared.time.DateSerializer

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 11:08
 */
@TableName("course_class_plan")
data class CourseClassPlanEntity(
  @TableId(type = IdType.AUTO)
  var classPlanId: Int,
  val classNum: String,
  val teaNum: String,
  @TableField(value = "date")
  val dateStr: String,
  val beginLesson: Int,
  val length: Int,
  val classroom: String,
  val isNewly: Boolean,
) {
  @TableField(exist = false)
  val date: Date = DateSerializer.deserialize(dateStr)
}
