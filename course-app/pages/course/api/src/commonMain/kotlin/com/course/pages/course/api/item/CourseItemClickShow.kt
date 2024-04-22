package com.course.pages.course.api.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst

/**
 * .
 *
 * @author 985892345
 * 2024/4/15 20:43
 */
interface CourseItemClickShow {
  fun showItemDetail(item: ICourseItem, content: @Composable () -> Unit)
  fun cancelShow()
}

/**
 * 默认在底部显示的弹窗
 */
@Composable
fun CourseBottomSheetItemClickShow(
  state: CourseBottomSheetState = remember { CourseBottomSheetState() },
  modifier: Modifier = Modifier,
  content: @Composable (CourseItemClickShow) -> Unit
) {
  Box(modifier = modifier) {
    Box(modifier = Modifier.pointerInput(Unit) {
      awaitEachGesture {
        // 拦截点击事件
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        if (state.visibility.value) {
          down.consume()
          while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val pointer = event.changes.fastFirst { it.id == down.id }
            pointer.consume()
            if (pointer.changedToUpIgnoreConsumed()) {
              state.cancelShow()
              break
            }
          }
        }
      }
    }) {
      content(state)
    }
    AnimatedVisibility(
      modifier = Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.BottomEnd),
      visible = state.visibility.value,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
    ) {
      Card(
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
      ) {
        state.content.value?.invoke()
      }
      DisposableEffect(Unit) {
        onDispose {
          state.endShow()
        }
      }
    }
  }
}

open class CourseBottomSheetState : CourseItemClickShow {

  val content = mutableStateOf<(@Composable () -> Unit)?>(null)

  val visibility = mutableStateOf(false)

  override fun showItemDetail(item: ICourseItem, content: @Composable () -> Unit) {
    if (this.content.value == null) {
      startShow()
    }
    this.content.value = content
  }

  open fun startShow() {
    this.visibility.value = true
  }

  override fun cancelShow() {
    visibility.value = false
  }

  open fun endShow() {
    content.value = null
  }
}

