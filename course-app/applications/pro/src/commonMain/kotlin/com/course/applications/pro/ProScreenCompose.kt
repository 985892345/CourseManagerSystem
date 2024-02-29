package com.course.applications.pro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.base.theme.AppTheme
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.SchoolCalendar
import com.course.components.utils.time.Today
import com.course.components.view.calendar.CalendarCompose
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState
import com.course.pages.course.ui.CourseCompose
import com.course.pages.course.ui.CourseState
import com.course.pages.course.ui.rememberCourseState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    val startDate = SchoolCalendar.beginDate[0]
    val calendarState = rememberCalendarState(
      startDate = startDate,
    )
    val courseState = rememberCourseState(
      startDate = startDate,
    )
    Column(modifier = Modifier.systemBarsPadding()) {
      CourseHeaderCompose(calendarState, courseState)
      CalendarCompose(
        modifier = Modifier.padding(start = 2.dp, end = 4.dp, top = 4.dp),
        state = calendarState
      ) {
        CourseCompose(
          state = courseState
        )
      }
    }
    LaunchedEffect(calendarState, courseState) {
      launch {
        snapshotFlow { calendarState.clickDate }.map {
          courseState.beginDate.daysUntil(it) / 7
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
          courseState.beginDate.plusWeeks(it)
            .plusDays(calendarState.clickDate.dayOfWeekOrdinal)
            .coerceIn(calendarState.startDateState.value, calendarState.endDateState.value)
        }.collect {
          if (it != calendarState.clickDate && !calendarState.verticalIsScrolling
            && !calendarState.horizontalIsScrolling
          ) {
            calendarState.updateClickDate(it)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseHeaderCompose(
  calendarState: CalendarState,
  courseState: CourseState,
) {
  ConstraintLayout(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
    val (week, back, term, notification) = createRefs()
    Text(
      text = SchoolCalendar.getWeekStr(calendarState.clickDate) ?: "未知周数",
      modifier = Modifier.constrainAs(week) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start, 16.dp)
      },
      color = Color.Black,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp
    )
    Text(
      text = SchoolCalendar.getTermStr(2020, calendarState.clickDate) ?: "",
      modifier = Modifier.constrainAs(term) {
        start.linkTo(week.end, 8.dp)
        baseline.linkTo(week.baseline)
      },
      fontSize = 12.sp,
      color = Color.Black,
    )
    val backFraction by remember {
      derivedStateOfStructure {
        val fraction = courseState.pagerState.run { currentPage + currentPageOffsetFraction } -
            courseState.beginDate.daysUntil(Today) / 7
        // 1 -> 0 -> 1
        minOf(abs(fraction), 1F)
      }
    }
    Text(
      text = "回到今天",
      modifier = Modifier.constrainAs(back) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        end.linkTo(parent.end, 16.dp)
      }.graphicsLayer {
        alpha = backFraction
        translationX = (1 - backFraction) * size.width
      }.clip(CircleShape).background(
        brush = Brush.linearGradient(
          colors = listOf(Color.Blue, Color(0xFF8686FF)),
          start = Offset.Zero,
          end = Offset.Infinite
        )
      ).padding(vertical = 10.dp, horizontal = 16.dp)
        .clickable {
          calendarState.updateClickDate(Today)
        },
      color = Color.White,
      fontSize = 14.sp
    )
  }
}

