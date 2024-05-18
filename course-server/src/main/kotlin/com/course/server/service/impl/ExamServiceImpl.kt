package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.course.server.controller.ExamController
import com.course.server.entity.ExamEntity
import com.course.server.entity.ExamStuEntity
import com.course.server.mapper.CourseMapper
import com.course.server.mapper.ExamMapper
import com.course.server.mapper.ExamStuMapper
import com.course.server.mapper.StudentMapper
import com.course.server.service.ExamService
import com.course.server.utils.SchoolCalendar
import com.course.shared.time.MinuteTimeDateSerializer
import com.course.source.app.exam.ExamBean
import com.course.source.app.exam.ExamTermBean
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 21:45
 */
@Service
class ExamServiceImpl(
  private val studentMapper: StudentMapper,
  private val courseMapper: CourseMapper,
  private val examMapper: ExamMapper,
  private val examStuMapper: ExamStuMapper,
) : ExamService {

  override fun getExam(
    stuNum: String,
  ): ExamTermBean {
    return ExamTermBean(
      term = SchoolCalendar.getTermStr(studentMapper.selectById(stuNum).entryYear) ?: "未知学期",
      beginDate = SchoolCalendar.getBeginDate(),
      exams = examStuMapper.selectList(
        KtQueryWrapper(ExamStuEntity::class.java)
          .eq(ExamStuEntity::stuNum, stuNum)
      ).map {
        val exam = examMapper.selectOne(
          KtQueryWrapper(ExamEntity::class.java)
            .eq(ExamEntity::courseNum, it.courseNum)
            .eq(ExamEntity::examType, it.examType)
        )
        val course = courseMapper.selectById(it.courseNum)
        ExamBean(
          startTime = exam.startTime,
          minuteDuration = exam.minuteDuration,
          courseName = course.courseName,
          classroom = it.classroom,
          seat = it.seat,
          examType = it.examType,
        )
      }
    )
  }

  override fun addExam(exams: List<ExamController.AddExam>) {
    exams.forEach { addExam ->
      addExam.exams.forEach {
        if (examMapper.selectOne(
            KtQueryWrapper(ExamEntity::class.java)
              .eq(ExamEntity::courseNum, it.courseNum)
              .eq(ExamEntity::examType, it.type)
          ) == null
        ) {
          examMapper.insert(
            ExamEntity(
              courseNum = it.courseNum,
              examType = it.type,
              startTimeStr = MinuteTimeDateSerializer.deserialize(it.startTime).toString(),
              minuteDuration = it.minuteDuration,
            )
          )
        }
        examStuMapper.insert(
          ExamStuEntity(
            courseNum = it.courseNum,
            examType = it.type,
            stuNum = addExam.stuNum,
            classroom = it.classroom,
            seat = it.seat,
          )
        )
      }
    }
  }
}