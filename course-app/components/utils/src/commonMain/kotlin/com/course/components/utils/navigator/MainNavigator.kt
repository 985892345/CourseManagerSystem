package com.course.components.utils.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.listSaver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorSaver
import cafe.adriel.voyager.transitions.SlideTransition
import com.course.components.utils.serializable.ObjectSerializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 20:23
 */

lateinit var mainNavigator: Navigator
  private set

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun MainNavigator(screen: BaseScreen) {
  CompositionLocalProvider(
    LocalNavigatorSaver provides NavigatorSaver
  ) {
    Navigator(
      screen = screen,
      onBackPressed = { sc ->
        if (sc is BaseScreen) {
          sc.backHandleList.asReversed().all {
            if (it.enabled) {
              it.onBackPressed.invoke()
              false
            } else true
          }
        } else true
      }
    ) {
      mainNavigator = it
      SlideTransition(it)
    }
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