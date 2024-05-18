package com.course.pages.course.model

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.time.Today
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.pages.course.api.item.lesson.LessonItemGroup
import com.course.pages.course.api.item.lesson.toLessonItemBean
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.source.app.course.CourseBean
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/3/17 16:53
 */
class CourseDetailController(
  val num: String,
  controllers: ImmutableList<CourseController> = persistentListOf(),
  val onClickItem: ((LessonItemData) -> Unit)?,
) : CourseDetail(controllers) {

  private val itemGroups = LessonItemGroup(onClickItem = onClickItem)
  private val courseBean = mutableStateOf<CourseBean?>(null)

  override val startDate: Date by derivedStateOfStructure {
    courseBean.value?.beginDate ?: Today.firstDate
  }

  private var clickDate by mutableStateOf(Today)

  override val initialClickDate: Date
    get() = clickDate

  override val title: String by derivedStateOfStructure {
    val start = courseBean.value?.beginDate
    if (start == null) "无数据" else getWeekStr(start, clickDate)
  }

  override val subtitle: String by derivedStateOfStructure {
    courseBean.value?.term ?: ""
  }

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    clickDate = date
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        LessonRepository.getCourseBean(num)
          .onEach { bean ->
            courseBean.value = bean
            itemGroups.resetData(bean.toLessonItemBean())
          }.last()
      }.tryThrowCancellationException()
    }
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    itemGroups.Content(weekBeginDate, timeline, scrollState)
  }

  private fun getWeekStr(start: Date, date: Date): String {
    val number = start.daysUntil(date) / 7 + 1
    if (number < 1) return ""
    if (number >= 100) return "第${number}周"
    val a = when (number / 10) {
      1 -> "十"
      2 -> "二十"
      3 -> "三十"
      4 -> "四十"
      5 -> "五十"
      6 -> "六十"
      7 -> "七十"
      8 -> "八十"
      9 -> "九十"
      else -> ""
    }
    val b = when (number % 10) {
      1 -> "一"
      2 -> "二"
      3 -> "三"
      4 -> "四"
      5 -> "五"
      6 -> "六"
      7 -> "七"
      8 -> "八"
      9 -> "九"
      else -> ""
    }
    return "第${a}${b}周"
  }
}