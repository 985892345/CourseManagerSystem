package com.course.server.controller

import com.course.server.service.ExamService
import com.course.source.app.exam.ExamTermBean
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 17:07
 */
@RestController
@RequestMapping("/exam")
class ExamController(
  private val examService: ExamService
) {

  @GetMapping("/get")
  fun getExam(
    stuNum: String,
  ): ResponseWrapper<ExamTermBean> {
    val result = examService.getExam(stuNum)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/add")
  fun addExam(
    @RequestBody
    exams: List<AddExam>,
  ): ResponseWrapper<Unit> {
    examService.addExam(exams)
    return ResponseWrapper.success(Unit)
  }

  @Serializable
  data class AddExam(
    val stuNum: String,
    val exams: List<Exam>,
  )

  @Serializable
  data class Exam(
    val courseNum: String,
    val courseName: String,
    val startTime: String,
    val minuteDuration: Int,
    val classroom: String,
    val seat: String,
    val type: String,
  )
}