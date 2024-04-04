package com.course.components.utils.compose

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 19:38
 */

/**
 * 点击不带虚影的 clickable
 */
@Composable
fun Modifier.clickableNoIndicator(
  enabled: Boolean = true,
  onClickLabel: String? = null,
  role: Role? = null,
  onClick: () -> Unit
) = clickable(
  interactionSource = remember { MutableInteractionSource() },
  indication = null,
  enabled = enabled,
  onClickLabel = onClickLabel,
  role = role,
  onClick = onClick
)

@Composable
fun Modifier.clickableCardIndicator(
  radius: Dp = 8.dp,
  enabled: Boolean = true,
  onClickLabel: String? = null,
  role: Role? = null,
  onClick: () -> Unit
) = clickable(
  interactionSource = remember { MutableInteractionSource() },
  indication = CardIndicationMap.getOrPut(radius) { CardIndication(radius) },
  enabled = enabled,
  onClickLabel = onClickLabel,
  role = role,
  onClick = onClick
)

private val CardIndicationMap = hashMapOf<Dp, CardIndication>()

private class CardIndication(val radius: Dp) : Indication {

  class CardIndicationInstance(
    private val isPressed: State<Boolean>,
    private val isHovered: State<Boolean>,
    private val isFocused: State<Boolean>,
    private val cornerRadius: CornerRadius,
  ) : IndicationInstance {
    override fun ContentDrawScope.drawIndication() {
      drawContent()
      if (isPressed.value) {
        drawRoundRect(color = Color.Black.copy(alpha = 0.3f), size = size, cornerRadius = cornerRadius)
      } else if (isHovered.value || isFocused.value) {
        drawRoundRect(color = Color.Black.copy(alpha = 0.1f), size = size, cornerRadius = cornerRadius)
      }
    }
  }

  @Composable
  override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
    val isPressed = interactionSource.collectIsPressedAsState()
    val isHovered = interactionSource.collectIsHoveredAsState()
    val isFocused = interactionSource.collectIsFocusedAsState()
    val cornerRadius = LocalDensity.current.run { CornerRadius(radius.toPx()) }
    return remember(interactionSource, cornerRadius) {
      CardIndicationInstance(isPressed, isHovered, isFocused, cornerRadius)
    }
  }
}