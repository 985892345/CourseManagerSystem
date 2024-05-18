package com.course.pages.team.ui.course.base

import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountType
import com.course.source.app.course.getEndMinuteTime
import com.course.source.app.course.getStartMinuteTime

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

  val startTime = MinuteTimeDate(date, getStartMinuteTime(beginLesson))
  val minuteDuration = getEndMinuteTime(beginLesson + node.size - 1)
    .minuteOfDay - startTime.minuteOfDay

  class Node(
    val member: Set<Member>,
  )

  class Member(
    val name: String,
    val num: String,
    val type: AccountType,
  )
}