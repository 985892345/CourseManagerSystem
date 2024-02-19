package com.course.applications.pro.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.structuralEqualityPolicy
import com.course.components.utils.time.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
  val pagerState: PagerState,
  val onClick: CalendarState.(LocalDate) -> Unit
) {

  var clickDate by mutableStateOf(Today)

  fun updateClickDate(date: LocalDate) {
    clickDate = date
  }

  var lastSheetValue by mutableStateOf(CalendarSheetValue.Collapsed)
    internal set

  var currentSheetValue: CalendarSheetValue? by mutableStateOf(CalendarSheetValue.Collapsed)
    internal set

  val lastIsExpanded: Boolean
    get() = lastSheetValue == CalendarSheetValue.Expanded

  val lastIsCollapsed: Boolean
    get() = lastSheetValue == CalendarSheetValue.Collapsed

  val currentIsExpanded: Boolean
    get() = currentSheetValue == CalendarSheetValue.Expanded

  val currentIsCollapsed: Boolean
    get() = currentSheetValue == CalendarSheetValue.Collapsed

  val isScrolling: Boolean
    get() = currentSheetValue == null

  val fraction by derivedStateOf(structuralEqualityPolicy()) {
    if (maxScrollOffset == 0F) 0F else scrollOffset / maxScrollOffset
  }

  internal var scrollOffset by mutableFloatStateOf(0F)
  internal var maxScrollOffset by mutableFloatStateOf(0F)
  internal var oldSelectPage = pagerState.settledPage

  init {
    snapshotFlow { pagerState.settledPage }.onEach {
      if (pagerState.currentPageOffsetFraction != 0F || oldSelectPage == it) return@onEach
      val unit = when (lastSheetValue) {
        CalendarSheetValue.Collapsed -> DateTimeUnit.WEEK
        CalendarSheetValue.Expanded -> DateTimeUnit.MONTH
      }
      clickDate = clickDate.plus(it - oldSelectPage, unit)
      oldSelectPage = it
    }.mapLatest {
      while (true) {
        val newPage = pagerState.pageCount / 2
        if (it == newPage) break
        delay(2000) // 延时以避免用户的快速连续滚动
        if (!pagerState.isScrollInProgress && !isScrolling) {
          oldSelectPage = newPage
          pagerState.scrollToPage(newPage) // 回到中点以实现伪无限滚动
        }
      }
    }.launchIn(CoroutineScope(Dispatchers.Main))
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCalendarState(
  onClick: CalendarState.(LocalDate) -> Unit = {
    updateClickDate(it)
  }
): CalendarState {
  val pagerState = rememberPagerState(initialPage = 30) { 60 }
  return remember {
    CalendarState(
      pagerState = pagerState,
      onClick = onClick,
    )
  }
}

enum class CalendarSheetValue {
  Collapsed,
  Expanded,
}
