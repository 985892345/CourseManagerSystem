package com.course.applications.local

import androidx.compose.runtime.Composable
import com.course.components.base.page.MainPageCompose
import com.course.components.utils.Utils
import com.course.pages.main.MainScreen

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 17:00
 */

object LocalApp {

  fun initApp() {
    Utils.initApp()
  }

  @Composable
  fun Content() {
    Utils.initCompose()
    MainPageCompose(MainScreen())
  }
}