package com.course.components.view.option

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/4/29 19:03
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptionScrollCompose(
  selectedLine: Animatable<Float, *>,
  options: ImmutableList<String>,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = remember {
    TextStyle(
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      color = Color.Black,
    )
  },
  selectedTextSizeRatio: Float = 1F,
  onDrag: (() -> Unit)? = null,
  onDragStart: (() -> Unit)? = null,
  onDraggedStopped: (() -> Unit)? = null,
) {
  val textStyleState = rememberUpdatedState(textStyle)
  val selectedTextSizeRatioState = rememberUpdatedState(selectedTextSizeRatio)
  val itemProvider = remember {
    OptionScrollItemProvider(
      items = options,
      textStyle = textStyleState,
      draggedLine = { selectedLine.value },
      selectedTextSizeRatio = selectedTextSizeRatioState,
    )
  }
  val parentHeight = remember { mutableIntStateOf(0) }
  val measurePolicy = remember {
    OptionScrollMeasurePolicy(
      items = options,
      draggedLine = { selectedLine.value },
      parentHeight = parentHeight,
    )
  }
  var draggedOffset by remember { mutableFloatStateOf(0F) }
  val coroutineScope = rememberCoroutineScope()
  LazyLayout(
    itemProvider = remember<() -> LazyLayoutItemProvider> { { itemProvider } },
    measurePolicy = measurePolicy,
    modifier = modifier.draggable(
      orientation = Orientation.Vertical,
      state = rememberDraggableState {
        draggedOffset = (draggedOffset - it)
          .coerceIn(0F, parentHeight.intValue / 3F * (options.size - 1).coerceAtLeast(0))
        coroutineScope.launch {
          selectedLine.snapTo(draggedOffset / (parentHeight.intValue / 3F))
          onDrag?.invoke()
        }
      },
      onDragStarted = {
        coroutineScope.launch {
          selectedLine.stop()
          draggedOffset = selectedLine.value * (parentHeight.intValue / 3F)
          onDragStart?.invoke()
        }
      },
      onDragStopped = {
        coroutineScope.launch {
          selectedLine.animateTo(selectedLine.value.roundToInt().toFloat())
          onDraggedStopped?.invoke()
        }
      },
    ).clipToBounds(),
  )
}