package com.course.pages.course.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.components.utils.time.Today
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.course.ui.pager.CoursePagerCompose
import com.course.pages.course.ui.pager.CoursePagerState
import com.course.pages.course.ui.pager.rememberCoursePagerState
import com.course.shared.time.Date
import kotlinx.collections.immutable.ImmutableList
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
  content: @Composable (CoursePagerState) -> Unit = {
    CoursePagerCompose(
      state = it
    )
  }
) {
  HorizontalPager(
    state = state.pagerState,
    modifier = Modifier.then(modifier).reflexScrollableForMouse(),
    key = { state.beginDate.plusWeeks(it).value },
    pageContent = { page ->
      val weekBeginDate = state.beginDate.plusWeeks(page)
      val coursePagerState = rememberCoursePagerState(
        weekBeginDate = weekBeginDate,
        timeline = timeline,
        itemGroups = state.itemGroups,
      )
      content(coursePagerState)
    }
  )
}


@OptIn(ExperimentalFoundationApi::class)
@Stable
class CourseComposeState(
  private val coroutineScope: CoroutineScope,
  val pagerState: PagerState,
  val startDateState: State<Date>,
  val endDateState: State<Date>,
  val itemGroups: ImmutableList<ICourseItemGroup>,
) {
  val beginDate: Date
    get() = startDateState.value.minusDays(startDateState.value.dayOfWeekOrdinal)
  val finalDate: Date
    get() = endDateState.value.plusDays(6 - endDateState.value.dayOfWeekOrdinal)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCourseComposeState(
  startDate: Date = Date(1901, 1, 1),
  endDate: Date = Date(2099, 12, 31),
  itemGroups: ImmutableList<ICourseItemGroup>,
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
      itemGroups = itemGroups,
    )
  }
}

