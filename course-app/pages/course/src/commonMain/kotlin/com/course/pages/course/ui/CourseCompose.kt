package com.course.pages.course.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.course.components.utils.compose.Wrapper
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.components.utils.time.Today
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.ui.pager.CoursePagerCompose
import com.course.pages.course.ui.pager.WeekItemsProvider
import com.course.pages.course.ui.pager.rememberCoursePagerState
import com.course.pages.course.ui.pager.scroll.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 13:56
 */

/**
 * ```
 * CourseCompose                  课表
 * |-- CoursePagerCompose          课表单页
 *     |-- CourseScrollCompose       滚轴
 *     |----- CourseTimelineCompose    时间轴
 *     |----- CourseItemGroupCompose   item 容器
 *            |-- CourseItemCompose      item
 * ```
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCompose(
  state: CourseComposeState,
  modifier: Modifier = Modifier,
  timeline: CourseTimeline = remember { CourseTimeline() },
) {
  val weekItemsProvider = getWeekItemsProvider(timeline, state.data)
  HorizontalPager(
    state = state.pagerState,
    modifier = Modifier.then(modifier).reflexScrollableForMouse(),
    key = { state.beginDate.plusWeeks(it).value },
    pageContent = { page ->
      val weekBeginDate = state.beginDate.plusWeeks(page)
      val coursePagerState = rememberCoursePagerState(
        weekBeginDate = weekBeginDate,
        timeline = timeline,
        weekItems = weekItemsProvider.getWeekItems(
          MinuteTimeDate(
            weekBeginDate,
            timeline.delayMinuteTime
          )
        )
      )
      CoursePagerCompose(
        state = coursePagerState
      )
    }
  )
}

@Composable
private fun getWeekItemsProvider(
  timeline: CourseTimeline,
  data: ImmutableList<CourseDataProvider>
): WeekItemsProvider {
  val oldWeekItemsProvider = remember { Wrapper<WeekItemsProvider?>(null) }
  val weekItemsProvider = remember(timeline) {
    oldWeekItemsProvider.value?.destroy()
    WeekItemsProvider(timeline)
  }.also { it.init(data) }
  DisposableEffect(Unit) {
    onDispose {
      weekItemsProvider.destroy()
    }
  }
  return weekItemsProvider
}


@OptIn(ExperimentalFoundationApi::class)
@Stable
class CourseComposeState(
  private val coroutineScope: CoroutineScope,
  val pagerState: PagerState,
  val startDateState: State<Date>,
  val endDateState: State<Date>,
  val data: ImmutableList<CourseDataProvider>,
) {

  val beginDate: Date
    get() = startDateState.value.minusDays(startDateState.value.dayOfWeekOrdinal)
  val finalDate: Date
    get() = endDateState.value.plusDays(6 - endDateState.value.dayOfWeekOrdinal)

  /**
   * 当前学期的周数
   *
   * @return 返回 0，则表示开学前一周（因为第一周开学）; 返回 null 则说明无数据
   *
   * # 注意：存在返回负数的情况！！！
   * ```
   *     -1      0      1      2       3        4             返回值
   *  ----------------------------------------------------------->
   * -14     -7      0      7      14       21       28       天数差
   * ```
   */
//  val nowWeek by derivedStateOf(structuralEqualityPolicy()) {
//    semesterVpData.terms.lastOrNull()?.let {
//      val dayOfTerm = it.firstDate.daysUntil(Today)
//      if (dayOfTerm >= 0) dayOfTerm / 7 + 1 else dayOfTerm / 7
//    }
//  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCourseComposeState(
  startDate: Date = Date(1901, 1, 1),
  endDate: Date = Date(2099, 12, 31),
  data: ImmutableList<CourseDataProvider> = remember { persistentListOf(CourseDataProvider()) },
): CourseComposeState {
  val coroutineScope = rememberCoroutineScope()
  val startDateState = rememberUpdatedState(startDate)
  val endDateState = rememberUpdatedState(endDate)
  val begin = startDateState.value.minusDays(startDateState.value.dayOfWeekOrdinal)
  val final = endDateState.value.plusDays(6 - endDateState.value.dayOfWeekOrdinal)
  val pagerState = rememberPagerState(
    initialPage = Snapshot.withoutReadObservation {
      begin.daysUntil(Today.coerceIn(begin, final)) / 7
    }
  ) { begin.daysUntil(final) / 7 + 1 }
  return remember {
    CourseComposeState(
      coroutineScope = coroutineScope,
      pagerState = pagerState,
      startDateState = startDateState,
      endDateState = endDateState,
      data = data,
    )
  }
}


val LocalCourseColor = staticCompositionLocalOf { CourseColor() }

@Stable
class CourseColor {
  var background by mutableStateOf(Color(0xFFFFFFFF), structuralEqualityPolicy())
  var sheetTip by mutableStateOf(Color(0xFFE2EDFB), structuralEqualityPolicy())
}

