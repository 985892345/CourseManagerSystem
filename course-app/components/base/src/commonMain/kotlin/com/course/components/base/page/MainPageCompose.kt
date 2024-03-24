package com.course.components.base.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorSaver
import cafe.adriel.voyager.transitions.SlideTransition
import com.course.components.base.theme.AppTheme
import com.course.components.base.ui.dialog.DialogCompose
import com.course.components.base.ui.toast.ToastCompose
import com.course.components.utils.serializable.ObjectSerializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 19:13
 */

lateinit var mainNavigator: Navigator
  private set

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun MainPageCompose(screen: Screen) {
  AppTheme {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
      CompositionLocalProvider(
        LocalNavigatorSaver provides NavigatorSaver
      ) {
        Navigator(screen = screen) {
          mainNavigator = it
          SlideTransition(it)
        }
      }
    }
    DialogCompose()
    ToastCompose()
  }
}

@OptIn(ExperimentalVoyagerApi::class, InternalVoyagerApi::class)
private val NavigatorSaver: NavigatorSaver<Any> =
  NavigatorSaver { _, key, stateHolder, disposeBehavior, parent ->
    listSaver(
      save = { navigator ->
        navigator.items.map {
          if (ObjectSerializable.isSerializable(it::class)) {
            ObjectSerializable.serialize(it)
          } else throw RuntimeException("${it::class} 不支持序列化, 请打上 @ObjectSerializable 注解")
        }
      },
      restore = { items ->
        val screens = items.map { ObjectSerializable.deserialize(it) as Screen }
        Navigator(screens, key, stateHolder, disposeBehavior, parent)
      }
    )
  }