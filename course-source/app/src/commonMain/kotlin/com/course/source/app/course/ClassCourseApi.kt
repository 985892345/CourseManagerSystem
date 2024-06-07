package com.course.source.app.course

import com.course.source.app.response.ResponseWrapper

/**
 * .
 *
 * @author 985892345
 * 2024/6/7 10:10
 */
interface ClassCourseApi {
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