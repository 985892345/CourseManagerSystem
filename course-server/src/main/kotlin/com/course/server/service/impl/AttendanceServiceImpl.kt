package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.course.server.entity.*
import com.course.server.mapper.*
import com.course.server.service.AttendanceService
import com.course.server.service.NotificationService
import com.course.server.utils.CourseUtils
import com.course.server.utils.ResponseException
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.attendance.*
import com.course.source.app.course.getStartMinuteTime
import com.course.source.app.notification.DecisionBtn
import com.course.source.app.notification.NotificationContent
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:51
 */
@Service
class AttendanceServiceImpl(
  private val attendanceStuMapper: AttendanceStuMapper,
  private val attendanceCodeMapper: AttendanceCodeMapper,
  private val attendanceLeaveMapper: AttendanceLeaveMapper,
  private val courseMapper: CourseMapper,
  private val courseClassMapper: CourseClassMapper,
  private val courseClassStuMapper: CourseClassStuMapper,
  private val courseClassPlanMapper: CourseClassPlanMapper,
  private val notificationService: NotificationService,
  private val studentMapper: StudentMapper,
  private val teacherMapper: TeacherMapper,
) : AttendanceService {

  override fun publishAttendanceCode(
    teaNum: String,
    classPlanId: Int,
    code: String,
    duration: Int,
    lateDuration: Int,
  ) {
    // 检查是否是当前老师上的这节课
    if (courseClassPlanMapper.selectOne(
        KtQueryWrapper(CourseClassPlanEntity::class.java)
          .eq(CourseClassPlanEntity::classPlanId, classPlanId)
          .eq(CourseClassPlanEntity::teaNum, teaNum)
      ) == null
    ) {
      throw ResponseException("权限不足")
    }
    // 检查考勤码是否重复
    val entity = attendanceCodeMapper.selectOne(
      KtQueryWrapper(AttendanceCodeEntity::class.java)
        .eq(AttendanceCodeEntity::classPlanId, classPlanId)
        .eq(AttendanceCodeEntity::code, code)
    )
    if (entity != null) {
      throw ResponseException("考勤码重复")
    }
    // 发布考勤
    val publishTimestamp = System.currentTimeMillis()
    val lateTimestamp = System.currentTimeMillis() + duration * 1000
    val finishTimestamp = lateTimestamp + lateDuration * 1000
    attendanceCodeMapper.insert(
      AttendanceCodeEntity(
        classPlanId = classPlanId,
        code = code,
        publishTimestamp = publishTimestamp,
        lateTimestamp = lateTimestamp,
        finishTimestamp = finishTimestamp,
      )
    )
  }

  override fun getAttendanceStudent(classPlanId: Int): List<AttendanceStudentList> {
    return attendanceCodeMapper.selectList( // 存在一节课发布多次考勤
      KtQueryWrapper(AttendanceCodeEntity::class.java)
        .eq(AttendanceCodeEntity::classPlanId, classPlanId)
    ).map { code ->
      val attendance = mutableListOf<AttendanceStudent>()
      val late = mutableListOf<AttendanceStudent>()
      val absent = mutableListOf<AttendanceStudent>()
      val askForLeave = mutableListOf<AttendanceStudent>()
      val stuNumSet = mutableSetOf<String>()
      // 申请了请假的
      attendanceLeaveMapper.selectList(
        KtQueryWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::classPlanId, classPlanId)
          .eq(AttendanceLeaveEntity::statusStr, AskForLeaveStatus.Approved.name)
      ).forEach {
        stuNumSet.add(it.stuNum)
        askForLeave.add(
          AttendanceStudent(
            stuNum = it.stuNum,
            name = studentMapper.selectById(it.stuNum).name,
            status = AttendanceStatus.AskForLeave
          )
        )
      }
      // 出席、迟到、缺勤、请假（包含后期修改）
      attendanceStuMapper.selectList(
        KtQueryWrapper(AttendanceStuEntity::class.java)
          .eq(AttendanceStuEntity::classPlanId, classPlanId)
          .eq(AttendanceStuEntity::code, code.code)
          .run {
            if (stuNumSet.isNotEmpty()) notIn(AttendanceStuEntity::stuNum, stuNumSet) else this
          }
      ).forEach {
        stuNumSet.add(it.stuNum)
        val student = AttendanceStudent(
          stuNum = it.stuNum,
          name = studentMapper.selectById(it.stuNum).name,
          status = it.attendanceStatus,
        )
        when (it.attendanceStatus) {
          AttendanceStatus.Attendance -> attendance.add(student)
          AttendanceStatus.Absent -> absent.add(student)
          AttendanceStatus.Late -> late.add(student) // 签到了但是已经算缺席
          AttendanceStatus.AskForLeave -> askForLeave.add(student)
        }
      }
      // 未签到
      // 通过当前课程教学班所有上课人数减去已有结果的就是缺勤人数
      courseClassPlanMapper.selectById(classPlanId).let { classPlan ->
        courseClassStuMapper.selectList(
          KtQueryWrapper(CourseClassStuEntity::class.java)
            .eq(CourseClassStuEntity::classNum, classPlan.classNum)
            .run {
              if (stuNumSet.isNotEmpty()) notIn(CourseClassStuEntity::stuNum, stuNumSet) else this
            }
        )
      }.forEach {
        absent.add(
          AttendanceStudent(
            stuNum = it.stuNum,
            name = studentMapper.selectById(it.stuNum).name,
            status = AttendanceStatus.Absent,
          )
        )
      }
      AttendanceStudentList(
        publishTimestamp = code.publishTimestamp,
        code = code.code,
        students = attendance + late + absent + askForLeave,
      )
    }
  }

  override fun changeAttendanceStatus(
    teaNum: String,
    classPlanId: Int,
    code: String,
    stuNum: String,
    status: AttendanceStatus,
  ) {
    // 检查是否是当前老师上的这节课
    if (courseClassPlanMapper.selectOne(
        KtQueryWrapper(CourseClassPlanEntity::class.java)
          .eq(CourseClassPlanEntity::classPlanId, classPlanId)
          .eq(CourseClassPlanEntity::teaNum, teaNum)
      ) == null
    ) {
      throw ResponseException("权限不足")
    }
    // 如果该学生本来就已经请假了，则不能进行修改
    if (attendanceLeaveMapper.selectOne(
        KtQueryWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::classPlanId, classPlanId)
          .eq(AttendanceLeaveEntity::stuNum, stuNum)
          .eq(AttendanceLeaveEntity::statusStr, AskForLeaveStatus.Approved.name)
      ) != null
    ) {
      throw ResponseException("该学生已经请假，不能进行修改")
    }
    // 检查是否发布了这个考勤
    val attendanceCode = attendanceCodeMapper.selectOne(
      KtQueryWrapper(AttendanceCodeEntity::class.java)
        .eq(AttendanceCodeEntity::classPlanId, classPlanId)
        .eq(AttendanceCodeEntity::code, code)
    )
    if (attendanceCode == null) {
      throw ResponseException("考勤不存在")
    }
    // 查找是否存在
    if (attendanceStuMapper.selectOne(
        KtQueryWrapper(AttendanceStuEntity::class.java)
          .eq(AttendanceStuEntity::classPlanId, classPlanId)
          .eq(AttendanceStuEntity::code, code)
          .eq(AttendanceStuEntity::stuNum, stuNum)
      ) != null
    ) {
      // 更新考勤
      attendanceStuMapper.update(
        KtUpdateWrapper(AttendanceStuEntity::class.java)
          .eq(AttendanceStuEntity::classPlanId, classPlanId)
          .eq(AttendanceStuEntity::code, code)
          .eq(AttendanceStuEntity::stuNum, stuNum)
          .set(AttendanceStuEntity::status, status.name)
          .set(AttendanceStuEntity::isModified, true)
      )
    } else {
      // 插入考勤
      attendanceStuMapper.insert(
        AttendanceStuEntity(
          classPlanId = classPlanId,
          code = code,
          stuNum = stuNum,
          timestamp = attendanceCode.publishTimestamp,
          status = status.name,
          isModified = true,
        )
      )
    }
  }


  // 学生

  override fun postAttendanceCode(classPlanId: Int, code: String, stuNum: String): AttendanceCodeStatus {
    // 检查是否是当前学生上的这节课
    val classPlan = courseClassPlanMapper.selectById(classPlanId)
    if (courseClassStuMapper.selectOne(
        KtQueryWrapper(CourseClassStuEntity::class.java)
          .eq(CourseClassStuEntity::classNum, classPlan.classNum)
          .eq(CourseClassStuEntity::stuNum, stuNum)
      ) == null
    ) {
      throw ResponseException("权限不足")
    }
    // 检查是否已请假
    if (attendanceLeaveMapper.selectOne(
        KtQueryWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::classPlanId, classPlanId)
          .eq(AttendanceLeaveEntity::stuNum, stuNum)
      )?.status == AskForLeaveStatus.Approved
    ) {
      throw ResponseException("当前课程已请假，无法签到")
    }
    // 查询已经发布的考勤码
    val attendanceCode = attendanceCodeMapper.selectOne(
      KtQueryWrapper(AttendanceCodeEntity::class.java)
        .eq(AttendanceCodeEntity::classPlanId, classPlanId)
        .eq(AttendanceCodeEntity::code, code)
    )
    if (attendanceCode == null) return AttendanceCodeStatus.Invalid // 未查询到考勤码
    // 插入签到信息
    val now = System.currentTimeMillis()
    attendanceStuMapper.insert(
      AttendanceStuEntity(
        classPlanId = classPlanId,
        code = code,
        stuNum = stuNum,
        timestamp = System.currentTimeMillis(),
        status = when {
          now <= attendanceCode.lateTimestamp -> AttendanceStatus.Attendance
          now <= attendanceCode.finishTimestamp -> AttendanceStatus.Late
          else -> AttendanceStatus.Absent
        }.name,
        isModified = false,
      )
    )
    return when {
      now <= attendanceCode.lateTimestamp -> AttendanceCodeStatus.Success
      now <= attendanceCode.finishTimestamp -> AttendanceCodeStatus.Late
      else -> AttendanceCodeStatus.Absent
    }
  }

  override fun getAttendanceHistory(classNum: String, stuNum: String): List<AttendanceHistory> {
    return courseClassPlanMapper.selectList( // 查找当前教学班所有教学计划
      KtQueryWrapper(CourseClassPlanEntity::class.java)
        .eq(CourseClassPlanEntity::classNum, classNum)
    ).flatMap { classPlan ->
      val list = mutableListOf<AttendanceHistory>()
      val attendanceLeave = attendanceLeaveMapper.selectOne(
        KtQueryWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::classPlanId, classPlan.classPlanId)
          .eq(AttendanceLeaveEntity::stuNum, stuNum)
      )
      // 检查是否已请假
      if (attendanceLeave?.status == AskForLeaveStatus.Approved) {
        list.add(
          AttendanceHistory(
            date = classPlan.date,
            beginLesson = classPlan.beginLesson,
            length = classPlan.length,
            publishTimestamp = 0,
            timestamp = attendanceLeave.timestamp,
            status = AttendanceStatus.AskForLeave,
            isModified = false
          )
        )
      } else {
        // 未请假时查询当前课程计划发布的所有考勤码
        attendanceCodeMapper.selectList(
          KtQueryWrapper(AttendanceCodeEntity::class.java)
            .eq(AttendanceCodeEntity::classPlanId, classPlan.classPlanId)
        ).forEach { attendanceCode ->
          // 查询当前考勤码是否有考勤记录
          val attendanceStu = attendanceStuMapper.selectOne(
            KtQueryWrapper(AttendanceStuEntity::class.java)
              .eq(AttendanceStuEntity::classPlanId, classPlan.classPlanId)
              .eq(AttendanceStuEntity::code, attendanceCode.code)
              .eq(AttendanceStuEntity::stuNum, stuNum)
          )
          if (attendanceStu != null) {
            list.add(
              AttendanceHistory(
                date = classPlan.date,
                beginLesson = classPlan.beginLesson,
                length = classPlan.length,
                publishTimestamp = attendanceCode.publishTimestamp,
                timestamp = attendanceStu.timestamp,
                status = attendanceStu.attendanceStatus,
                isModified = attendanceStu.isModified,
              )
            )
          } else {
            // 没有记录就是缺勤
            list.add(
              AttendanceHistory(
                date = classPlan.date,
                beginLesson = classPlan.beginLesson,
                length = classPlan.length,
                publishTimestamp = attendanceCode.publishTimestamp,
                timestamp = 0,
                status = AttendanceStatus.Absent,
                isModified = false,
              )
            )
          }
        }
      }
      list
    }
  }

  override fun getAskForLeaveHistory(classNum: String, stuNum: String): List<AskForLeaveHistory> {
    return courseClassPlanMapper.selectList( // 查询当前教学班所有教学计划
      KtQueryWrapper(CourseClassPlanEntity::class.java)
        .eq(CourseClassPlanEntity::classNum, classNum)
    ).flatMap { classPlan ->
      attendanceLeaveMapper.selectList(
        KtQueryWrapper(AttendanceLeaveEntity::class.java)
          .eq(AttendanceLeaveEntity::classPlanId, classPlan.classPlanId)
          .eq(AttendanceLeaveEntity::stuNum, stuNum)
      ).map {
        AskForLeaveHistory(
          date = classPlan.date,
          beginLesson = classPlan.beginLesson,
          length = classPlan.length,
          reason = it.reason,
          status = it.status,
        )
      }
    }
  }

  override fun postAskForLeave(classPlanId: Int, stuNum: String, reason: String) {
    val classPlan = courseClassPlanMapper.selectById(classPlanId)
    val expiredTimestamp = MinuteTimeDate(
      classPlan.date,
      getStartMinuteTime(classPlan.beginLesson)
    ).toEpochMilliseconds()
    val now = System.currentTimeMillis()
    if (now >= expiredTimestamp) {
      throw ResponseException("无法请假已上或正在上的课程")
    }
    val old = attendanceLeaveMapper.selectList(
      KtQueryWrapper(AttendanceLeaveEntity::class.java)
        .eq(AttendanceLeaveEntity::classPlanId, classPlanId)
        .eq(AttendanceLeaveEntity::stuNum, stuNum)
    )
    if (old.any { it.status == AskForLeaveStatus.Pending }) {
      throw ResponseException("已存在相同请假申请")
    }
    if (old.any { it.status == AskForLeaveStatus.Approved }) {
      throw ResponseException("该课程已请假成功")
    }
    val courseClass = courseClassMapper.selectById(classPlan.classNum)
    val course = courseMapper.selectById(courseClass.courseNum)
    val teacher = teacherMapper.selectById(classPlan.teaNum)
    val student = studentMapper.selectById(stuNum)
    val lessonPeriod = CourseUtils.getLessonPeriod(
      date = classPlan.date,
      beginLesson = classPlan.beginLesson,
      length = classPlan.length,
      courseName = course.courseName,
    )

    val attendanceLeave = AttendanceLeaveEntity(
      leaveId = 0,
      classPlanId = classPlanId,
      stuNum = stuNum,
      reason = reason,
      timestamp = System.currentTimeMillis(),
      statusStr = AskForLeaveStatus.Pending.name,
    )
    attendanceLeaveMapper.insert(attendanceLeave)

    // 发送通知
    notificationService.addNotification(
      userId = teacher.userId,
      time = MinuteTimeDate.now(),
      content = NotificationServerContent.Decision(
        clientContent = NotificationContent.Decision(
          title = "学生请假申请",
          content = "申请人：${student.name}\n" +
              "请假课程：$lessonPeriod\n" +
              "请假原因：$reason",
          btn = DecisionBtn.Pending(
            positiveText = "已同意",
            negativeText = "已驳回",
            negativeDialog = "确定驳回该申请吗？",
          ),
        ),
        responseUserId = student.userId,
        positiveResponse = NotificationServerContent.Normal(
          title = "请假已审批通过",
          content = "请假课程：$lessonPeriod"
        ),
        negativeResponse = NotificationServerContent.Normal(
          title = "请假审批未通过",
          content = "请假课程：$lessonPeriod"
        ),
        expiredText = "已过期",
        expiredTimestamp = expiredTimestamp,
        decisionType = DecisionType.AskForLeave(
          leaveId = attendanceLeave.leaveId
        ),
      ),
    )
  }
}