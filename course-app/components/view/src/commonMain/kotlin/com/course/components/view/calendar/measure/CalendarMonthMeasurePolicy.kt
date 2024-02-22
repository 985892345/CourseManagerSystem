package com.course.components.view.calendar.measure

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.time.copy
import com.course.components.view.calendar.state.CalendarState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlin.math.roundToInt

/**
 * 测量月份的日历视图
 *
 * @author 985892345
 * @date 2024/2/22 13:48
 */
class CalendarMonthMeasurePolicy(
  showDate: LocalDate,
  state: CalendarState,
) : MeasurePolicy {

  var state: CalendarState by mutableStateOf(state)

  var showDate: LocalDate by mutableStateOf(showDate)

  private val beginDate by derivedStateOfStructure {
    this.showDate.copy(dayOfMonth = 1).run {
      minus(dayOfWeek.ordinal, DateTimeUnit.DAY)
    }
  }

  private val nowEndDate by derivedStateOfStructure {
    this.showDate.copy(
      dayOfMonth = 31,
      noOverflow = true
    )
  }

  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    return when (state.scrollOffset) {
      0F -> collapsedMeasure(measurables, constraints)
      state.maxScrollOffset -> expandedMeasure(measurables, constraints)
      else -> scrollingFastMeasure(measurables, constraints)
    }
  }

  private fun MeasureScope.collapsedMeasure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    val line = beginDate.daysUntil(showDate) / 7
    val placeable = measurables[line].measure(constraints.copy(minWidth = 0, minHeight = 0))
    return layout(constraints.maxWidth, placeable.height) {
      placeable.placeRelative(x = 0, y = 0)
    }
  }

  private fun MeasureScope.expandedMeasure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    val placeables = measurables.fastMap {
      it.measure(constraints.copy(minWidth = 0, minHeight = 0))
    }
    return layout(constraints.maxWidth, placeables.fastSumBy { it.height }) {
      if (beginDate.daysUntil(nowEndDate) / 7 == 5) {
        var top = 0
        placeables.fastForEach {
          it.placeRelative(x = 0, y = top)
          top += it.height
        }
      } else {
        var top = 0F
        val lineDistance = placeables.last().height / 4F
        repeat(placeables.size - 1) {
          val placeable = placeables[it]
          placeable.placeRelative(x = 0, y = top.roundToInt())
          top += placeable.height + lineDistance
        }
      }
    }
  }

  private fun MeasureScope.scrollingFastMeasure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    // scroll 不是初始状态，又因为每行高度一致，所以可以提前算出行高
    val lineHeight = (state.maxScrollOffset / 5).toInt()
    val newConstraints = constraints.copy(
      minWidth = 0,
      minHeight = lineHeight,
      maxHeight = lineHeight
    )
    val line = beginDate.daysUntil(showDate) / 7
    // 有些月份会占 5 行有些会占 6 行
    val lineCount = beginDate.daysUntil(nowEndDate) / 7 + 1
    // 如果显示 5 行时则每行之间有间距
    val lineDistance = if (lineCount  == 5) lineHeight / 4F else 0F
    // 只保存会显示的 placeable
    val placeables = arrayOfNulls<Placeable>(lineCount)
    // 顶部隐藏的距离
    val topHideDistance = -(state.maxScrollOffset - state.scrollOffset) / (lineCount - 1) * line
    repeat(lineCount) {
      val t = topHideDistance + (lineHeight + lineDistance) * it
      if (t < state.scrollOffset + lineHeight && t + lineHeight > 0) {
        // 根据 top 和 bottom 判断是否处于显示区域内，只有显示的才会测量
        placeables[it] = measurables[it].measure(newConstraints)
      }
    }
    return layout(constraints.maxWidth, lineHeight + state.scrollOffset.roundToInt()) {
      repeat(lineCount) {
        val t = topHideDistance + (lineHeight + lineDistance) * it
        placeables[it]?.placeRelative(x = 0, y = t.roundToInt())
      }
    }
  }
}