package com.course.pages.course.api.item.lesson

import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.course.CourseBean
import com.course.source.app.course.LessonBean
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/11 22:35
 */
@Serializable
data class LessonItemData(
  val week: Int,
  val startTime: MinuteTimeDate, // 开始时间
  val minuteDuration: Int, // 课程总分钟数，包括中途课间
  val lesson: LessonBean,
  val courseBean: CourseBean,
) {
  companion object {
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
  }
}

fun CourseBean.toLessonItemBean(): List<LessonItemData> {
  return lessons.flatMap { lesson ->
    lesson.weeks.map {
      val startTime = LessonItemData.getStartMinuteTime(lesson.beginLesson)
      val endTime = LessonItemData.getEndMinuteTime(lesson.beginLesson + lesson.length - 1)
      LessonItemData(
        week = it,
        startTime = MinuteTimeDate(beginDate.plusWeeks(it - 1).plusDays(lesson.dayOfWeek.ordinal), startTime),
        minuteDuration = startTime.minutesUntil(endTime),
        lesson = lesson,
        courseBean = this,
      )
    }
  }
}
