package com.course.components.view.calendar.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Today
import com.course.components.utils.time.copy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/18 18:43
 */
@OptIn(ExperimentalFoundationApi::class)
@Stable
class CalendarState(
  internal val coroutineScope: CoroutineScope,
  val startDate: LocalDate,
  val endDate: LocalDate,
  internal val weekPagerState: PagerState,
  internal val monthPagerState: PagerState,
  val onClick: CalendarState.(LocalDate) -> Unit
) {

  /**
   * 选中的日期
   */
  val clickDate by derivedStateOfStructure {
    if (currentIsCollapsed) {
      startDate.plus(weekPagerState.currentPage, DateTimeUnit.WEEK)
        .plus(clickDayOfWeek.ordinal - startDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
    } else {
      startDate.plus(monthPagerState.currentPage, DateTimeUnit.MONTH)
        .copy(dayOfMonth = clickDayOfMonth, noOverflow = true)
    }
  }

  internal var clickDayOfMonth by mutableIntStateOf(Today.dayOfMonth)

  internal var clickDayOfWeek by mutableStateOf(Today.dayOfWeek)

  /**
   * 更新选中的日期，如果不在当前页则会自动跳转
   */
  fun updateClickDate(date: LocalDate) {
    if (date in startDate..endDate) {
      clickDayOfMonth = date.dayOfMonth
      clickDayOfWeek = date.dayOfWeek
      coroutineScope.launch {
        val monthPage = date.year
          .minus(startDate.year)
          .times(12)
          .plus(date.monthNumber)
          .minus(startDate.monthNumber)
        if (currentIsCollapsed) {
          if (monthPage != monthPagerState.currentPage) {
            monthPagerState.scrollToPage(monthPage)
          }
        } else {
          if (monthPage != monthPagerState.currentPage) {
            monthPagerState.animateScrollToPage(monthPage)
          }
          val weekPage = startDate.daysUntil(date)
            .minus(date.dayOfWeek.ordinal)
            .plus(startDate.dayOfWeek.ordinal)
            .div(7)
          if (weekPage != weekPagerState.currentPage) {
            weekPagerState.scrollToPage(weekPage)
          }
        }
      }
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

  init {
    // 联动 weekPage 和 monthPage
    snapshotFlow { weekPagerState.currentPage }.onEach {
      if (!currentIsCollapsed) return@onEach
      val now = startDate.plus(it, DateTimeUnit.WEEK)
        .plus(clickDayOfWeek.ordinal - startDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
      clickDayOfMonth = now.dayOfMonth
      val page = now.year
        .minus(startDate.year)
        .times(12)
        .plus(now.monthNumber)
        .minus(startDate.monthNumber)
      if (page != monthPagerState.currentPage) {
        monthPagerState.scrollToPage(page)
      }
    }.launchIn(coroutineScope)
    snapshotFlow { monthPagerState.currentPage }.onEach {
      if (currentIsCollapsed) return@onEach
      val now = startDate.plus(it, DateTimeUnit.MONTH)
        .copy(dayOfMonth = clickDayOfMonth, noOverflow = true)
      clickDayOfWeek = now.dayOfWeek
      val page = startDate.daysUntil(now)
        .minus(now.dayOfWeek.ordinal)
        .plus(startDate.dayOfWeek.ordinal)
        .div(7)
      if (page != weekPagerState.currentPage) {
        weekPagerState.scrollToPage(page)
      }
    }.launchIn(coroutineScope)
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
  // 采用双 page 是为了更好的解决周视图和月视图页数不一致的问题
  val weekPagerState = rememberPagerState(
    initialPage = startDate.daysUntil(Today)
      .minus(Today.dayOfWeek.ordinal)
      .plus(startDate.dayOfWeek.ordinal)
      .div(7)
  ) {
    startDate.daysUntil(endDate)
      .minus(endDate.dayOfWeek.ordinal)
      .plus(startDate.dayOfWeek.ordinal)
      .div(7).plus(1)
  }
  val monthPagerState = rememberPagerState(
    initialPage = Today.year
      .minus(startDate.year)
      .times(12)
      .plus(Today.monthNumber)
      .minus(startDate.monthNumber)
  ) {
    endDate.year
      .minus(startDate.year)
      .times(12)
      .plus(endDate.monthNumber)
      .minus(startDate.monthNumber).plus(1)
  }
  val coroutineScope = rememberCoroutineScope()
  return remember {
    CalendarState(
      coroutineScope = coroutineScope,
      startDate = startDate,
      endDate = endDate,
      weekPagerState = weekPagerState,
      monthPagerState = monthPagerState,
      onClick = onClick,
    )
  }
}

enum class CalendarSheetValue {
  Collapsed,
  Expanded,
}
