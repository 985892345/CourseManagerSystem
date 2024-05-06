package com.course.components.view.option

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/4/29 19:21
 */
@OptIn(ExperimentalFoundationApi::class)
class OptionScrollMeasurePolicy(
  val items: List<String>,
  val draggedLine: () -> Float,
  val parentHeight: MutableIntState,
) : (LazyLayoutMeasureScope, Constraints) -> MeasureResult {

  override fun invoke(
    scope: LazyLayoutMeasureScope,
    constraints: Constraints
  ): MeasureResult {
    val draggedLineValue = draggedLine.invoke()
    val centerIndex = draggedLineValue.roundToInt()
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight
    val centerPlaceable = scope.measure(
      centerIndex,
      Constraints.fixed(layoutWidth, layoutHeight / 3)
    ).first()
    val centerOffset = layoutHeight / 3 * (draggedLineValue % 1).let {
      if (it < 0.5F) -it else 1 - it
    }
    val halfHeight = (layoutHeight - centerPlaceable.height) / 2
    val topPlaceables = Array(
      ((halfHeight + centerOffset) / centerPlaceable.height).toInt() + 1
    ) {
      val index = centerIndex - it - 1
      if (index >= 0) {
        scope.measure(index, Constraints.fixed(layoutWidth, centerPlaceable.height)).first()
      } else null
    }
    val bottomPlaceables = Array(
      ((halfHeight - centerOffset) / centerPlaceable.height).toInt() + 1
    ) {
      val index = centerIndex + it + 1
      if (index < items.size) {
        scope.measure(index, Constraints.fixed(layoutWidth, centerPlaceable.height)).first()
      } else null
    }
    parentHeight.intValue = layoutHeight
    return scope.layout(layoutWidth, layoutHeight) {
      val centerY = (halfHeight + centerOffset).roundToInt()
      centerPlaceable.place(
        x = (layoutWidth - centerPlaceable.width) / 2,
        y = centerY,
      )
      topPlaceables.forEachIndexed { index, placeable ->
        placeable?.place(
          x = (layoutWidth - placeable.width) / 2,
          y = centerY - centerPlaceable.height * (index + 1)
        )
      }
      bottomPlaceables.forEachIndexed { index, placeable ->
        placeable?.place(
          x = (layoutWidth - placeable.width) / 2,
          y = centerY + centerPlaceable.height * (index + 1)
        )
      }
    }
  }
}