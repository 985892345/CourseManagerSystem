package com.course.server.utils

import com.course.shared.time.Date
import com.course.shared.time.toChinese
import com.course.shared.utils.Num2CN

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 20:47
 */
object CourseUtils {

  fun getLessonPeriod(date: Date, beginLesson: Int, length: Int): String {
    val week = SchoolCalendar.getBeginDate().daysUntil(date) / 7 + 1
    val weekStr = "第${Num2CN.transform(week)}周"
    val dayOfWeek = date.dayOfWeek.toChinese()
    val period = List(length) { Num2CN.transform(beginLesson + it) }
      .joinToString("", postfix = "节")
    return "$weekStr  $dayOfWeek  $period"
  }

  fun getLessonPeriod(date: Date, beginLesson: Int, length: Int, courseName: String): String {
    return "${getLessonPeriod(date, beginLesson, length)}  $courseName"
  }


}