package com.course.components.utils.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

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
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  enabled: Boolean = true,
  onClickLabel: String? = null,
  role: Role? = null,
  onClick: () -> Unit
) = clickable(
  interactionSource = interactionSource,
  indication = null,
  enabled = enabled,
  onClickLabel = onClickLabel,
  role = role,
  onClick = onClick
)