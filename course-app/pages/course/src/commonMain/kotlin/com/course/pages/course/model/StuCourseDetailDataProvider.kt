package com.course.pages.course.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.time.Today
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.api.item.lesson.toCourseItem
import com.course.pages.course.api.item.lesson.toLessonItemBean
import com.course.shared.time.Date
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

  private val courseBeans = mutableStateMapOf<Int, CourseBean>()
  private val courseItems = mutableMapOf<Int, List<ICourseItem>>()

  override val startDate: Date by derivedStateOfStructure {
    courseBeans.minByOrNull { it.key }?.value?.beginDate ?: Today.firstDate
  }

  private var clickDate by mutableStateOf(Today)

  override val initialClickDate: Date
    get() = clickDate

  override fun getTerms(): List<Pair<Int, Date>> {
    return courseBeans.toMap().map { it.key to it.value.beginDate }.sortedBy { -it.first }
  }

  override val title: String by derivedStateOfStructure {
    val start = getClickCourseBean()?.beginDate
    if (start == null) "无数据" else getWeekStr(start, clickDate)
  }

  override val subtitle: String by derivedStateOfStructure {
    getClickCourseBean()?.term ?: ""
  }

  private fun getClickCourseBean(): CourseBean? {
    return courseBeans.minByOrNull {
      if (it.value.beginDate <= clickDate) {
        it.value.beginDate.daysUntil(clickDate)
      } else Int.MAX_VALUE
    }?.value
  }

  private var prevCourseBeanJob: Job? = null

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    if (clickDate != date) {
      clickDate = date
      tryRequestPrevTerm()
    }
  }

  private fun tryRequestPrevTerm() {
    val clickCourseBean = getClickCourseBean()
    if (courseBeans.toMap().isNotEmpty() && // 确保已经加载了第一次的数据
      clickCourseBean != null &&
      clickCourseBean.beginDate.daysUntil(clickDate) < 60 && // 距离开始日期小于 60 天
      clickCourseBean.termIndex != 0 && // 不是第一学期
      !courseBeans.toMap().containsKey(clickCourseBean.termIndex - 1) && // 未加载上一个学期的课程时
      prevCourseBeanJob == null // 未处于加载状态
    ) {
      // 加载上一个学期的课程
      prevCourseBeanJob = coroutineScope.launch {
        runCatching {
          StuLessonRepository.getCourseBean(stuNum, clickCourseBean.termIndex - 1)
            .flowOn(Dispatchers.IO)
            .onEach { bean ->
              setCourse(bean)
            }.last()
        }.tryThrowCancellationException().onSuccess {
          prevCourseBeanJob = null // 加载成功时取消加载状态，下一次加载时就是加载上上个学期的数据
          onChangedTermIndex(it.termIndex, it.beginDate)
        }
      }
      onRequestTerm(clickCourseBean.termIndex - 1)
    }
  }

  private var firstCourseBeanJob: Job? = null

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    if (firstCourseBeanJob == null) {
      val nowTermIndex = StuLessonRepository.getNowTermIndex(stuNum)
      firstCourseBeanJob = coroutineScope.launch {
        runCatching {
          StuLessonRepository.getCourseBean(stuNum, nowTermIndex)
            .flowOn(Dispatchers.IO)
            .onEach { bean ->
              setCourse(bean)
            }.last()
        }.onFailure {
          firstCourseBeanJob = null // 如果失败则等待下一次 onComposeInit 时重复加载
        }.tryThrowCancellationException().onSuccess {
          onChangedTermIndex(-it.termIndex, it.beginDate)
        }
      }
      onRequestTerm(if (nowTermIndex != Int.MIN_VALUE) -nowTermIndex else nowTermIndex)
    }
  }

  private fun setCourse(bean: CourseBean) {
    courseBeans[bean.termIndex] = bean
    val newItems = bean.toLessonItemBean().toCourseItem()
    val oldItems = courseItems.put(bean.termIndex, newItems)
    removeAll(oldItems)
    addAll(newItems)
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