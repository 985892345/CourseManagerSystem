package com.course.source.app.team

import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:41
 */
interface ClassApi {

  suspend fun getClassMembers(courseNum: String): ResponseWrapper<List<ClassMember>>

  suspend fun deleteCourse(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int
  ): ResponseWrapper<Unit>

  suspend fun changeCourse(
    courseNum: String,
    oldWeek: Int,
    oldDayOfWeek: DayOfWeek,
    oldBeginLesson: Int,
    oldLength: Int,
    oldClassroom: String,
    newWeek: Int,
    newDayOfWeek: DayOfWeek,
    newBeginLesson: Int,
    newLength: Int,
    newClassroom: String,
  ): ResponseWrapper<Unit>

  suspend fun createCourse(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int,
    length: Int,
    classroom: String,
  ): ResponseWrapper<Unit>
}

@Serializable
data class ClassMember(
  val name: String,
  val num: String,
  val type: AccountType,
)