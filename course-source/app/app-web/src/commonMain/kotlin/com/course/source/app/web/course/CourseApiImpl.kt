package com.course.source.app.web.course

import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.data.CourseDataProvider
import com.course.source.app.account.AccountBean
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
object CourseApiImpl : SourceRequest(), CourseApi, IMainCourseDataProvider {

  private val courseBeanRequest by requestContent<CourseBean>(
    key = "course",
    name = "课程",
    linkedMapOf(
      "stuNum" to "学号",
      "term" to "学期，大一上为0，负数表示当前学期"
    ),
    """
      // 返回以下 json 格式，如果无数据，则返回 null
      {
        {
          beginDate: String // 开始日期
          term: String // 学期
          termIndex: Int // 学期索引，大一上为0
          lessons: [
            {
              id: Int, // 课程唯一 id，即使课程号相同，但上课时间不同，应返回不同的 id
              course: String, // 课程名
              classroom: String, // 教室
              teacher: String, // 老师名字
              courseNum: String, // 课程号
              rawWeek: String, // 周期，比如：1-8周
              type: String, // 选修或者必修
              weeks: List<Int>, // 在哪几周上课
              dayOfWeek: String, // 星期数，英文单词，如：MONDAY
              beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
              length: Int, // 课的长度
            }
          ]
        }
      }
      
    """.trimIndent()
  )

  private val courseRequestGroup by requestGroup<CourseBean>(
    key = "course-custom",
    name = "自定义课表",
    linkedMapOf("stuNum" to "学号"),
    """
      // 返回以下 json 格式，如果无数据，则返回 null
      [
        {
          
        }
      ]
    """.trimIndent()
  )

  override suspend fun getCourseBean(
    stuNum: String,
    termIndex: Int,
  ): ResponseWrapper<CourseBean> {
    val data = courseBeanRequest.request(true, stuNum, termIndex.toString())
    return if (data != null) ResponseWrapper.success(data) else ResponseWrapper.failure(
      -1,
      "数据源无数据"
    )
  }

  override fun createCourseDataProviders(account: AccountBean?): List<CourseDataProvider> {
    return emptyList()
  }
}
