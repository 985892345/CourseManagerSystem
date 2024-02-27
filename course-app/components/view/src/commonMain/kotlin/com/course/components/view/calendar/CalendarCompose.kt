package com.course.components.view.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.Date
import com.course.components.utils.time.Festival
import com.course.components.utils.time.SolarTerms
import com.course.components.utils.time.Today
import com.course.components.utils.time.toChineseCalendar
import com.course.components.view.calendar.month.CalendarMonthCompose
import com.course.components.view.calendar.scroll.CalendarNestedScroll
import com.course.components.view.calendar.state.CalendarState
import com.course.components.view.calendar.state.rememberCalendarState

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 10:16
 */
@Composable
fun CalendarCompose(
  modifier: Modifier = Modifier,
  state: CalendarState = rememberCalendarState(),
  calendar: @Composable ColumnScope.() -> Unit = {
    Row {
      state.MonthTextCompose(modifier = Modifier.width(30.dp).padding(top = 28.dp))
      Column(modifier = Modifier.weight(1F)) {
        state.WeekTextCompose()
        state.CalendarMonthCompose { begin, show ->
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

@Composable
fun CalendarState.CalendarWeekCompose(
  beginDate: Date,
  showDate: Date,
) {
  Row(
    modifier = Modifier
  ) {
    repeat(7) {
      Box(modifier = Modifier.weight(1F)) {
        CalendarDateCompose(
          date = beginDate.plusDays(it),
          showDate = showDate,
        )
      }
    }
  }
}

@Composable
fun CalendarState.CalendarDateCompose(
  date: Date,
  showDate: Date,
) {
  val dateState by rememberUpdatedState(date)
  val showDateState by rememberUpdatedState(showDate)
  val alphaState by remember {
    derivedStateOfStructure {
      if (dateState !in startDateState.value..endDateState.value) 0.3F
      else if (dateState.monthNumber == showDateState.monthNumber) 1F
      else if (currentIsCollapsed) 1F
      else 1F - fraction * 0.7F
    }
  }
  Box(
    modifier = Modifier.graphicsLayer {
      alpha = alphaState
    }.clickableNoIndicator { onClick.invoke(this, date) },
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
          date == Today && date == showDate -> Color.Blue
          date == showDate -> Color.LightGray
          date == Today -> Color.White
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
        text = date.dayOfMonth.toString(),
        color = when {
          date == Today && date == showDate -> Color.White
          date == Today -> Color.Blue
          else -> Color.Black
        },
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
      )
      val specialDay = Festival.get(date) ?: SolarTerms.get(date)?.chinese
      Text(
        modifier = Modifier.constrainAs(tLunar) {
          top.linkTo(tDay.bottom)
          bottom.linkTo(parent.bottom)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
        }.padding(),
        text = specialDay ?: date.toChineseCalendar().run {
          if (dayOfMonth == 1) getMonthStr() else getDayStr()
        },
        color = when {
          date == Today && date == showDate -> Color.White
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
          date == Today && date == showDate -> Color.White
          else -> Color.Green
        },
        fontSize = 8.sp,
      )
    }
  }
}