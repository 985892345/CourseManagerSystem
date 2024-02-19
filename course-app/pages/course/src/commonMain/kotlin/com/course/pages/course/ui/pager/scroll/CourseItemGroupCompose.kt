package com.course.pages.course.ui.pager.scroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.course.components.utils.time.Today
import com.course.components.utils.time.diffDays
import com.course.pages.course.ui.pager.CoursePagerCombine

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:41
 */
@Composable
fun CoursePagerCombine.CourseItemGroupCompose(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = Modifier.then(modifier)
  ) {
    if (monDate != null && Today.diffDays(monDate) in 0..6) {
      TodayBackgroundCompose()
    }
  }
}

@Composable
private fun TodayBackgroundCompose() {
  Row {
    val startWeight = Today.dayOfWeek.ordinal
    if (startWeight > 0) {
      Spacer(modifier = Modifier.weight(startWeight.toFloat()))
    }
    Spacer(
      modifier = Modifier.weight(1F)
        .fillMaxHeight()
        .background(color = Color(0x93E8F0FC))
    )
    val endWeight = 6 - Today.dayOfWeek.ordinal
    if (endWeight > 0) {
      Spacer(modifier = Modifier.weight(endWeight.toFloat()))
    }
  }
}
