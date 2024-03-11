package com.course.applications.pro

import androidx.compose.runtime.Composable
import com.course.components.base.page.MainPageCompose
import com.course.components.utils.Utils
import com.course.pages.main.MainScreen
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 17:00
 */

object ProApp {

  fun initApp() {
    ProKtProviderInitializer.tryInitKtProvider()
    Utils.initApp()
  }

  @Composable
  fun Content() {
    MainPageCompose(MainScreen)
  }
}