package com.course.applications.pro

import androidx.compose.runtime.Composable
import com.course.components.base.page.MainPageCompose
import com.course.components.utils.Utils
import com.course.pages.login.LoginScreen
import com.course.pages.main.MainScreen
import com.course.pages.main.sHasLogin

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 17:00
 */

object ProApp {

  fun initApp() {
    Utils.initApp()
  }

  @Composable
  fun Content() {
    Utils.initCompose()
    MainPageCompose(if (sHasLogin) MainScreen() else LoginScreen())
  }
}