package com.course.components.font

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.course.components.utils.provider.Provider
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

/**
 * 因为 Compose Multiplatform 加载字体时在 Android 端会闪退，官方并未修护，暂时这样解决
 * https://github.com/JetBrains/compose-multiplatform/issues/3472
 *
 * @author 985892345
 * 2024/3/24 23:47
 */
@Stable
@Composable
fun JBFontFamily(): FontFamily {
  return Provider.implOrNull(JBFontFamilyPlatform::class)?.get() ?: JBFontFamilyDefault()
}

internal interface JBFontFamilyPlatform {
  @Stable
  @Composable
  fun get(): FontFamily
}

@OptIn(ExperimentalResourceApi::class)
@Stable
@Composable
private fun JBFontFamilyDefault(): FontFamily {
  return FontFamily(
    Font(FontResource("font/jb_thin.ttf"), weight = FontWeight.Thin),
    Font(FontResource("font/jb_thin_italic.ttf"), weight = FontWeight.Thin, style = FontStyle.Italic),
    Font(FontResource("font/jb_extra_light.ttf"), weight = FontWeight.ExtraLight),
    Font(FontResource("font/jb_extra_light_italic.ttf"), weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(FontResource("font/jb_light.ttf"), weight = FontWeight.Light),
    Font(FontResource("font/jb_light_italic.ttf"), weight = FontWeight.Light, style = FontStyle.Italic),
    Font(FontResource("font/jb_regular.ttf"), weight = FontWeight.Normal),
    Font(FontResource("font/jb_regular_italic.ttf"), weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(FontResource("font/jb_medium.ttf"), weight = FontWeight.Medium),
    Font(FontResource("font/jb_medium_italic.ttf"), weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(FontResource("font/jb_semi_bold.ttf"), weight = FontWeight.SemiBold),
    Font(FontResource("font/jb_semi_bold_italic.ttf"), weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(FontResource("font/jb_bold.ttf"), weight = FontWeight.Bold),
    Font(FontResource("font/jb_bold_italic.ttf"), weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(FontResource("font/jb_extra_bold.ttf"), weight = FontWeight.ExtraBold),
    Font(FontResource("font/jb_extra_bold_italic.ttf"), weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(FontResource("font/jb_extra_bold.ttf"), weight = FontWeight.Black),
    Font(FontResource("font/jb_extra_bold_italic.ttf"), weight = FontWeight.Black, style = FontStyle.Italic),
  )
}