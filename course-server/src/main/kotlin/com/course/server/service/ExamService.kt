package com.course.server.service

import com.course.server.controller.ExamController
import com.course.source.app.exam.ExamTermBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 21:44
 */
interface ExamService {

  fun getExam(
    stuNum: String,
  ): ExamTermBean

  fun addExam(
    exams: List<ExamController.AddExam>,
  )
}