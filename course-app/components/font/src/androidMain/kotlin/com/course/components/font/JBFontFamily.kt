package com.course.components.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.course.app.components.font.R
import com.g985892345.provider.api.annotation.ImplProvider
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.compose.ui.text.font.Font

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 23:47
 */
@ImplProvider
object JBFontFamilyAndroid : JBFontFamilyPlatform {
  @Composable
  override fun get(): FontFamily {
    return FontFamily(
      Font(R.font.jb_thin, weight = FontWeight.Thin),
      Font(R.font.jb_thin_italic, weight = FontWeight.Thin, style = FontStyle.Italic),
      Font(R.font.jb_extra_light, weight = FontWeight.ExtraLight),
      Font(R.font.jb_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
      Font(R.font.jb_light, weight = FontWeight.Light),
      Font(R.font.jb_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
      Font(R.font.jb_regular, weight = FontWeight.Normal),
      Font(R.font.jb_regular_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
      Font(R.font.jb_medium, weight = FontWeight.Medium),
      Font(R.font.jb_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
      Font(R.font.jb_semi_bold, weight = FontWeight.SemiBold),
      Font(R.font.jb_semi_bold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
      Font(R.font.jb_bold, weight = FontWeight.Bold),
      Font(R.font.jb_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
      Font(R.font.jb_extra_bold, weight = FontWeight.ExtraBold),
      Font(R.font.jb_extra_bold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
      Font(R.font.jb_extra_bold, weight = FontWeight.Black),
      Font(R.font.jb_extra_bold_italic, weight = FontWeight.Black, style = FontStyle.Italic),
    )
  }
}