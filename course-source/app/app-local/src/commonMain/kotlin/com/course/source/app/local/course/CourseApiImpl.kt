package com.course.source.app.local.course

import androidx.compose.runtime.snapshotFlow
import com.course.components.utils.preferences.createSettings
import com.course.pages.course.api.IMainCourseController
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
@ImplProvider(clazz = IMainCourseController::class, name = "CourseApiImpl")
object CourseApiImpl : SourceRequest(), CourseApi, IMainCourseController {

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
          beginDate: String // 学期开始日期, 格式为: 2024-04-01
          lessons: [
            {
              courseNum: String, // 课程号
              courseName: String, // 课程名
              teacher: String, // 老师名字
              type: String, // 课程类型，选修或必修
              period: [
                {
                  periodId: Int,
                  date: String, // 上课时间，格式为: 2024-04-01 09:00:00
                  beginLesson: Int, // 开始节数，如：1、2 节课以 1 开始
                  length: Int, // 课的长度
                  classroom: String, // 教室
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
    ),
    """
      // 只加载当前学期
      // 返回以下 json 格式，如果无数据，则返回 null
      [
        {
          id: String, // 唯一 id
          zIndex: Float, // 显示的层级，数字越大，越显示在上面；日程为 0，课程层级为 10，考试为 20
          startTime: String, // 开始时间，格式为: 2024-04-01 08:00
          minuteDuration: Int, // 持续时间，单位为分钟
          backgroundColor: String, // 背景色，如："FF123456"
          topText: String, // 顶部文本
          topTextColor: String, // 顶部文本颜色，如："FF123456"
          bottomText: String, // 底部文本
          bottomTextColor: String, // 底部文本颜色，如："FF123456"
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

  override suspend fun getCourseBean(num: String): ResponseWrapper<CourseBean> {
    if (courseBeanRequest.requestUnits.isEmpty()) {
      // 如果未设置请求体，则挂起直到设置后才返回
      snapshotFlow { courseBeanRequest.requestUnits.toList() }.first { it.isNotEmpty() }
    }
    val data = courseBeanRequest.request(false, true, num)
    return if (data != null) ResponseWrapper.success(data) else ResponseWrapper.failure(
      -1,
      "数据源无数据"
    )
  }

  override fun createCourseController(account: AccountBean?): List<CourseController> {
    return listOf(SourceCourseController(account))
  }

  init {
    CourseApiInjector.init(courseBeanRequest)
  }
}
