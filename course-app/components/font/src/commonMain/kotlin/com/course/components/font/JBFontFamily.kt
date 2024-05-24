package com.course.components.font

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import coursemanagersystem.course_app.components.font.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

/**
 *
 *
 * @author 985892345
 * 2024/3/24 23:47
 */
@Stable
@Composable
fun JBFontFamily(): FontFamily {
  return JBFontFamilyDefault()
}

@OptIn(ExperimentalResourceApi::class)
@Stable
@Composable
private fun JBFontFamilyDefault(): FontFamily {
  return FontFamily(
    Font(Res.font.jb_thin, weight = FontWeight.Thin),
    Font(Res.font.jb_thin_italic, weight = FontWeight.Thin, style = FontStyle.Italic),
    Font(Res.font.jb_extra_light, weight = FontWeight.ExtraLight),
    Font(Res.font.jb_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(Res.font.jb_light, weight = FontWeight.Light),
    Font(Res.font.jb_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(Res.font.jb_regular, weight = FontWeight.Normal),
    Font(Res.font.jb_regular_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(Res.font.jb_medium, weight = FontWeight.Medium),
    Font(Res.font.jb_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(Res.font.jb_semi_bold, weight = FontWeight.SemiBold),
    Font(Res.font.jb_semi_bold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(Res.font.jb_bold, weight = FontWeight.Bold),
    Font(Res.font.jb_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(Res.font.jb_extra_bold, weight = FontWeight.ExtraBold),
    Font(Res.font.jb_extra_bold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(Res.font.jb_extra_bold, weight = FontWeight.Black),
    Font(Res.font.jb_extra_bold_italic, weight = FontWeight.Black, style = FontStyle.Italic),
  )
}