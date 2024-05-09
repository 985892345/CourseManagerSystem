package com.course.pages.team.ui.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.debug.logg
import com.course.components.utils.provider.Provider
import com.course.components.utils.time.Today
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.source.app.team.TeamMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 16:28
 */
class MemberCourseDetail(
  val members: List<TeamMember>
) : CourseDetail(persistentListOf()) {

  private val itemGroup = MemberCourseItemGroup()

  private var beginDate by mutableStateOf<Date?>(null)
  private var termIndex: Int = -1

  override val startDate: Date by derivedStateOfStructure {
    beginDate ?: Today.firstDate
  }

  private var clickDate by mutableStateOf(Today)

  override val title: String by derivedStateOfStructure {
    beginDate?.let { getWeekStr(it, clickDate) } ?: "无数据"
  }

  override val subtitle: String
    get() = ""

  override fun getTerms(): List<Pair<Int, Date>> {
    return if (termIndex != -1) listOf(termIndex to beginDate!!) else emptyList()
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    itemGroup.Content(weekBeginDate, timeline, scrollState)
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    val courseService = Provider.impl(ICourseService::class)
    coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
      logg("throwable = ${throwable.stackTraceToString()}")
    }) {
      members.map {
        it to async(Dispatchers.IO) {
          courseService.refreshCourseBean(it.num, it.type, -1)
        }
      }.map {
        val bean = it.second.await()
        if (beginDate == null) {
          beginDate = bean.beginDate
          termIndex = bean.termIndex
        } else {
          if (beginDate != bean.beginDate) {
            throw IllegalStateException("beginDate 不一致, member=${it.first}")
          }
          if (termIndex != bean.termIndex) {
            throw IllegalStateException("termIndex 不一致, member=${it.first}")
          }
        }
        it.first to bean
      }.let {
        itemGroup.resetData(it)
      }
    }
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