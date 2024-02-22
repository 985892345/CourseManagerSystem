package com.course.components.view.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.utils.compose.Stab
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.stable
import com.course.components.utils.time.Festival
import com.course.components.utils.time.SolarTerms
import com.course.components.utils.time.Today
import com.course.components.utils.time.copy
import com.course.components.utils.time.toChineseCalendar
import com.course.components.view.calendar.measure.CalendarMonthMeasurePolicy
import com.course.components.view.calendar.scroll.CalendarNestedScroll
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 10:16
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarCompose(
  modifier: Modifier = Modifier,
  state: CalendarState = rememberCalendarState(),
  calendar: @Composable ColumnScope.() -> Unit = {
    Row {
      state.MonthTextCompose(modifier = Modifier.width(30.dp).padding(top = 28.dp))
      Column(modifier = Modifier.weight(1F)) {
        state.WeekTextCompose()
        state.CalendarPagerCompose { begin, show ->
          state.CalendarWeekCompose(begin, show)
        }
      }
    }
  },
  content: @Composable ColumnScope.() -> Unit = {},
) {
  Column(
    modifier = Modifier.fillMaxSize()
      .nestedScroll(remember { CalendarNestedScroll(state) })
      .then(modifier)
  ) {
    calendar()
    content()
  }
}

@Composable
fun CalendarState.MonthTextCompose(
  modifier: Modifier = Modifier
) {
  Text(
    modifier = modifier,
    text = "${clickDate.monthNumber}月",
    fontSize = 14.sp,
    textAlign = TextAlign.Center,
    overflow = TextOverflow.Visible,
  )
}

@Composable
fun CalendarState.WeekTextCompose(
  modifier: Modifier = Modifier
) {
  Row(modifier = modifier) {
    arrayOf("一", "二", "三", "四", "五", "六", "日").forEach {
      Text(
        modifier = Modifier.weight(1F),
        text = it,
        textAlign = TextAlign.Center,
        fontSize = 10.sp
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarState.CalendarPagerCompose(
  modifier: Modifier = Modifier,
  content: @Composable PagerScope.(begin: Stab<LocalDate>, show: Stab<LocalDate>) -> Unit
) {
  Box(modifier = modifier) {
    if (currentIsCollapsed) {
      HorizontalPager(
        state = weekPagerState,
        userScrollEnabled = !isScrolling,
        beyondBoundsPageCount = 1,
        key = {
          startDate.plus(it, DateTimeUnit.WEEK)
            .plus(clickDayOfWeek.ordinal - startDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
            .toEpochDays()
        },
        pageContent = { page ->
          val firstDate = startDate.plus(page, DateTimeUnit.WEEK)
            .minus(startDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
          content(
            firstDate.stable(),
            firstDate.plus(clickDayOfWeek.ordinal, DateTimeUnit.DAY)
              .coerceIn(startDate, endDate).stable()
          )
        }
      )
    } else {
      HorizontalPager(
        state = monthPagerState,
        userScrollEnabled = !isScrolling,
        key = {
          startDate.plus(it, DateTimeUnit.MONTH)
            .copy(dayOfMonth = clickDayOfMonth, noOverflow = true)
            .toEpochDays()
        },
        pageContent = { page ->
          val showDate = startDate.plus(page, DateTimeUnit.MONTH)
            .copy(dayOfMonth = clickDayOfMonth, noOverflow = true)
            .coerceIn(startDate, endDate).stable()
          CalendarMonthCompose(
            showDate = showDate,
          ) {
            content(it, showDate)
          }
        }
      )
    }
  }
}

@Composable
fun CalendarState.CalendarMonthCompose(
  modifier: Modifier = Modifier,
  showDate: Stab<LocalDate>,
  weekContent: @Composable (Stab<LocalDate>) -> Unit,
) {
  Layout(
    modifier = Modifier.clipToBounds().then(modifier),
    content = {
      val beginState = showDate.le.copy(dayOfMonth = 1).run {
        minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
      }
      repeat(6) {
        weekContent(beginState.plus(it, DateTimeUnit.WEEK).stable())
      }
    },
    measurePolicy = remember {
      CalendarMonthMeasurePolicy(showDate.le, this)
    }.apply {
      this.showDate = showDate.le
      this.state = this@CalendarMonthCompose
    }
  )
}

@Composable
fun CalendarState.CalendarWeekCompose(
  beginDate: Stab<LocalDate>,
  showDate: Stab<LocalDate>,
) {
  Row(
    modifier = Modifier.onSizeChanged {
      maxScrollOffset = it.height * 5F
    }
  ) {
    repeat(7) {
      Box(modifier = Modifier.weight(1F)) {
        CalendarDateCompose(beginDate.le.plus(it, DateTimeUnit.DAY).stable(), showDate)
      }
    }
  }
}

@Composable
fun CalendarState.CalendarDateCompose(
  beginDate: Stab<LocalDate>,
  showDate: Stab<LocalDate>,
) {
  val alpha = if (beginDate.le !in startDate..endDate) 0.3F
  else if (currentIsCollapsed) 1F
  else if (beginDate.le.monthNumber == showDate.le.monthNumber) 1F
  else 1F - fraction * 0.7F
  Box(
    modifier = Modifier.alpha(alpha)
      .clickableNoIndicator { onClick.invoke(this, beginDate.le) },
    contentAlignment = Alignment.Center
  ) {
    ConstraintLayout(
      modifier = Modifier.layout { measurable, constraints ->
        val width = minOf(constraints.maxWidth, constraints.maxHeight)
        val placeable = measurable.measure(
          Constraints.fixed(width, width)
        )
        layout(width, width) {
          placeable.placeRelative(x = 0, y = 0)
        }
      }.background(
        color = when {
          beginDate.le == Today && beginDate == showDate -> Color.Blue
          beginDate == showDate -> Color.LightGray
          beginDate.le == Today -> Color.White
          else -> Color.Transparent
        },
        shape = CircleShape
      )
    ) {
      val (tDay, tLunar, tRest) = createRefs()
      createVerticalChain(tDay, tLunar, chainStyle = ChainStyle.Packed)
      Text(
        modifier = Modifier.constrainAs(tDay) {
          top.linkTo(parent.top)
          bottom.linkTo(tLunar.top)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }.padding(),
        text = beginDate.le.dayOfMonth.toString(),
        color = when {
          beginDate.le == Today && beginDate == showDate -> Color.White
          beginDate.le == Today -> Color.Blue
          else -> Color.Black
        },
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
      )
      val specialDay = Festival.get(beginDate.le) ?: SolarTerms.get(beginDate.le)?.chinese
      Text(
        modifier = Modifier.constrainAs(tLunar) {
          top.linkTo(tDay.bottom)
          bottom.linkTo(parent.bottom)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }.padding(),
        text = specialDay ?: beginDate.le.toChineseCalendar().run {
          if (dayOfMonth == 1) getMonthStr() else getDayStr()
        },
        color = when {
          beginDate.le == Today && beginDate == showDate -> Color.White
          specialDay != null -> Color.Blue
          else -> Color.Gray
        },
        fontSize = 9.sp,
      )
      Text(
        modifier = Modifier.constrainAs(tRest) {
          top.linkTo(tDay.top, margin = (-2).dp)
          start.linkTo(tDay.end, margin = (-2).dp)
        }.padding(),
        text = "休",
        color = when {
          beginDate.le == Today && beginDate == showDate -> Color.White
          else -> Color.Green
        },
        fontSize = 8.sp,
      )
    }
  }
}