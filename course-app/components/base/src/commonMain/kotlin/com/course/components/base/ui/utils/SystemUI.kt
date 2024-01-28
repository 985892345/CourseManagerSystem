package com.course.components.base.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/28 14:41
 */
object SystemUI {

  val statusBarsHeight: Dp
    @Composable
    get() = LocalDensity.current.run {
      WindowInsets.statusBars.getTop(this).toDp()
    }


}
