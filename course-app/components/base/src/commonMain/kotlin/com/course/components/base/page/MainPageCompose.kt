package com.course.components.base.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.course.components.base.ui.dialog.DialogCompose
import com.course.components.base.ui.toast.ToastCompose

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 19:13
 */

val mainScreen: Screen
  get() = mainScreenInternal!!

private var mainScreenInternal: Screen? = null

val mainNavigator: Navigator
  get() = mainNavigatorInternal!!

private var mainNavigatorInternal: Navigator? = null

@Composable
fun MainPageCompose(mainScreen: Screen) {
  mainScreenInternal = mainScreen
  Navigator(mainScreen) {
    mainNavigatorInternal = LocalNavigator.currentOrThrow
    Box(modifier = Modifier.fillMaxSize()) {
      CurrentScreen()
      DialogCompose()
      ToastCompose()
    }
  }
}