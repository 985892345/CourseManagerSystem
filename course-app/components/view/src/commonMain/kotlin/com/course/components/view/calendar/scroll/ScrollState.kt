package com.course.components.view.calendar.scroll

import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/27 13:43
 */
@Stable
sealed interface VerticalScrollState {
  val offset: Float
  data object Collapsed : VerticalScrollState {
    override val offset: Float
      get() = 0F
  }

  @JvmInline
  value class Scrolling(override val offset: Float) : VerticalScrollState

  @JvmInline
  value class Expanded(override val offset: Float) : VerticalScrollState
}

@Stable
sealed interface HorizontalScrollState {
  val offset: Float
  data object Idle : HorizontalScrollState {
    override val offset: Float
      get() = 0F
  }

  @JvmInline
  value class Scrolling(override val offset: Float) : HorizontalScrollState
}