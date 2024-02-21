package com.course.components.view.calendar.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.time.Today
import com.course.components.utils.time.diffDays
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/18 18:43
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalCoroutinesApi::class)
@Stable
class CalendarState(
  startDate: LocalDate,
  endDate: LocalDate,
  val pagerState: PagerState,
  val onClick: CalendarState.(LocalDate) -> Unit
) {

  /**
   * 日历视图的开始日期
   */
  var startDate by mutableStateOf(startDate)
    internal set

  /**
   * 日历视图的结束日期
   */
  var endDate by mutableStateOf(endDate)
    internal set

  /**
   * 选中的日期
   */
  var clickDate by mutableStateOf(Today)
    internal set

  /**
   * 更新选中的日期，如果不在当前页则会自动跳转
   */
  fun updateClickDate(date: LocalDate) {
    if (date in startDate..endDate) {
      clickDate = date
    }
  }

  /**
   * 最后一次展开/折叠状态，不包含滑动
   */
  var lastSheetValue by mutableStateOf(CalendarSheetValue.Collapsed)
    internal set

  val lastIsExpanded: Boolean
    get() = lastSheetValue == CalendarSheetValue.Expanded

  val lastIsCollapsed: Boolean
    get() = lastSheetValue == CalendarSheetValue.Collapsed

  /**
   * 当前展开/折叠状态，如果为 null，则说明处于滑动中
   *
   * 使用 [currentIsCollapsed]、[currentIsExpanded]、[isScrolling] 只观察单一状态
   */
  var currentSheetValue: CalendarSheetValue? by mutableStateOf(CalendarSheetValue.Collapsed)
    internal set

  val currentIsExpanded: Boolean by derivedStateOfStructure {
    currentSheetValue == CalendarSheetValue.Expanded
  }

  val currentIsCollapsed: Boolean by derivedStateOfStructure {
    currentSheetValue == CalendarSheetValue.Collapsed
  }

  val isScrolling: Boolean by derivedStateOfStructure {
    currentSheetValue == null
  }

  /**
   * 当前滑动的值
   */
  var scrollOffset by mutableFloatStateOf(0F)
    internal set

  /**
   * 最大滑动的值，如果未布局，则返回 -1
   */
  var maxScrollOffset by mutableFloatStateOf(-1F)
    internal set

  /**
   * 当前滑动的比例，[0.0, 1.0]
   */
  val fraction: Float by derivedStateOfStructure {
    if (maxScrollOffset <= 0F) 0F else scrollOffset / maxScrollOffset
  }

  // 上一次停止的页面，每一页的日期由 当前页数、oldSelectPage、clickDate、lastSheetValue 共同决定
  internal var oldSelectPage = pagerState.settledPage

  init {
    snapshotFlow { pagerState.settledPage }.onEach {
      // 在翻页结束后更新 clickDate
      if (oldSelectPage == it) return@onEach
      val unit = when (lastSheetValue) {
        CalendarSheetValue.Collapsed -> DateTimeUnit.WEEK
        CalendarSheetValue.Expanded -> DateTimeUnit.MONTH
      }
      clickDate = clickDate.plus(it - oldSelectPage, unit)
      oldSelectPage = it
    }.mapLatest {
      // 回到中点以实现伪无限翻页
      val centerPage = pagerState.pageCount / 2
      val startPageDiff = diffPage(clickDate, startDate)
      if (startPageDiff <= centerPage) {
        delayBackPage(startPageDiff)
      } else {
        val endPageDiff = diffPage(endDate, clickDate)
        if (endPageDiff <= centerPage) {
          delayBackPage(pagerState.currentPage - endPageDiff - 1)
        } else {
          delayBackPage(centerPage)
        }
      }
    }.launchIn(AppCoroutineScope)
  }

  /**
   * 计算两个日期的页面差，取决于 [lastSheetValue] 状态
   */
  fun diffPage(date1: LocalDate, date2: LocalDate): Int {
    return when (lastSheetValue) {
      CalendarSheetValue.Collapsed -> {
        (date1.diffDays(date2) - date1.dayOfWeek.ordinal + date2.dayOfWeek.ordinal) / 7
      }
      CalendarSheetValue.Expanded -> {
        (date1.year - date2.year) * 12 + date1.monthNumber - date2.monthNumber
      }
    }
  }

  private suspend fun delayBackPage(page: Int) {
    delay(2000) // 延时以避免用户的快速连续滚动
    if (!pagerState.isScrollInProgress && !isScrolling) {
      oldSelectPage = page
      pagerState.scrollToPage(page)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCalendarState(
  startDate: LocalDate = LocalDate(1901, 1, 1),
  endDate: LocalDate = LocalDate(2099, 12, 31),
  onClick: CalendarState.(LocalDate) -> Unit = {
    if (it in startDate..endDate) {
      updateClickDate(it)
    } else toast("选择的日期不能超过 $startDate-$endDate")
  },
): CalendarState {
  val pagerState = rememberPagerState(initialPage = 30) { 60 }
  return remember {
    CalendarState(
      startDate = startDate,
      endDate = endDate,
      pagerState = pagerState,
      onClick = onClick,
    )
  }.apply {
    this.startDate = startDate
    this.endDate = endDate
  }
}

enum class CalendarSheetValue {
  Collapsed,
  Expanded,
}
