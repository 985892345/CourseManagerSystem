package com.course.components.view.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.base.theme.LocalAppColors

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 21:56
 */
@Composable
fun Modifier.drawRequiredPoint(paddingRight: Dp = 5.dp): Modifier {
  val red = LocalAppColors.current.red
  return this.drawBehind {
    val right = paddingRight.toPx()
    val width = 10F
    val half = width / 2
    val sqrt = half / 1.414215F
    drawLine(
      color = red,
      start = Offset(size.width - width - right, half),
      end = Offset(size.width - right, half)
    )
    drawLine(
      color = red,
      start = Offset(size.width - half - right, 0F),
      end = Offset(size.width - half - right, width)
    )
    drawLine(
      color = red,
      start = Offset(size.width - half - sqrt - right, half - sqrt),
      end = Offset(size.width - (half - sqrt) - right, half + sqrt)
    )
    drawLine(
      color = red,
      start = Offset(size.width - half - sqrt - right, half + sqrt),
      end = Offset(size.width - (half - sqrt) - right, half - sqrt)
    )
  }
}