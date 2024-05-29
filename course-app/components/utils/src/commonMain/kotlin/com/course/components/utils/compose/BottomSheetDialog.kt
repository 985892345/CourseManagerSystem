package com.course.components.utils.compose

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastFirst
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.navigator.mainNavigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 在底部显示的弹窗
 *
 * @author 985892345
 * 2024/4/15 20:43
 */
fun showBottomSheetWindow(
  dismissOnBackPress: (dismiss: () -> Unit) -> Boolean = { true },
  dismissOnClickOutside: (dismiss: () -> Unit) -> Boolean = { true },
  scrimColor: Color = Color.Transparent.copy(alpha = 0.6F),
  content: @Composable BottomSheetScope.(dismiss: () -> Unit) -> Unit
) {
  var height by mutableFloatStateOf(0F)
  var offsetY by mutableFloatStateOf(0F)
  val dragValueChannel = Channel<BottomSheetDragValue?>(1, BufferOverflow.DROP_OLDEST)

  fun dragStopped(velocity: Float) {
    if (velocity > 1000) {
      dragValueChannel.trySend(BottomSheetDragValue(offsetY, height, velocity))
    } else if (velocity < -1000) {
      dragValueChannel.trySend(BottomSheetDragValue(offsetY, 0F, velocity))
    } else if (offsetY > height / 2) {
      dragValueChannel.trySend(BottomSheetDragValue(offsetY, height, velocity))
    } else {
      dragValueChannel.trySend(BottomSheetDragValue(offsetY, 0F, velocity))
    }
  }

  var isDismissed = false
  val dismiss: () -> Unit = {
    isDismissed = true
    dragValueChannel.trySend(BottomSheetDragValue(offsetY, height))
  }
  (mainNavigator.lastItem as BaseScreen).showWindow(dismissOnBackPress) { windowDismiss ->
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding().focusRequester(focusRequester).focusable().onKeyEvent {
      if (it.type == KeyEventType.KeyDown && it.key == Key.Escape && dismissOnBackPress(dismiss)) {
        if (!isDismissed) {
          dismiss.invoke()
        }
        true
      } else false
    }) {
      Spacer(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(pass = PointerEventPass.Initial)
          down.consume()
          while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val pointer = event.changes.fastFirst { it.id == down.id }
            pointer.consume()
            if (pointer.changedToUpIgnoreConsumed()) {
              if (dismissOnClickOutside(dismiss)) {
                dismiss.invoke()
              }
              break
            }
          }
        }
      }.graphicsLayer {
        alpha = if (height == 0F) 0F else (1 - offsetY / height).coerceIn(0F, 1F)
      }.background(scrimColor))
      Box(
        modifier = Modifier.align(Alignment.BottomStart)
          .fillMaxWidth()
          .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
              if (height == 0F) {
                height = placeable.height.toFloat()
                offsetY = height
                dragValueChannel.trySend(BottomSheetDragValue(placeable.height.toFloat(), 0F))
              }
              placeable.place(0, offsetY.roundToInt())
            }
          }.nestedScroll(object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
              if (offsetY != 0F) {
                // available 手指向上移动为负
                val newOffset = (offsetY + available.y).coerceIn(0F, height)
                offsetY = newOffset
                return available
              }
              return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
              consumed: Offset,
              available: Offset,
              source: NestedScrollSource
            ): Offset {
              if (offsetY == 0F && available.y > 0 && consumed.y == 0F) {
                val newOffset = (offsetY + available.y).coerceIn(0F, height)
                offsetY = newOffset
                return Offset(0F, newOffset - offsetY)
              }
              return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
              if (offsetY != height) {
                dragStopped(available.y)
                return available
              }
              return super.onPreFling(available)
            }
          })
      ) {
        val scope = object : BottomSheetScope {
          @Composable
          override fun Modifier.bottomSheetDraggable(): Modifier {
            return draggable(
              orientation = Orientation.Vertical,
              state = rememberDraggableState {
                val newOffset = offsetY + it
                offsetY = if (newOffset < 0) 0F else newOffset
              },
              onDragStarted = {
                dragValueChannel.trySend(null)
              },
              onDragStopped = { velocity ->
                dragStopped(velocity)
              }
            )
          }
        }
        content(scope, dismiss)
      }
      LaunchedEffect(Unit) {
        var prevVelocity = 0F
        var job: Job? = null
        while (true) {
          val dragValue = dragValueChannel.receive()
          job?.cancel()
          job = null
          if (dragValue != null) {
            job = launch {
              animate(
                initialValue = dragValue.initialValue,
                targetValue = dragValue.targetValue,
                initialVelocity = dragValue.initialVelocity(prevVelocity),
              ) { value, velocity ->
                prevVelocity = velocity
                offsetY = value.coerceAtLeast(0F)
              }
              delay(100) // 等待后台其他任务执行完毕
              if (dragValue.targetValue == height) {
                windowDismiss()
              }
            }
          }
        }
      }
    }
  }
}

interface BottomSheetScope {
  @Composable
  fun Modifier.bottomSheetDraggable(): Modifier
}

private class BottomSheetDragValue(
  val initialValue: Float,
  val targetValue: Float,
  private val initialVelocity: Float = Float.POSITIVE_INFINITY,
) {
  fun initialVelocity(default: Float): Float =
    if (initialVelocity == Float.POSITIVE_INFINITY) default else initialVelocity
}


