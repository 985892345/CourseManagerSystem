package com.course.server.service

import com.course.server.controller.CourseController
import com.course.shared.time.Date
import com.course.source.app.course.ClassMember
import com.course.source.app.course.CourseBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 20:31
 */
interface CourseService {

  fun getCourseBean(
    num: String,
  ): CourseBean

  fun getClassMembers(classNum: String): List<ClassMember>

  fun deleteCourse(
    teaNum: String,
    classPlanId: Int,
  )

  fun changeCourse(
    teaNum: String,
    classPlanId: Int,
    newDate: Date,
    newBeginLesson: Int,
    newLength: Int,
    newClassroom: String,
  )

  fun createCourse(
    teaNum: String,
    classNum: String,
    date: Date,
    beginLesson: Int,
    length: Int,
    classroom: String,
  ): Int

  fun add(data: List<CourseController.Course>)
}