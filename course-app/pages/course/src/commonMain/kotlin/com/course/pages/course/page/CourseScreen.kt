package com.course.pages.course.page

import androidx.compose.animation.core.animate
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.base.navigator.screen.CourseRemoteScreen
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.navigator.RemoteScreenEnter
import com.course.components.utils.time.Date
import com.course.components.utils.time.SchoolCalendar
import com.course.components.utils.time.Today
import com.course.components.view.calendar.CalendarCompose
import com.course.components.view.calendar.layout.CalendarContentOffsetMeasurePolicy
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState
import com.course.pages.course.ui.CourseCompose
import com.course.pages.course.ui.CourseState
import com.course.pages.course.ui.rememberCourseState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/29 22:33
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@RemoteScreenEnter
fun CourseScreenContent(screen: CourseRemoteScreen) {
//  val startDate = SchoolCalendar.beginDate[0]
  val startDate = Date(2024, 2, 26)
  val endDate = Date(2024, 3, 31)
  val calendarState = rememberCalendarState(
    startDate = startDate,
    endDate = endDate,
  )
  val courseState = rememberCourseState(
    startDate = startDate,
    endDate = endDate,
  )
  Column(modifier = Modifier.systemBarsPadding()) {
    CourseHeaderCompose(screen.stuNum, calendarState, courseState)
    CalendarCompose(
      modifier = Modifier.padding(start = 2.dp, end = 4.dp, top = 4.dp),
      state = calendarState
    ) {
      CourseCompose(
        modifier = Modifier.layout(remember(calendarState) {
          CalendarContentOffsetMeasurePolicy(calendarState)
        }),
        state = courseState
      )
    }
  }
  LaunchedEffect(calendarState, courseState) {
    var isAnimateScrollCourse = false
    launch {
      snapshotFlow { calendarState.clickDate }.map {
        courseState.beginDate.daysUntil(it) / 7
      }.collect {
        if (courseState.pagerState.currentPage != it) {
          launch {
            isAnimateScrollCourse = true
            try {
              // 每次都需要一个新的协程进行处理，上游发送太快时前一个 launch 会被取消
              courseState.pagerState.animateScrollToPage(it)
            } finally {
              isAnimateScrollCourse = false
            }
          }
        }
      }
    }
    launch {
      snapshotFlow { courseState.pagerState.currentPage }.filter {
        !isAnimateScrollCourse
      }.map {
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

private class CalendarCourseLayout : MeasurePolicy {
  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    TODO("Not yet implemented")
  }

}

@Composable
private fun CourseHeaderCompose(
  stuNum: String,
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
      text = SchoolCalendar.getTermStr(
        stuNum.substring(0, 4).toInt(),
        calendarState.clickDate
      ) ?: "",
      modifier = Modifier.constrainAs(term) {
        start.linkTo(week.end, 8.dp)
        baseline.linkTo(week.baseline)
      },
      fontSize = 12.sp,
      color = Color.Black,
    )
    if (Today in calendarState.startDateState.value..calendarState.endDateState.value) {
      // Today 不在显示范围时不显示回到今天按钮
      BackTodayCompose(
        modifier = Modifier.constrainAs(back) {
          top.linkTo(parent.top)
          bottom.linkTo(parent.bottom)
          end.linkTo(parent.end, 16.dp)
        },
        calendarState = calendarState,
        courseState = courseState,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackTodayCompose(
  modifier: Modifier,
  calendarState: CalendarState,
  courseState: CourseState,
) {
  var animateFraction by remember { mutableFloatStateOf(1F) }
  val backFraction by remember(calendarState, courseState) {
    derivedStateOfStructure {
      if (animateFraction != 1F) {
        animateFraction
      } else if (Today.dayOfWeek == calendarState.clickDate.dayOfWeek) {
        val fraction = courseState.pagerState.run { currentPage + currentPageOffsetFraction } -
            courseState.beginDate.daysUntil(Today) / 7
        // 1 -> 0 -> 1
        minOf(abs(fraction), 1F)
      } else 1F
    }
  }
  val coroutineScope = rememberCoroutineScope()
  Text(
    text = "回到今天",
    modifier = modifier.graphicsLayer {
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
        coroutineScope.launch {
          animate(1F, 0F) { value, _ ->
            animateFraction = value
          }
          animateFraction = 1F
        }
        calendarState.updateClickDate(Today)
      },
    color = Color.White,
    fontSize = 14.sp,
  )
  LaunchedEffect(calendarState, Today) {
    calendarState.clickEventFlow.collect {
      if (it.new != Today && it.old == Today) {
        animate(0F, 1F) { value, _ ->
          animateFraction = value
        }
      } else if (it.new == Today && it.old != Today) {
        animate(1F, 0F) { value, _ ->
          animateFraction = value
        }
        animateFraction = 1F
      }
    }
  }
}