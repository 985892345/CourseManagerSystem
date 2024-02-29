package com.course.components.utils.size

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/29 20:46
 */

val Int.px2dp: Dp
  @Composable
  get() = LocalDensity.current.run { toDp() }

val Float.px2dp: Dp
  @Composable
  get() = LocalDensity.current.run { toDp() }