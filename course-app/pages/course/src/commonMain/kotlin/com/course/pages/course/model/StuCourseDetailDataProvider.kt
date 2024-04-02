package com.course.pages.course.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Today
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.api.item.lesson.toCourseItem
import com.course.pages.course.api.item.lesson.toLessonItemBean
import com.course.shared.time.Date
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * .
 *
 * @author 985892345
 * 2024/3/17 16:53
 */
class StuCourseDetailDataProvider(
  val stuNum: String,
  vararg dataProviders: CourseDataProvider
) : CourseDetail(*dataProviders) {

  private val courseBeans = mutableStateListOf<CourseBean>()

  override val startDate: Date by derivedStateOfStructure {
    courseBeans.firstOrNull()?.beginDate ?: Today.firstDate
  }

  private var clickDate by mutableStateOf(Today)

  override val title: String by derivedStateOfStructure {
    val start = courseBeans.firstOrNull { it.beginDate < clickDate }?.beginDate
    if (start == null) "" else getWeekStr(start, clickDate)
  }

  override val subtitle: String by derivedStateOfStructure {
    courseBeans.firstOrNull { it.beginDate < clickDate }?.term ?: ""
  }

  private var prevCourseBeanJob: Job? = null

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    clickDate = date
    val last = courseBeans.last()
    if (last.beginDate.daysUntil(date) < 60) {
      if (prevCourseBeanJob == null && last.termIndex != 0) {
        prevCourseBeanJob = StuLessonRepository
          .getCourseBean(stuNum, last.termIndex - 1)
          .flowOn(Dispatchers.IO)
          .onEach {
            addAll(it.toLessonItemBean().toCourseItem())
            courseBeans.add(it)
          }.launchIn(coroutineScope)
      }
    }
  }

  override fun initProvider(coroutineScope: CoroutineScope) {
    super.initProvider(coroutineScope)
    StuLessonRepository.getCourseBean(stuNum)
      .flowOn(Dispatchers.IO)
      .onEach {
        addAll(it.toLessonItemBean().toCourseItem())
        courseBeans.add(it)
      }.launchIn(coroutineScope)
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