package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.course.server.controller.CourseController
import com.course.server.entity.*
import com.course.server.mapper.*
import com.course.server.service.CourseService
import com.course.server.service.NotificationService
import com.course.server.utils.CourseUtils
import com.course.server.utils.NumUtils
import com.course.server.utils.ResponseException
import com.course.server.utils.SchoolCalendar
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.course.*
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 20:31
 */
@Service
class CourseServiceImpl(
  private val studentMapper: StudentMapper,
  private val teacherMapper: TeacherMapper,
  private val courseMapper: CourseMapper,
  private val courseClassMapper: CourseClassMapper,
  private val courseClassStuMapper: CourseClassStuMapper,
  private val courseClassPlanMapper: CourseClassPlanMapper,
  private val notificationService: NotificationService,
) : CourseService {

  override fun getCourseBean(num: String): CourseBean {
    return CourseBean(
      term = if (NumUtils.isStudent(num)) {
        SchoolCalendar.getTermStr(studentMapper.selectById(num).entryYear) ?: "未知学期"
      } else "",
      beginDate = SchoolCalendar.getBeginDate(),
      lessons = getLessons(num),
    )
  }

  override fun getClassMembers(classNum: String): List<ClassMember> {
    return courseClassStuMapper.selectList(
      KtQueryWrapper(CourseClassStuEntity::class.java)
        .eq(CourseClassStuEntity::classNum, classNum)
    ).map {
      ClassMember(
        name = studentMapper.selectById(it.stuNum).name,
        num = it.stuNum
      )
    }
  }

  override fun deleteCourse(teaNum: String, classPlanId: Int) {
    val classPlan = courseClassPlanMapper.selectOne(
      KtQueryWrapper(CourseClassPlanEntity::class.java)
        .eq(CourseClassPlanEntity::classPlanId, classPlanId)
        .eq(CourseClassPlanEntity::teaNum, teaNum)
    )
    if (classPlan == null) {
      throw ResponseException("课程不存在")
    } else if (!classPlan.isNewly) {
      throw ResponseException("原课程不允许被删除")
    } else {
      courseClassPlanMapper.deleteById(classPlanId)
      // 发送通知
      val course = courseClassMapper.selectById(classPlan.classNum).let {
        courseMapper.selectById(it.courseNum)
      }
      val time = MinuteTimeDate.now()
      courseClassStuMapper.selectList(
        KtQueryWrapper(CourseClassStuEntity::class.java)
          .eq(CourseClassStuEntity::classNum, classPlan.classNum)
      ).forEach {
        notificationService.addNotification(
          userId = studentMapper.selectById(it.stuNum).userId,
          time = time,
          content = NotificationServerContent.Normal(
            title = "课程被移除通知",
            content = "课程：${course.courseName}\n" +
                "被移除时间段：${
                  CourseUtils.getLessonPeriod(
                    date = classPlan.date,
                    beginLesson = classPlan.beginLesson,
                    length = classPlan.length,
                  )
                }"
          )
        )
      }
    }
  }

  override fun changeCourse(
    teaNum: String,
    classPlanId: Int,
    newDate: Date,
    newBeginLesson: Int,
    newLength: Int,
    newClassroom: String,
  ) {
    val classPlan = courseClassPlanMapper.selectOne(
      KtQueryWrapper(CourseClassPlanEntity::class.java)
        .eq(CourseClassPlanEntity::classPlanId, classPlanId)
        .eq(CourseClassPlanEntity::teaNum, teaNum)
    )
    if (classPlan == null) {
      throw ResponseException("课程不存在")
    } else {
      courseClassPlanMapper.update(
        KtUpdateWrapper(CourseClassPlanEntity::class.java)
          .eq(CourseClassPlanEntity::classPlanId, classPlanId)
          .set(CourseClassPlanEntity::date, newDate)
          .set(CourseClassPlanEntity::beginLesson, newBeginLesson)
          .set(CourseClassPlanEntity::length, newLength)
          .set(CourseClassPlanEntity::classroom, newClassroom)
      )
      // 发送通知
      val course = courseClassMapper.selectById(classPlan.classNum).let {
        courseMapper.selectById(it.courseNum)
      }
      val time = MinuteTimeDate.now()
      courseClassStuMapper.selectList(
        KtQueryWrapper(CourseClassStuEntity::class.java)
          .eq(CourseClassStuEntity::classNum, classPlan.classNum)
      ).forEach {
        notificationService.addNotification(
          userId = studentMapper.selectById(it.stuNum).userId,
          time = time,
          content = NotificationServerContent.Normal(
            title = "调课提醒",
            content = "课程：${course.courseName}\n" +
                "原时间段：${
                  CourseUtils.getLessonPeriod(
                    date = classPlan.date,
                    beginLesson = classPlan.beginLesson,
                    length = classPlan.length,
                  )
                }\n" +
                "新时间段：${
                  CourseUtils.getLessonPeriod(
                    date = newDate,
                    beginLesson = newBeginLesson,
                    length = newLength,
                  )
                }"
          )
        )
      }
    }
  }

  override fun createCourse(
    teaNum: String,
    classNum: String,
    date: Date,
    beginLesson: Int,
    length: Int,
    classroom: String,
  ): Int {
    val courseClass = courseClassMapper.selectById(classNum) ?: throw ResponseException("教学班不存在")
    val entity = CourseClassPlanEntity(
      classPlanId = 0,
      classNum = classNum,
      teaNum = teaNum,
      dateStr = date.toString(),
      beginLesson = beginLesson,
      length = length,
      classroom = classroom,
      isNewly = true,
    )
    courseClassPlanMapper.insert(entity)

    // 发送通知
    val course = courseMapper.selectById(courseClass.courseNum)
    val time = MinuteTimeDate.now()
    courseClassStuMapper.selectList(
      KtQueryWrapper(CourseClassStuEntity::class.java)
        .eq(CourseClassStuEntity::classNum, classNum)
    ).forEach {
      notificationService.addNotification(
        userId = studentMapper.selectById(it.stuNum).userId,
        time = time,
        content = NotificationServerContent.Normal(
          title = "补课提醒",
          content = "课程名：${course.courseName}\n" +
              "新增课程：${
                CourseUtils.getLessonPeriod(
                  date = date,
                  beginLesson = beginLesson,
                  length = length,
                )
              }"
        )
      )
    }
    return entity.classPlanId
  }

  override fun add(data: List<CourseController.Course>) {
    val teacherNumByName = mutableMapOf<String, String>()
    data.forEach { course ->
      if (courseMapper.selectById(course.courseNum) == null) {
        courseMapper.insert(
          CourseEntity(
            courseNum = course.courseNum,
            courseName = course.courseName,
            courseType = course.courseType,
          )
        )
      }
      course.classByClassNum.forEach { (classNum, courseClass) ->
        if (courseClassMapper.selectById(classNum) == null) {
          courseClassMapper.insert(
            CourseClassEntity(
              classNum = classNum,
              courseNum = course.courseNum,
            )
          )
          courseClass.stuNumSet.forEach { stuNum ->
            courseClassStuMapper.insert(
              CourseClassStuEntity(
                classNum = classNum,
                stuNum = stuNum,
              )
            )
          }
          courseClass.classPlanSet.forEach { plan ->
            courseClassPlanMapper.insert(
              CourseClassPlanEntity(
                classPlanId = 0,
                classNum = classNum,
                dateStr = plan.date,
                beginLesson = plan.beginLesson,
                length = plan.length,
                classroom = plan.classroom,
                isNewly = false,
                teaNum = teacherNumByName.getOrPut(plan.teacher) {
                  try {
                    teacherMapper.selectOne(
                      KtQueryWrapper(TeacherEntity::class.java)
                        .eq(TeacherEntity::name, plan.teacher)
                    ).teaNum
                  } catch (e: Exception) {
                    println("courseNum = ${course.courseNum}\n" +
                        "classNum = ${classNum}\n" +
                        "plan = ${plan}\n" +
                        "teacher = ${plan.teacher}")
                    throw e
                  }
                },
              )
            )
          }
        }
      }
    }
  }

  private fun getLessons(num: String): List<LessonBean> {
    return if (NumUtils.isStudent(num)) {
      // 查找教学班
      courseClassStuMapper.selectList(
        KtQueryWrapper(CourseClassStuEntity::class.java)
          .eq(CourseClassStuEntity::stuNum, num)
      ).map {
        // 根据教学班查找教学计划
        it to courseClassPlanMapper.selectList(
          KtQueryWrapper(CourseClassPlanEntity::class.java)
            .eq(CourseClassPlanEntity::classNum, it.classNum)
        )
      }.map { pair ->
        val course = courseClassMapper.selectById(pair.first.classNum).let {
          courseMapper.selectById(it.courseNum)
        }
        LessonBean(
          courseNum = course.courseNum,
          classNum = pair.first.classNum,
          courseName = course.courseName,
          type = course.courseType,
          period = pair.second.transformPeriod(),
        )
      }
    } else if (NumUtils.isTeacher(num)) {
      courseClassPlanMapper.selectList(
        KtQueryWrapper(CourseClassPlanEntity::class.java)
          .eq(CourseClassPlanEntity::teaNum, num)
      ).groupBy { it.classNum }.map { entry ->
        val course = courseClassMapper.selectById(entry.key).let {
          courseMapper.selectById(it.courseNum)
        }
        LessonBean(
          courseNum = course.courseNum,
          classNum = entry.key,
          courseName = course.courseName,
          type = course.courseType,
          period = entry.value.transformPeriod(),
        )
      }
    } else emptyList()
  }

  private fun List<CourseClassPlanEntity>.transformPeriod(): List<LessonPeriod> {
    val teaNumNameMap = mutableMapOf<String, String>()
    return groupBy {
      LessonPeriod(
        dateList = emptyList(),
        beginLesson = it.beginLesson,
        length = it.length,
        classroom = it.classroom,
        teacher = teaNumNameMap.getOrPut(it.teaNum) { teacherMapper.selectById(it.teaNum).name },
      )
    }.map { entry ->
      entry.key.copy(
        dateList = entry.value.map {
          LessonPeriodDate(
            classPlanId = it.classPlanId,
            date = it.date,
            isNewly = it.isNewly,
          )
        }.sortedBy { it.date }
      )
    }
  }
}