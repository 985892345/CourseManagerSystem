package com.course.source.app.web.request

import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * 2024/4/13 10:36
 */
sealed interface IRequestSource {
  val key: String

  @Composable
  fun CardContent()
}