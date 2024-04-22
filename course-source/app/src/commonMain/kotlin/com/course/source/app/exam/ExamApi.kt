package com.course.source.app.exam

import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 12:55
 */
interface ExamApi {

  /**
   * 获取所有考试信息
   */
  suspend fun getExam(
    stuNum: String,
  ): ResponseWrapper<List<ExamTermBean>>
}

@Serializable
data class ExamTermBean(
  val term: String,
  val termIndex: Int,
  val beginDate: Date,
  val exams: List<ExamBean>,
)

@Serializable
data class ExamBean(
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  val course: String,
  val courseNum: String,
  val classroom: String,
  val type: String,
  val seat: String,
)
