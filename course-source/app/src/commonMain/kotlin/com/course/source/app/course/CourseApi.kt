package com.course.source.app.course

import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:22
 */
interface CourseApi {

  suspend fun getCourseBean(
    num: String,
  ): ResponseWrapper<CourseBean>

  suspend fun getClassMembers(classNum: String): ResponseWrapper<List<ClassMember>>

  suspend fun deleteCourse(
    classPlanId: Int,
  ): ResponseWrapper<Unit>

  suspend fun changeCourse(
    classPlanId: Int,
    newDate: String,
    newBeginLesson: Int,
    newLength: Int,
    newClassroom: String,
  ): ResponseWrapper<Unit>

  suspend fun createCourse(
    classNum: String,
    date: String,
    beginLesson: Int,
    length: Int,
    classroom: String,
  ): ResponseWrapper<Int>
}

@Serializable
data class CourseBean(
  val term: String,
  val beginDate: Date, // 开始时间
  val lessons: List<LessonBean>, // 课程
)

@Serializable
data class LessonBean(
  val courseNum: String, // 课程号
  val classNum: String, // 课程班级号
  val courseName: String, // 课程名
  val type: String, // 课程类型，选修或必修
  val period: List<LessonPeriod>, // 当前课程班级要上课的时间段
)

@Serializable
data class LessonPeriod(
  val dateList: List<LessonPeriodDate>,
  val beginLesson: Int,
  val length: Int,
  val classroom: String,
  val teacher: String, // 老师名字
)

@Serializable
data class LessonPeriodDate(
  val classPlanId: Int,
  val date: Date,
  val isNewly: Boolean, // 是否是后期新增课程
)

@Serializable
data class ClassMember(
  val name: String,
  val num: String,
)

fun getStartMinuteTime(lesson: Int): MinuteTime {
  return when (lesson) {
    1 -> MinuteTime(8, 0) // 第一节课开始
    2 -> MinuteTime(8, 55) // 第二节课开始
    3 -> MinuteTime(10, 15) // 第三节课开始
    4 -> MinuteTime(11, 10) // 第四节课开始
    5 -> MinuteTime(14, 0) // 第五节课开始
    6 -> MinuteTime(14, 55) // 第六节课开始
    7 -> MinuteTime(16, 10) // 第七节课开始
    8 -> MinuteTime(17, 10) // 第八节课开始
    9 -> MinuteTime(19, 0) // 第九节课开始
    10 -> MinuteTime(19, 55) // 第十节课开始
    11 -> MinuteTime(20, 50) // 第十一节课开始
    12 -> MinuteTime(21, 45) // 第十二节课开始
    else -> error("不支持的开始时间")
  }
}

fun getEndMinuteTime(lesson: Int): MinuteTime {
  return when (lesson) {
    1 -> MinuteTime(8, 45) // 第一节课结束
    2 -> MinuteTime(9, 40) // 第二节课结束
    3 -> MinuteTime(11, 0) // 第三节课结束
    4 -> MinuteTime(11, 55) // 第四节课结束
    5 -> MinuteTime(14, 45) // 第五节课结束
    6 -> MinuteTime(15, 40) // 第六节课结束
    7 -> MinuteTime(17, 0) // 第七节课结束
    8 -> MinuteTime(17, 55) // 第八节课结束
    9 -> MinuteTime(19, 45) // 第九节课结束
    10 -> MinuteTime(20, 40) // 第十节课结束
    11 -> MinuteTime(21, 35) // 第十一节课结束
    12 -> MinuteTime(22, 30) // 第十二节课结束
    else -> error("不支持的结束时间")
  }
}
