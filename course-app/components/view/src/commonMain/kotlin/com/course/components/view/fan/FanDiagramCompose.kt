package com.course.components.view.fan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import com.course.components.utils.list.fastSumByFloat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 19:26
 */
@Composable
fun FanDiagramCompose(
  data: ImmutableList<FanDiagramData>,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.width.coerceAtMost(size.height) / 2f

    drawCircle(color = Color.White, radius = radius)

    var startAngle = -90F
    val total = data.fastSumByFloat { it.size }
    if (total == 0F) return@Canvas

    data.fastForEach { data ->
      val sweepAngle = (data.sizeAnim.value / total) * 360F
      val endAngle = startAngle + sweepAngle
      drawArc(
        color = data.color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2),
      )
      startAngle = endAngle
    }
  }
  LaunchedEffect(data) {
    data.fastForEach {
      if (it.sizeAnim.value != it.size) {
        launch {
          it.sizeAnim.animateTo(it.size, tween(1000))
        }
      }
    }
  }
}

class FanDiagramData(
  val size: Float,
  val color: Color,
  val isNeedAnim: Boolean,
) {
  internal val sizeAnim = Animatable(if (isNeedAnim) 0F else size)
}