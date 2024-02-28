package com.course.components.view.calendar.month

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.State
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import com.course.components.utils.time.Date
import com.course.components.view.calendar.scroll.HorizontalScrollState
import com.course.components.view.calendar.scroll.VerticalScrollState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/25 22:11
 */
@OptIn(ExperimentalFoundationApi::class)
internal class CalendarMonthMeasurePolicy(
  private val verticalScrollState: State<VerticalScrollState>,
  private val horizontalScrollState: State<HorizontalScrollState>,
  private val lineHeightState: MutableFloatState,
  private val startDate: State<Date>,
  private val endDate: State<Date>,
  private val clickDate: State<Date>,
  private val showIndexSet: MutableSet<Int>,
) : (LazyLayoutMeasureScope, Constraints) -> MeasureResult {

  override fun invoke(scope: LazyLayoutMeasureScope, constraints: Constraints): MeasureResult {
    showIndexSet.clear()
    return when (val vertical = verticalScrollState.value) {
      VerticalScrollState.Collapsed -> when (horizontalScrollState.value) {
        HorizontalScrollState.Idle -> scope.collapsedMeasure(constraints)
        is HorizontalScrollState.Scrolling -> scope.collapsedHorizontalScrollingMeasure(constraints)
      }

      is VerticalScrollState.Expanded -> scope.expandedMeasure(constraints)
      is VerticalScrollState.Scrolling -> scope.verticalScrollingMeasure(constraints, vertical)
    }
  }

  private fun getBeginDate(): Date {
    return startDate.value.copy(dayOfMonth = 1).run {
      minusDays(dayOfWeekOrdinal)
    }
  }

  private fun getFinalDate(): Date {
    return endDate.value.copy(dayOfMonth = 31, noOverflow = true).run {
      plusDays(6 - dayOfWeekOrdinal)
    }
  }

  private fun LazyLayoutMeasureScope.measure(date: Date, constraints: Constraints): Placeable? {
    return if (date in getBeginDate()..getFinalDate()) {
      val index = startDate.value.indexUntil(date)
      showIndexSet.add(index)
      measure(index, constraints).first()
    } else null
  }

  private fun LazyLayoutMeasureScope.collapsedMeasure(constraints: Constraints): MeasureResult {
    val expandedMonthPlaceable = getExpandedMonthPlaceable(clickDate.value, constraints)
    val lineHeight = expandedMonthPlaceable.first().height
    lineHeightState.value = lineHeight.toFloat()
    val line = clickDate.value.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
    val leftPlaceable: Placeable?
    val rightPlaceable: Placeable?
    if (line == 0) {
      leftPlaceable = measure(
        clickDate.value.minusWeeks(1),
        Constraints.fixed(constraints.maxWidth, lineHeight)
      )
      rightPlaceable = expandedMonthPlaceable[1]
    } else if (line == expandedMonthPlaceable.lastIndex) {
      leftPlaceable = expandedMonthPlaceable[line - 1]
      rightPlaceable = measure(
        clickDate.value.plusWeeks(1),
        Constraints.fixed(constraints.maxWidth, lineHeight)
      )
    } else {
      leftPlaceable = expandedMonthPlaceable[line - 1]
      rightPlaceable = expandedMonthPlaceable[line + 1]
    }
    return layout(constraints.maxWidth, lineHeight) {
      leftPlaceable?.placeRelativeWithLayer(x = -constraints.maxWidth, y = 0)
      rightPlaceable?.placeRelativeWithLayer(x = constraints.maxWidth, y = 0)
      val lineDistance = if (expandedMonthPlaceable.size == 6) 0F else lineHeight / 4F
      var top = -line * (lineHeight + lineDistance)
      repeat(expandedMonthPlaceable.size) {
        val placeable = expandedMonthPlaceable[it]
        if (placeable !== leftPlaceable && placeable !== rightPlaceable) {
          placeable.placeRelativeWithLayer(x = 0, y = top.roundToInt())
        }
        top += lineHeight + lineDistance
      }
    }
  }

  private fun LazyLayoutMeasureScope.collapsedHorizontalScrollingMeasure(
    constraints: Constraints,
  ): MeasureResult {
    val pageDiff = (horizontalScrollState.value.offset / constraints.maxWidth).roundToInt()
    // center
    val centerDate = clickDate.value.minusWeeks(pageDiff).coerceIn(startDate.value, endDate.value)
    val centerPlaceable = measure(centerDate, constraints.copy(minWidth = constraints.maxWidth))!!
    lineHeightState.value = centerPlaceable.height.toFloat()
    val newConstraints = Constraints.fixed(centerPlaceable.width, centerPlaceable.height)
    // left
    val leftPlaceable = measure(centerDate.minusWeeks(1), newConstraints)
    // right
    val rightPlaceable = measure(centerDate.plusWeeks(1), newConstraints)
    return layout(constraints.maxWidth, centerPlaceable.height) {
      val sign = horizontalScrollState.value.offset.sign.toInt()
      val mod = abs(horizontalScrollState.value.offset) % constraints.maxWidth
      val x = if (mod < constraints.maxWidth / 2F) mod.roundToInt() * sign
      else (mod.roundToInt() - constraints.maxWidth) * sign
      centerPlaceable.placeRelativeWithLayer(x = x, y = 0)
      leftPlaceable?.placeRelativeWithLayer(x = -constraints.maxWidth + x, y = 0)
      rightPlaceable?.placeRelativeWithLayer(x = constraints.maxWidth + x, y = 0)
    }
  }

  private fun LazyLayoutMeasureScope.verticalScrollingMeasure(
    constraints: Constraints,
    vertical: VerticalScrollState.Scrolling,
  ): MeasureResult {
    val expandedMonthPlaceable = getExpandedMonthPlaceable(clickDate.value, constraints)
    val lineHeight = expandedMonthPlaceable.first().height
    lineHeightState.value = lineHeight.toFloat()
    return layout(constraints.maxWidth, lineHeight + vertical.offset.roundToInt()) {
      val line = clickDate.value.run { copy(dayOfMonth = 1).dayOfWeekOrdinal + dayOfMonth - 1 } / 7
      val lineDistance = if (expandedMonthPlaceable.size == 6) 0F else lineHeight / 4F
      // top 初始值为顶部隐藏的距离
      var top = -(lineHeight * 5 - vertical.offset) / (expandedMonthPlaceable.size - 1) * line
      repeat(expandedMonthPlaceable.size) {
        expandedMonthPlaceable[it].placeRelativeWithLayer(x = 0, y = top.roundToInt())
        top += lineHeight + lineDistance
      }
    }
  }

  private fun LazyLayoutMeasureScope.expandedMeasure(
    constraints: Constraints,
  ): MeasureResult {
    val pageDiff = (horizontalScrollState.value.offset / constraints.maxWidth).roundToInt()
    // center
    val centerPageShowDate = clickDate.value.minusMonths(pageDiff)
    val centerPlaceable = getExpandedMonthPlaceable(centerPageShowDate, constraints)
    // left
    val leftPageShowDate = centerPageShowDate.minusMonths(1)
    val leftPlaceable = if (leftPageShowDate >= startDate.value.copy(dayOfMonth = 1)) {
      getExpandedMonthPlaceable(leftPageShowDate, constraints)
    } else null
    // right
    val rightPageShowDate = centerPageShowDate.plusMonths(1)
    val rightPlaceable =
      if (rightPageShowDate <= endDate.value.copy(dayOfMonth = 31, noOverflow = true)) {
        getExpandedMonthPlaceable(rightPageShowDate, constraints)
      } else null
    val lineHeight = centerPlaceable[0].height
    lineHeightState.value = lineHeight.toFloat()
    return layout(constraints.maxWidth, lineHeight * 6) {
      val sign = horizontalScrollState.value.offset.sign.toInt()
      val mod = abs(horizontalScrollState.value.offset) % constraints.maxWidth
      val x = if (mod < constraints.maxWidth / 2F) mod.roundToInt() * sign
      else (mod.roundToInt() - constraints.maxWidth) * sign
      layoutExpandedPlaceable(centerPlaceable, x)
      if (leftPlaceable != null) {
        layoutExpandedPlaceable(leftPlaceable, -constraints.maxWidth + x)
      }
      if (rightPlaceable != null) {
        layoutExpandedPlaceable(rightPlaceable, constraints.maxWidth + x)
      }
    }
  }

  private fun LazyLayoutMeasureScope.getExpandedMonthPlaceable(
    date: Date,
    constraints: Constraints
  ): Array<Placeable> {
    val lineCount = date.monthLineCount()
    var newConstraints = constraints.copy(minWidth = constraints.maxWidth)
    val firstDate = date.copy(dayOfMonth = 1)
    return Array(lineCount) { index ->
      if (index == 0) {
        measure(
          startDate.value.indexUntil(firstDate).also { showIndexSet.add(it) },
          newConstraints
        ).first().also {
          newConstraints = Constraints.fixed(it.width, it.height)
        }
      } else {
        measure(
          startDate.value.indexUntil(
            firstDate.run {
              minusDays(dayOfWeekOrdinal)
                .plusWeeks(index)
            }
          ).also { showIndexSet.add(it) },
          newConstraints
        ).first()
      }
    }
  }

  private fun Placeable.PlacementScope.layoutExpandedPlaceable(array: Array<Placeable>, x: Int) {
    val lineDistance = if (array.size == 6) 0F else array[0].height / 4F
    var top = 0F
    repeat(array.size) {
      val placeable = array[it]
      placeable.placeRelativeWithLayer(x = x, y = top.roundToInt())
      top += placeable.height + lineDistance
    }
  }
}

