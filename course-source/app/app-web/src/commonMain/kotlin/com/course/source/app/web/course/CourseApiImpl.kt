package com.course.source.app.web.course

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ColorArgbSerializable
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.CourseItemClickShow
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.api.item.TopBottomText
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountBean
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.web.request.SourceRequest
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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

  private val courseRequestGroup by requestGroup<List<SourceCardCourseItem>>(
    key = "course-custom",
    name = "自定义课表",
    linkedMapOf(
      "stuNum" to "学号，若无学号则为空串",
      "startDate" to "开始日期，格式为 2024-04-01",
    ),
    """
      // 返回以下 json 格式，如果无数据，则返回 null
      [
        {
          startTime: String, // 开始时间，格式为: 2024-04-01 08:00
          minuteDuration: Int, // 持续时间，单位为分钟
          rank: Int, // 数字越大，优先级越高，优先显示在上面
          itemKey: String, // item 唯一的 key 值，用于定位 item 在当前周内是否发生移动
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

  override suspend fun getCourseBean(
    stuNum: String,
    termIndex: Int,
  ): ResponseWrapper<CourseBean> {
    if (courseBeanRequest.requestUnits.isEmpty()) {
      // 如果未设置请求体，则挂起直到设置后才返回
      snapshotFlow { courseBeanRequest.requestUnits.toList() }.first { it.isNotEmpty() }
    }
    val data = courseBeanRequest.request(false, termIndex == -1, stuNum, termIndex.toString())
    return if (data != null) ResponseWrapper.success(data) else ResponseWrapper.failure(
      -1,
      "数据源无数据"
    )
  }

  override fun createCourseDataProviders(account: AccountBean?): List<CourseDataProvider> {
    return listOf(SourceCourseDataProvider(account))
  }

  private class SourceCourseDataProvider(
    val account: AccountBean?
  ) : CourseDataProvider() {

    private var oldItems = emptyList<SourceCardCourseItem>()
    private var requestJob: Job? = null

    private var isFirstCallbackChangedStartDate = true

    override fun onChangedStartDate(startDate: Date) {
      super.onChangedStartDate(startDate)
      if (isFirstCallbackChangedStartDate) {
        // 第一次加载课表数据时 startDate 不为学期开始日期，所以不进行请求
        isFirstCallbackChangedStartDate = false
        return
      }
      requestJob?.cancel()
      requestJob = coroutineScope.launch(Dispatchers.IO) {
        runCatching {
          courseRequestGroup.request(false, true, account?.num ?: "", startDate.toString())
        }.tryThrowCancellationException().onSuccess { map ->
          removeAll(oldItems)
          oldItems = map.map { entry ->
            entry.value.map {
              it.copy(itemKey = "${entry.key.key}-${it.itemKey}")
            }
          }.flatten()
          addAll(oldItems)
        }
      }
    }
  }

  @Serializable
  private data class SourceCardCourseItem(
    override val startTime: MinuteTimeDate,
    override val minuteDuration: Int,
    override val rank: Int,
    override val itemKey: String,
    @Serializable(ColorArgbSerializable::class)
    val backgroundColor: Color,
    val topText: String,
    @Serializable(ColorArgbSerializable::class)
    val topTextColor: Color,
    val bottomText: String,
    @Serializable(ColorArgbSerializable::class)
    val bottomTextColor: Color,
    val title: String? = null,
    val description: String? = null,
    val showOptions: List<Pair<String, String>>,
  ) : ICourseItem {

    @Composable
    override fun Content(itemClickShow: CourseItemClickShow) {
      CardContent(backgroundColor) {
        Box(modifier = Modifier.clickable {
          clickItem(itemClickShow)
        }) {
          TopBottomText(
            top = topText,
            topColor = topTextColor,
            bottom = bottomText,
            bottomColor = bottomTextColor,
          )
        }
      }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun clickItem(itemClickShow: CourseItemClickShow) {
      itemClickShow.showItemDetail(this) {
        Box(
          modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
          Column {
            Text(
              modifier = Modifier.basicMarquee(),
              text = title ?: topText,
              fontSize = 22.sp,
              color = LocalAppColors.current.tvLv2,
              fontWeight = FontWeight.Bold,
            )
            if (description != null) {
              Text(
                modifier = Modifier.basicMarquee(),
                text = description,
                fontSize = 13.sp,
                color = LocalAppColors.current.tvLv2,
              )
            }
            showOptions.fastForEach {
              Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Text(
                  modifier = Modifier.align(Alignment.TopStart),
                  text = it.first,
                  fontSize = 15.sp,
                  color = LocalAppColors.current.tvLv2,
                )
                Text(
                  modifier = Modifier.align(Alignment.TopEnd),
                  text = it.second,
                  fontSize = 15.sp,
                  color = LocalAppColors.current.tvLv2,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}
