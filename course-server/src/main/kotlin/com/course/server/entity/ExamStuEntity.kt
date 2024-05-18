package com.course.server.entity

import com.baomidou.mybatisplus.annotation.TableName

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 12:10
 */
@TableName("exam_stu")
data class ExamStuEntity(
  val courseNum: String,
  val examType: String,
  val stuNum: String,
  val classroom: String,
  val seat: String,
)
