package com.course.source.app.course

import com.course.shared.course.Terms
import com.course.shared.course.TermsIntSerializer
import com.course.shared.time.Date
import com.course.source.app.response.ResponseWrapper
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:22
 */
interface CourseApi {

  suspend fun getCourseBean(
    stuNum: String,
    term: Terms? = null,
  ): ResponseWrapper<CourseBean>
}

@Serializable
data class CourseBean(
  val beginDate: Date, // 开始时间
  @Serializable(TermsIntSerializer::class)
  val term: Terms, // 学期
  val lessons: List<LessonBean>, // 课程
)

@Serializable
data class LessonBean(
  val id: Int, // 课程唯一 id，即使课程号相同，但上课时间不同，应返回不同的 id
  val lessonName: String, // 课程名
  val classroom: String, // 教室
  val teacher: String, // 老师名字
  val lessonNum: String, // 课程号
  val cycle: String, // 周期，比如：1-8周
  val type: String, // 选修或者必修
  val weeks: List<Int>, // 在哪几周上课
  val dayOfWeek: DayOfWeek, // 星期数
  val beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
  val length: Int, // 课的长度
)