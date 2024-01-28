package com.course.components.base.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/28 11:09
 */

val LocalAppColors: ProvidableCompositionLocal<AppColor> = staticCompositionLocalOf {
  AppLightColor
}

sealed class AppColor(
  val tvLv1: Color = Color(0xFF112C54),
  val tvLv2: Color = Color(0xFF112C57),
  val tvLv3: Color = Color(0xFF15315B),
  val tvLv4: Color = Color(0xFF2A4E84),
)

data object AppLightColor : AppColor()

data object AppDarkColor : AppColor(
  tvLv1 = Color(0xFFFFFFFF),
  tvLv2 = Color(0xFFFFFFFF),
  tvLv3 = Color(0xFFFFFFFF),
  tvLv4 = Color(0xFFFFFFFF),
)