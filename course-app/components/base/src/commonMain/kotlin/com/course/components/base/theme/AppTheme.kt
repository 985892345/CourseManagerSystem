package com.course.components.base.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 16:39
 */

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  ConfigAppTheme(darkTheme)
  MaterialTheme(
    colors = if (darkTheme) DarkColor else LightColor,
    typography = Typography,
    shapes = Shapes,
    content = content,
  )
}

@Composable
expect fun ConfigAppTheme(darkTheme: Boolean)

private val LightColor = lightColors(
)

private val DarkColor = darkColors(
)

private val Typography = Typography(
)

private val Shapes = Shapes(
)