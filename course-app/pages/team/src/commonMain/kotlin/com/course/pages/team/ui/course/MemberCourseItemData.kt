package com.course.pages.team.ui.course

import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.team.TeamMember

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 17:30
 */
data class MemberCourseItemData(
  val date: Date,
  val beginLesson: Int,
  val node: List<Node>,
) {

  val startTime = MinuteTimeDate(date, LessonItemData.getStartMinuteTime(beginLesson))
  val minuteDuration = LessonItemData.getEndMinuteTime(beginLesson + node.size - 1)
    .minuteOfDay - startTime.minuteOfDay

  class Node(
    val member: Set<TeamMember>,
  )
}