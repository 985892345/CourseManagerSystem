package com.course.components.base.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
  CompositionLocalProvider(
    LocalAppDarkTheme provides darkTheme,
    LocalAppColors provides if (darkTheme) AppDarkColor else AppLightColor,
  ) {
    ConfigAppTheme(darkTheme) {
      MaterialTheme(
        colors = if (darkTheme) DarkColor else LightColor,
        typography = Typography,
        shapes = Shapes,
        content = content,
      )
    }
  }
}

val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
expect fun ConfigAppTheme(darkTheme: Boolean, content: @Composable () -> Unit)

private val LightColor = lightColors(
)

private val DarkColor = darkColors(
)

private val Typography = Typography(
  body1 = TextStyle.Default.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    letterSpacing = 0.5.sp
  )
)

private val Shapes = Shapes(
)