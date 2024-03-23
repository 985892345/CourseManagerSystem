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

  fun initApp() {
    Provider.getAllImpl(IInitialService::class).forEach {
      it.value.get().onAppInit()
    }
    platformInitApp()
  }

  @Composable
  fun initCompose() {
    AppComposeCoroutineScope = rememberCoroutineScope()
  }
}

internal expect fun platformInitApp()