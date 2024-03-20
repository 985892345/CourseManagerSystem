package com.course.pages.course.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class StuLessonBean(
  @SerialName("data")
  val `data`: List<StuLesson>,
  @SerialName("info")
  val info: String,
  @SerialName("nowWeek")
  val nowWeek: Int,
  @SerialName("status")
  val status: Int,
  @SerialName("stuNum")
  val stuNum: String,
  @SerialName("version")
  val version: String
) {
  @Serializable
  data class StuLesson(
    @SerialName("begin_lesson")
    val beginLesson: Int,
    @SerialName("classroom")
    val classroom: String,
    @SerialName("course")
    val course: String,
    @SerialName("course_num")
    val courseNum: String,
    @SerialName("day")
    val day: String,
    @SerialName("hash_day")
    val hashDay: Int, // 星期数，0 为星期一
    @SerialName("hash_lesson")
    val hashLesson: Int, // 课的起始数（我也不知道怎么具体描述），0 为 1、2 节课，1 为 3、4 节课，依次类推
    @SerialName("lesson")
    val lesson: String,
    @SerialName("period")
    val period: Int, // 课的长度
    @SerialName("rawWeek")
    val rawWeek: String,
    @SerialName("teacher")
    val teacher: String,
    @SerialName("type")
    val type: String,
    @SerialName("week")
    val week: List<Int>,
    @SerialName("week_begin")
    val weekBegin: Int,
    @SerialName("week_end")
    val weekEnd: Int,
    @SerialName("weekModel")
    val weekModel: String
  )
}