package com.course.applications.pro

import androidx.compose.runtime.Composable
import com.course.components.base.page.MainPageCompose
import com.course.components.base.theme.AppTheme

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 16:22
 */
@Composable
fun ProScreenCompose() {
  AppTheme(darkTheme = false) {
    MainPageCompose(ProMainScreen)
  }
}

