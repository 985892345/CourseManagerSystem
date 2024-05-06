package com.course.source.app.local.course

import androidx.compose.runtime.snapshotFlow
import com.course.components.utils.preferences.createSettings
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.controller.CourseController
import com.course.source.app.account.AccountBean
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.course.source.app.local.request.SourceRequest
import com.course.source.app.response.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.flow.first

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:22
 */
@ImplProvider(clazz = CourseApi::class)
@ImplProvider(clazz = SourceRequest::class, name = "CourseApiImpl")
@ImplProvider(clazz = IMainCourseDataProvider::class, name = "CourseApiImpl")
object CourseApiImpl : SourceRequest(), CourseApi, IMainCourseDataProvider {

  private val courseBeanRequest by requestContent<CourseBean>(
    key = "course",
    name = "课程",
    linkedMapOf(
      "stuNum" to "学号",
    ),
    """
      // 只加载当前学期课程
      // 返回以下 json 格式，如果无数据，则返回 null
      {
        {
          term: String // 学期
          termIndex: Int // 学期索引，大一上为0，不能返回负数
          beginDate: String // 学期开始日期, 格式为: 2024-04-01
          lessons: [
            {
              course: String, // 课程名
              classroom: String, // 教室
              classroomSimplify: String, // 教室简写，用于 item 显示
              teacher: String, // 老师名字
              courseNum: String, // 课程号
              weeks: List<Int>, // 在哪几周上课
              dayOfWeek: String, // 星期数，英文单词，如：MONDAY
              beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
              length: Int, // 课的长度
              showOptions: [ // 点击后课程详细的展示选项
                {
                  first: String,
                  second: String,
                }
              ]
            }
          ]
        }
      }
      
    """.trimIndent()
  )

  internal val courseRequestGroup by requestGroup<List<SourceCourseItemData>>(
    key = "course-custom",
    name = "自定义课表",
    linkedMapOf(
      "stuNum" to "学号，若无学号则为空串",
      "beginDate" to "当前学期的开始日期，格式为 2024-04-01",
    ),
    """
      // 只加载当前学期
      // 返回以下 json 格式，如果无数据，则返回 null
      [
        {
          id: String, // 唯一 id
          zIndex: Float, // 显示的层级，数字越大，越显示在上面；课程层级为 0，考试为 3
          startTime: String, // 开始时间，格式为: 2024-04-01 08:00
          minuteDuration: Int, // 持续时间，单位为分钟
          backgroundColor: Long, // 背景色，如："FF123456" (字符串形式)
          topText: String, // 顶部文本
          topTextColor: Long, // 顶部文本颜色，如："FF123456" (字符串形式)
          bottomText: String, // 底部文本
          bottomTextColor: Long, // 底部文本颜色，如："FF123456" (字符串形式)
          title: String?, // 标题，为 null 时取 topText
          description: String?, // 描述，为 null 时不显示
          showOptions: [ // 点击后详细的展示选项
            {
              first: String,
              second: String,
            }
          ]
        }
      ]
    """.trimIndent()
  )

  private val settings = createSettings("SourceRequest-course")

  override suspend fun getCourseBean(
    stuNum: String,
    termIndex: Int,
  ): ResponseWrapper<CourseBean> {
    if (termIndex >= 0) {
      // 只加载当前学期的课程，对大于 0 的进行判断是否为当前学期
      val nowTermIndex = settings.getInt(stuNum, -1)
      if (termIndex != nowTermIndex) {
        return ResponseWrapper.failure(-2, "数据源只被允许加载当前学期数据")
      }
    }
    if (courseBeanRequest.requestUnits.isEmpty()) {
      // 如果未设置请求体，则挂起直到设置后才返回
      snapshotFlow { courseBeanRequest.requestUnits.toList() }.first { it.isNotEmpty() }
    }
    val data = courseBeanRequest.request(false, true, stuNum)
    if (data != null && termIndex < 0) {
      // 更新当前学期数
      settings.putInt(stuNum, data.termIndex)
    }
    return if (data != null) ResponseWrapper.success(data) else ResponseWrapper.failure(
      -1,
      "数据源无数据"
    )
  }

  override fun createCourseDataProviders(account: AccountBean?): List<CourseController> {
    return listOf(SourceCourseController(account))
  }
}
