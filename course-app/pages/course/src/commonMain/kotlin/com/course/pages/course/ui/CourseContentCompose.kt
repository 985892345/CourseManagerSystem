package com.course.pages.course.ui

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Today
import com.course.components.view.calendar.CalendarCompose
import com.course.components.view.calendar.layout.CalendarContentOffsetMeasurePolicy
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState
import com.course.pages.course.api.controller.CourseDetail
import com.course.shared.time.Date
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * .
 *
 * @author 985892345
 * 2024/3/17 16:47
 */

@Composable
fun CourseContentCompose(
  detail: CourseDetail,
  contentPadding: PaddingValues = remember { PaddingValues(0.dp) },
  course: @Composable (CourseComposeState) -> Unit = {
    CourseCompose(
      state = it,
      paddingBottom = contentPadding.calculateBottomPadding(),
    )
  }
) {
  val calendarState = rememberCalendarState(
    initialClickDate = detail.initialClickDate,
    startDate = detail.startDate,
    endDate = detail.endDate,
  )
  val courseComposeState = rememberCourseComposeState(
    startDate = detail.startDate,
    endDate = detail.endDate,
    initialDate = detail.initialClickDate,
    itemGroups = (detail.controllers + detail).toImmutableList(),
  )
  Column(modifier = Modifier.absolutePadding(
    left = contentPadding.calculateLeftPadding(LocalLayoutDirection.current),
    right = contentPadding.calculateRightPadding(LocalLayoutDirection.current),
    top = contentPadding.calculateTopPadding(),
  )) {
    CourseHeaderCompose(detail, calendarState, courseComposeState)
    CalendarCompose(
      modifier = Modifier.padding(start = 2.dp, end = 4.dp, top = 4.dp),
      state = calendarState
    ) {
      Box(
        modifier = Modifier.layout(remember(calendarState) {
          CalendarContentOffsetMeasurePolicy(calendarState)
        }),
        propagateMinConstraints = true,
      ) {
        course(courseComposeState)
      }
    }
  }
  configCalendarCoursePager(calendarState, courseComposeState)
  callbackToDetail(detail, calendarState)
}

@Composable
private fun callbackToDetail(detail: CourseDetail, calendarState: CalendarState) {
  val coroutineScope = rememberCoroutineScope()
  DisposableEffect(Unit) {
    detail.onComposeInit(coroutineScope)
    onDispose {
      detail.onComposeDispose()
    }
  }
  LaunchedEffect(detail, calendarState) {
    snapshotFlow { calendarState.clickDate }.collect {
      detail.onChangedClickDate(it)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun configCalendarCoursePager(
  calendarState: CalendarState,
  courseComposeState: CourseComposeState
) {
  LaunchedEffect(calendarState, courseComposeState) {
    var isAnimateScrollCourse = false
    launch {
      snapshotFlow { calendarState.clickDate }.map {
        courseComposeState.beginDate.daysUntil(it) / 7
      }.collect {
        if (courseComposeState.pagerState.currentPage != it) {
          launch {
            isAnimateScrollCourse = true
            try {
              // 每次都需要一个新的协程进行处理，上游发送太快时前一个 launch 会被取消
              courseComposeState.pagerState.animateScrollToPage(it)
            } finally {
              isAnimateScrollCourse = false
            }
          }
        }
      }
    }
    launch {
      snapshotFlow { courseComposeState.pagerState.currentPage }.filter {
        !isAnimateScrollCourse
      }.map {
        courseComposeState.beginDate.plusWeeks(it)
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

@Composable
private fun CourseHeaderCompose(
  detail: CourseDetail,
  calendarState: CalendarState,
  courseComposeState: CourseComposeState,
  today: Date = Today,
) {
  ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
    val (week, back, term) = createRefs()
    Text(
      text = detail.title,
      modifier = Modifier.constrainAs(week) {
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        start.linkTo(parent.start, 16.dp)
      }.clickableNoIndicator { detail.onClickTitle() },
      color = LocalAppColors.current.tvLv2,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp
    )
    Text(
      text = detail.subtitle,
      modifier = Modifier.constrainAs(term) {
        start.linkTo(week.end, 8.dp)
        baseline.linkTo(week.baseline)
      }.clickableNoIndicator { detail.onClickSubtitle() },
      fontSize = 12.sp,
      color = LocalAppColors.current.tvLv2,
    )
    if (today in calendarState.startDateState.value..calendarState.endDateState.value) {
      // Today 不在显示范围时不显示回到今天按钮
      BackTodayCompose(
        modifier = Modifier.constrainAs(back) {
          top.linkTo(parent.top)
          bottom.linkTo(parent.bottom)
          end.linkTo(parent.end, 16.dp)
        },
        calendarState = calendarState,
        courseComposeState = courseComposeState,
        today = today,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackTodayCompose(
  modifier: Modifier,
  calendarState: CalendarState,
  courseComposeState: CourseComposeState,
  today: Date,
) {
  var animateFraction by remember { mutableFloatStateOf(1F) }
  val backFraction by remember(calendarState, courseComposeState) {
    derivedStateOfStructure {
      if (animateFraction != 1F) {
        animateFraction
      } else if (today.dayOfWeek == calendarState.clickDate.dayOfWeek) {
        val fraction =
          courseComposeState.pagerState.run { currentPage + currentPageOffsetFraction } -
              courseComposeState.beginDate.daysUntil(today) / 7
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
      brush = Brush.horizontalGradient(
        colors = listOf(Color.Blue, Color(0xFF8686FF)),
      )
    ).padding(vertical = 10.dp, horizontal = 16.dp)
      .clickable {
        coroutineScope.launch {
          animate(1F, 0F) { value, _ ->
            animateFraction = value
          }
          animateFraction = 1F
        }
        calendarState.updateClickDate(today)
      },
    color = Color.White,
    fontSize = 14.sp,
  )
  LaunchedEffect(calendarState, today) {
    calendarState.clickEventFlow.collect {
      if (it.new != today && it.old == today) {
        animate(0F, 1F) { value, _ ->
          animateFraction = value
        }
      } else if (it.new == today && it.old != today) {
        animate(1F, 0F) { value, _ ->
          animateFraction = value
        }
        animateFraction = 1F
      }
    }
  }
}