package com.course.source.app.local.exam

import androidx.compose.runtime.snapshotFlow
import com.course.source.app.exam.ExamApi
import com.course.source.app.exam.ExamTermBean
import com.course.source.app.local.request.SourceRequest
import com.course.source.app.response.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.flow.first

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 17:13
 */
@ImplProvider(clazz = ExamApi::class)
@ImplProvider(clazz = SourceRequest::class, name = "ExamApiImpl")
object ExamApiImpl : SourceRequest(), ExamApi {

  private val examTermBeansRequest by requestContent<List<ExamTermBean>>(
    key = "exam",
    name = "考试",
    linkedMapOf(
      "stuNum" to "学号",
    ),
    """
      // 只加载当前学期考试
      // 返回以下 json 格式，如果无数据，则返回 null
      [
        {
          term: String, // 学期
          termIndex: Int, // 学期索引，从 0 开始
          beginDate: String, // 学期开始日期, 格式为: 2024-04-01
          exams: [
            {
              startTime: String // 开始时间，格式为: 2024-04-01 08:00
              minuteDuration: Int, // 持续时间，单位为分钟
              course: String, // 课程名称
              courseNum: String, // 课程编号
              classroom: String, // 教室（缩写）
              type: String, // 考试类型
              seat: String, // 座位
            }
          ]
        }
      ]
      
    """.trimIndent()
  )

  override suspend fun getExam(stuNum: String): ResponseWrapper<List<ExamTermBean>> {
    if (examTermBeansRequest.requestUnits.isEmpty()) {
      // 如果未设置请求体，则挂起直到设置后才返回
      snapshotFlow { examTermBeansRequest.requestUnits.toList() }.first { it.isNotEmpty() }
    }
    val data = examTermBeansRequest.request(
      false,
      true,
      stuNum,
    )
    return if (data != null) ResponseWrapper.success(data) else ResponseWrapper.failure(
      -1,
      "数据源无数据"
    )
  }
}