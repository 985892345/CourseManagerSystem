package com.course.pages.course.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.components.utils.time.Date
import com.course.components.utils.time.Today
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.CoursePagerCompose
import com.course.pages.course.ui.pager.rememberCoursePagerState
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
  state: CourseState,
  modifier: Modifier = Modifier,
) {
  HorizontalPager(
    state = state.pagerState,
    modifier = Modifier.then(modifier).reflexScrollableForMouse(),
    key = { state.beginDate.plusWeeks(it).time },
    pageContent = remember {
      { page ->
        val coursePagerState = rememberCoursePagerState(
          beginDate = state.beginDate.plusWeeks(page),
          items = state.data
        )
        CoursePagerCompose(
          state = coursePagerState
        )
      }
    }
  )
}


@OptIn(ExperimentalFoundationApi::class)
@Stable
class CourseState(
  private val coroutineScope: CoroutineScope,
  val pagerState: PagerState,
  val startDateState: State<Date>,
  val endDateState: State<Date>,
  val data: SnapshotStateList<ICourseItemBean>,
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
fun rememberCourseState(
  startDate: Date = Date(1901, 1, 1),
  endDate: Date = Date(2099, 12, 31),
  data: SnapshotStateList<ICourseItemBean> = remember { SnapshotStateList() },
): CourseState {
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
    CourseState(
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

