package com.course.server.controller

import com.course.server.service.CourseService
import com.course.server.utils.TokenUtils
import com.course.shared.time.DateSerializer
import com.course.source.app.account.AccountType
import com.course.source.app.course.ClassMember
import com.course.source.app.course.CourseBean
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 20:24
 */
@RestController
@RequestMapping("/course")
class CourseController(
  private val courseService: CourseService
) {

  @GetMapping("/get")
  fun getCourseBean(
    num: String,
  ): ResponseWrapper<CourseBean> {
    val result = courseService.getCourseBean(num)
    return ResponseWrapper.success(result)
  }

  @GetMapping("/members")
  fun getClassMembers(classNum: String): ResponseWrapper<List<ClassMember>> {
    val result = courseService.getClassMembers(classNum)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/delete")
  fun deleteCourse(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    if (info.type != AccountType.Teacher) return ResponseWrapper.failure(20000, "权限不足")
    courseService.deleteCourse(info.num, classPlanId)
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/change")
  fun changeCourse(
    @RequestHeader(TokenUtils.header) token: String,
    classPlanId: Int,
    newDate: String,
    newBeginLesson: Int,
    newLength: Int,
    newClassroom: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    if (info.type != AccountType.Teacher) return ResponseWrapper.failure(20000, "权限不足")
    courseService.changeCourse(
      teaNum = info.num,
      classPlanId = classPlanId,
      newDate = DateSerializer.deserialize(newDate),
      newBeginLesson = newBeginLesson,
      newLength = newLength,
      newClassroom = newClassroom,
    )
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/create")
  fun createCourse(
    @RequestHeader(TokenUtils.header) token: String,
    classNum: String,
    date: String,
    beginLesson: Int,
    length: Int,
    classroom: String,
  ): ResponseWrapper<Int> {
    val info = TokenUtils.parseToken(token)
    if (info.type != AccountType.Teacher) return ResponseWrapper.failure(20000, "权限不足")
    val result = courseService.createCourse(
      teaNum = info.num,
      classNum = classNum,
      date = DateSerializer.deserialize(date),
      beginLesson = beginLesson,
      length = length,
      classroom = classroom,
    )
    return ResponseWrapper.success(result)
  }


  @PostMapping("/add")
  fun addCourse(
    @RequestBody
    data: List<Course>,
  ): ResponseWrapper<Unit> {
    courseService.add(data)
    return ResponseWrapper.success(Unit)
  }

  @Serializable
  data class Course(
    val courseNum: String,
    val classByClassNum: MutableMap<String, CourseClass>,
    val courseName: String,
    val courseType: String,
  )

  @Serializable
  data class CourseClass(
    val classNum: String,
    val stuNumSet: MutableSet<String>,
    val classPlanSet: MutableSet<ClassPlan>,
  )

  @Serializable
  data class ClassPlan(
    val teacher: String,
    val date: String,
    val beginLesson: Int,
    val length: Int,
    val classroom: String,
  )
}