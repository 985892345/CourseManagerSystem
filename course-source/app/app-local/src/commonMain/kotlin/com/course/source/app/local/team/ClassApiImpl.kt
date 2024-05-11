package com.course.source.app.local.team

import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.team.ClassApi
import com.course.source.app.team.ClassMember
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.datetime.DayOfWeek

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 17:27
 */
@ImplProvider
object ClassApiImpl : ClassApi {
  override suspend fun getClassMembers(courseNum: String): ResponseWrapper<List<ClassMember>> {
    return ResponseWrapper.success(
      listOf(
        ClassMember(
          name = "甲",
          num = "2023214399",
          type = AccountType.Student,
        ),
        ClassMember(
          name = "乙",
          num = "2023214398",
          type = AccountType.Student,
        ),
        ClassMember(
          name = "丙",
          num = "2023214397",
          type = AccountType.Student,
        ),
      )
    )
  }

  override suspend fun deleteCourse(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun changeCourse(
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
    newClassroom: String
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun createCourse(
    courseNum: String,
    week: Int,
    dayOfWeek: DayOfWeek,
    beginLesson: Int,
    length: Int,
    classroom: String
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }
}