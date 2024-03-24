package com.course.source.app.web.course

import com.course.shared.course.Terms
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.web.request.SourceRequest
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:22
 */
@ImplProvider(clazz = CourseApi::class)
@ImplProvider(clazz = SourceRequest::class, name = "CourseApiImpl")
object CourseApiImpl : SourceRequest(), CourseApi {

  private val courseBeanRequest by requestContent<CourseBean>(
    "课表",
    linkedMapOf(
      "stuNum" to "学号",
      "term" to "学期，大一上为0，负数表示当前学期"
    ),
    """
      // 返回 json 格式
      {
        beginDate: String // 开始日期
        term: Int // 学期，大一上为0
        lessons: [
          {
            id: Int, // 课程唯一 id，即使课程号相同，但上课时间不同，应返回不同的 id
            lessonName: String, // 课程名
            classroom: String, // 教室
            teacher: String, // 老师名字
            lessonNum: String, // 课程号
            cycle: String, // 周期，比如：1-8周
            type: String, // 选修或者必修
            weeks: List<Int>, // 在哪几周上课
            dayOfWeek: String, // 星期数，英文单词，如：MONDAY
            beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
            length: Int, // 课的长度
          }
        ]
      }
    """.trimIndent()
  )

  override suspend fun getCourseBean(
    stuNum: String,
    term: Terms?,
  ): ResponseWrapper<CourseBean> {
    return ResponseWrapper.success(
      courseBeanRequest.request(true, stuNum, (term?.ordinal ?: -1).toString()),
    )
  }
}
