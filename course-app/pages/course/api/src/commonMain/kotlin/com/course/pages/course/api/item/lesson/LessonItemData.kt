package com.course.pages.course.api.item.lesson

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.course.*
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/11 22:35
 */
@Serializable
data class LessonItemData(
  val week: Int, // 第几周
  val startTime: MinuteTimeDate, // 开始时间
  val minuteDuration: Int, // 课程总分钟数，包括中途课间
  val lesson: LessonBean,
  val period: LessonPeriod,
  val periodDate: LessonPeriodDate,
  val courseBean: CourseBean,
)

fun CourseBean.toLessonItemBean(): List<LessonItemData> {
  return lessons.flatMap { lesson ->
    lesson.toLessonItemBean(this)
  }
}

fun LessonBean.toLessonItemBean(courseBean: CourseBean): List<LessonItemData> {
  return period.flatMap { it.toLessonItemBean(courseBean, this) }
}

fun LessonPeriod.toLessonItemBean(courseBean: CourseBean, lesson: LessonBean): List<LessonItemData> {
  return dateList.map { it.toLessonItemBean(courseBean, lesson, this) }
}

fun LessonPeriodDate.toLessonItemBean(
  courseBean: CourseBean,
  lesson: LessonBean,
  period: LessonPeriod,
): LessonItemData {
  val week = courseBean.beginDate.daysUntil(date) / 7 + 1
  val startMinuteTime = getStartMinuteTime(period.beginLesson)
  val endMinuteTime = getEndMinuteTime(period.beginLesson + period.length - 1)
  return LessonItemData(
    week = week,
    startTime = MinuteTimeDate(date, startMinuteTime),
    minuteDuration = startMinuteTime.minutesUntil(endMinuteTime),
    lesson = lesson,
    period = period,
    periodDate = this,
    courseBean = courseBean,
  )
}

