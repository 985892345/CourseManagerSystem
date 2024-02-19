package com.course.applications.pro

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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.course.applications.pro.state.CalendarNestedScroll
import com.course.applications.pro.state.CalendarSheetValue
import com.course.applications.pro.state.CalendarState
import com.course.applications.pro.state.rememberCalendarState
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.stable.Stab
import com.course.components.utils.stable.stable
import com.course.components.utils.time.Festival
import com.course.components.utils.time.SolarTerms
import com.course.components.utils.time.Today
import com.course.components.utils.time.toChineseCalendar
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
@Composable
fun CalendarCompose(
  modifier: Modifier = Modifier,
  state: CalendarState = rememberCalendarState(),
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize()
      .nestedScroll(remember { CalendarNestedScroll(state) })
      .pointerInput(Unit) {
        awaitPointerEventScope {
          // 等待所有手指抬起
          if (!currentEvent.changes.fastAny { it.pressed }) {
            do {
              val events = awaitPointerEvent(PointerEventPass.Final)
            } while (events.changes.fastAny { it.pressed })
          }
        }
      }
      .then(modifier)
  ) {
    state.CalendarTopCompose()
    content()
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarState.CalendarTopCompose(
  modifier: Modifier = Modifier,
) {
  Row(modifier = Modifier.then(modifier)) {
    Text(
      modifier = Modifier.width(30.dp).padding(top = 28.dp),
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
    Column(modifier = Modifier.weight(1F)) {
      Row {
        arrayOf("一", "二", "三", "四", "五", "六", "日").forEach {
          Text(
            modifier = Modifier.weight(1F),
            text = it,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
          )
        }
      }
      HorizontalPager(
        state = pagerState,
        userScrollEnabled = !isScrolling,
        modifier = Modifier,
      ) {
        if (currentIsCollapsed) {
          WeekLineCompose(
            date = clickDate.plus(it - oldSelectPage, DateTimeUnit.WEEK).run {
              minus(dayOfWeek.ordinal, DateTimeUnit.DAY).stable()
            },
            showDate = clickDate.plus(it - oldSelectPage, DateTimeUnit.WEEK).stable(),
          )
        } else {
          MonthCompose(
            modifier = Modifier,
            showDate = clickDate.plus(it - oldSelectPage, DateTimeUnit.MONTH).stable(),
          )
        }
      }
    }
  }
}

@Composable
private fun CalendarState.MonthCompose(
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
        WeekLineCompose(
          date = firstDate.plus(it, DateTimeUnit.WEEK).stable(),
          showDate = showDate,
        )
      }
    },
    measurePolicy = remember(showDate) {
      { measurables, constraints ->
        val placeables = measurables.map {
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
          placeables.forEach {
            it.placeRelativeWithLayer(x = 0, y = top)
            top += it.height
          }
        }
      }
    }
  )
}

@Composable
private fun CalendarState.WeekLineCompose(
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
        DateCompose(date.le.plus(it, DateTimeUnit.DAY).stable(), showDate)
      }
    }
  }
}

@Composable
private fun CalendarState.DateCompose(
  date: Stab<LocalDate>,
  showDate: Stab<LocalDate>,
) {
  val alpha = if (currentIsCollapsed) 1F
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