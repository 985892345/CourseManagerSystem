package com.course.applications.pro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.course.components.base.theme.AppTheme
import com.course.components.utils.time.Date
import com.course.components.view.calendar.CalendarCompose
import com.course.components.view.calendar.state.rememberCalendarState
import com.course.pages.course.ui.CourseCompose
import com.course.pages.course.ui.rememberCourseState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 16:22
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProScreenCompose() {
  AppTheme(darkTheme = false) {
    val startDate = Date(2024, 1, 29)
    val endDate = Date(2024, 3, 3)
    val calendarState = rememberCalendarState(
      startDate = startDate,
      endDate = endDate,
    )
    val courseState = rememberCourseState(
      startDate = startDate,
      endDate = endDate,
    )
    CalendarCompose(
      modifier = Modifier.systemBarsPadding(),
      state = calendarState
    ) {
      CourseCompose(
        state = courseState
      )
    }
    LaunchedEffect(calendarState, courseState) {
      launch {
        snapshotFlow { calendarState.clickDate }.map {
          courseState.beginDateState.value.daysUntil(it) / 7
        }.collect {
          if (courseState.pagerState.currentPage != it) {
            launch {
              // 每次都需要一个新的协程进行处理，上游发送太快时前一个 launch 会被取消
              courseState.pagerState.animateScrollToPage(it)
            }
          }
        }
      }
      launch {
        snapshotFlow { courseState.pagerState.targetPage }.map {
          courseState.beginDateState.value.plusWeeks(it)
            .plusDays(calendarState.clickDate.dayOfWeekOrdinal)
            .coerceIn(startDate, endDate)
        }.collect {
          if (it != calendarState.clickDate) {
            calendarState.updateClickDate(it)
          }
        }
      }
    }
  }
}

