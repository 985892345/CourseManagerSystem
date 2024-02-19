package com.course.components.base.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.course.components.base.navigator.CurrentScreen
import com.course.components.base.navigator.Navigator
import com.course.components.base.navigator.Screen
import com.course.components.base.ui.dialog.DialogCompose
import com.course.components.base.ui.toast.ToastCompose

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 19:13
 */

lateinit var mainScreen: Screen
  private set

lateinit var mainNavigator: Navigator
  private set

@Composable
fun MainPageCompose(screen: Screen) {
  mainScreen = screen
  Navigator(screen = screen) {
    mainNavigator = it
    Box(modifier = Modifier.fillMaxSize()) {
      CurrentScreen()
      DialogCompose()
      ToastCompose()
    }
  }
}