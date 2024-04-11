package com.course.components.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.components.utils.init.IInitialService
import com.course.components.utils.provider.Provider

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 17:09
 */
object Utils {

  private val initialServices = Provider.getAllImpl(IInitialService::class).map { it.value.get() }

  fun initApp() {
    initialServices.forEach {
      it.onAppInit()
    }
    platformInitApp()
  }

  @Composable
  fun initCompose() {
    AppComposeCoroutineScope = rememberCoroutineScope()
    initialServices.forEach {
      it.onComposeInit()
    }
  }
}

internal expect fun platformInitApp()