package com.course.source.app.course

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

  /**
   * @param termIndex 学期索引，大一上为0，传入负数时返回当前学期
   */
  suspend fun getCourseBean(
    stuNum: String,
    termIndex: Int,
  ): ResponseWrapper<CourseBean>
}

@Serializable
data class CourseBean(
  val term: String, // 学期
  val termIndex: Int, // 学期索引，大一上为0
  val beginDate: Date, // 开始时间
  val lessons: List<LessonBean>, // 课程
)

@Serializable
data class LessonBean(
  val course: String, // 课程名
  val classroom: String, // 教室
  val classroomSimplify: String, // 教室简写，用于 item 显示
  val teacher: String, // 老师名字
  val courseNum: String, // 课程号
  val weeks: List<Int>, // 在哪几周上课
  val dayOfWeek: DayOfWeek, // 星期数
  val beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
  val length: Int, // 课的长度
  val showOptions: List<Pair<String, String>>, // 点击后课程详细的展示选项
  val isNewlyAddedCourse: Boolean = false, // 是否为后期新增课程
)