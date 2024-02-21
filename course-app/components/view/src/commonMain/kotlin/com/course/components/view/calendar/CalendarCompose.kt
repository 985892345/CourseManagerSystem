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
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.utils.compose.Stab
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.stable
import com.course.components.utils.time.Festival
import com.course.components.utils.time.SolarTerms
import com.course.components.utils.time.Today
import com.course.components.utils.time.toChineseCalendar
import com.course.components.view.calendar.scroll.CalendarNestedScroll
import com.course.components.view.calendar.state.CalendarSheetValue
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.roundToInt

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
        state.CalendarPagerCompose {
          state.CalendarMonthCompose(it)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarState.MonthTextCompose(
  modifier: Modifier = Modifier
) {
  Text(
    modifier = modifier,
    text = "${
      clickDate.plus(
        pagerState.currentPage - oldSelectPage,
        when (lastSheetValue) {
          CalendarSheetValue.Collapsed -> DateTimeUnit.WEEK
          CalendarSheetValue.Expanded -> DateTimeUnit.MONTH
        }
      ).monthNumber
    }月",
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
  content: @Composable PagerScope.(Int) -> Unit
) {
  HorizontalPager(
    state = pagerState,
    userScrollEnabled = !isScrolling,
    modifier = modifier,
    beyondBoundsPageCount = 1,
    pageContent = content,
  )
}

@Composable
fun CalendarState.CalendarMonthCompose(
  pager: Int,
  modifier: Modifier = Modifier,
) {
  CalendarMonthCompose(
    modifier = modifier,
    showDate = clickDate.plus(
      pager - oldSelectPage,
      when (lastSheetValue) {
        CalendarSheetValue.Collapsed -> DateTimeUnit.WEEK
        CalendarSheetValue.Expanded -> DateTimeUnit.MONTH
      }
    ).stable(),
  )
}

@Composable
fun CalendarState.CalendarMonthCompose(
  modifier: Modifier = Modifier,
  showDate: Stab<LocalDate>,
) {
  Layout(
    modifier = Modifier.clipToBounds().then(modifier),
    content = {
      val firstDate = LocalDate(showDate.le.year, showDate.le.monthNumber, 1).run {
        minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
      }
      repeat(5) {
        CalendarWeekCompose(
          date = firstDate.plus(it, DateTimeUnit.WEEK).stable(),
          showDate = showDate,
        )
      }
    },
    measurePolicy = remember(showDate) {
      { measurables, constraints ->
        val placeables = measurables.fastMap {
          it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        // 不能引用外面的值，每次都需要重新计算
        val firstDate = LocalDate(showDate.le.year, showDate.le.monthNumber, 1).run {
          minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
        }
        val line = (showDate.le.toEpochDays() - firstDate.toEpochDays()) / 7
        val showPlaceable = placeables[line]
        layout(constraints.maxWidth, showPlaceable.height + scrollOffset.roundToInt()) {
          var top = (scrollOffset / (placeables.size - 1) * line).roundToInt()
          repeat(line) { top -= placeables[it].height }
          placeables.fastForEach {
            it.placeRelativeWithLayer(x = 0, y = top)
            top += it.height
          }
        }
      }
    }
  )
}

@Composable
fun CalendarState.CalendarWeekCompose(
  date: Stab<LocalDate>,
  showDate: Stab<LocalDate>,
) {
  Row(
    modifier = Modifier.onSizeChanged {
      maxScrollOffset = it.height * 4F
    }
  ) {
    repeat(7) {
      Box(modifier = Modifier.weight(1F)) {
        CalendarDateCompose(date.le.plus(it, DateTimeUnit.DAY).stable(), showDate)
      }
    }
  }
}

@Composable
fun CalendarState.CalendarDateCompose(
  date: Stab<LocalDate>,
  showDate: Stab<LocalDate>,
) {
  val alpha = if (date.le !in startDate..endDate) 0.3F
  else if (currentIsCollapsed) 1F
  else if (date.le.monthNumber == showDate.le.monthNumber) 1F
  else 1F - fraction * 0.7F
  Box(
    modifier = Modifier.alpha(alpha)
      .clickableNoIndicator { onClick.invoke(this, date.le) },
    contentAlignment = Alignment.Center
  ) {
    ConstraintLayout(
      modifier = Modifier.layout { measurable, constraints ->
        val width = minOf(constraints.maxWidth, constraints.maxHeight)
        val placeable = measurable.measure(
          Constraints(width, width, width, width)
        )
        layout(width, width) {
          placeable.placeRelative(x = 0, y = 0)
        }
      }.background(
        color = when {
          date.le == Today && date == showDate -> Color.Blue
          date == showDate -> Color.LightGray
          date.le == Today -> Color.White
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
        text = date.le.dayOfMonth.toString(),
        color = when {
          date.le == Today && date == showDate -> Color.White
          date.le == Today -> Color.Blue
          else -> Color.Black
        },
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
      )
      val specialDay = Festival.get(date.le) ?: SolarTerms.get(date.le)?.chinese
      Text(
        modifier = Modifier.constrainAs(tLunar) {
          top.linkTo(tDay.bottom)
          bottom.linkTo(parent.bottom)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }.padding(),
        text = specialDay ?: date.le.toChineseCalendar().run {
          if (dayOfMonth == 1) getMonthStr() else getDayStr()
        },
        color = when {
          date.le == Today && date == showDate -> Color.White
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
          date.le == Today && date == showDate -> Color.White
          else -> Color.Green
        },
        fontSize = 8.sp,
      )
    }
  }
}