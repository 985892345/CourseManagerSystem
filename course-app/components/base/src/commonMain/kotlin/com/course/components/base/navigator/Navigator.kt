package com.course.components.base.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import cafe.adriel.voyager.navigator.Navigator as InnerNavigator

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/29 11:01
 */

val LocalNavigator: ProvidableCompositionLocal<Navigator> = staticCompositionLocalOf { error("") }

@Composable
fun Navigator(
  screen: Screen,
  content: @Composable (Navigator) -> Unit = { CurrentScreen() }
) {
  InnerNavigator(
    screen = ScreenWrapper(screen),
    onBackPressed = {
      (it as ScreenWrapper).onBackPressed()
    },
    content = {
      val navigator = Navigator(it)
      CompositionLocalProvider(
        LocalNavigator provides navigator
      ) {
        content(navigator)
      }
    }
  )
}

@Composable
fun CurrentScreen() {
  cafe.adriel.voyager.navigator.CurrentScreen()
}

class Navigator(
  internal val innerNavigator: InnerNavigator,
) {
  val items: List<Screen> get() = innerNavigator.items.map { (it as ScreenWrapper).screen }

  val lastItemOrNull: Screen? get() = (innerNavigator.lastItemOrNull as ScreenWrapper?)?.screen

  val size: Int get() = innerNavigator.size

  val isEmpty: Boolean get() = innerNavigator.isEmpty

  val canPop: Boolean get() = innerNavigator.canPop

  @Composable
  fun registerOnBackPressed(action: (CoroutineScope.() -> Boolean)? = null) {
    lastItemOrNull?.registerOnBackPressed(action)
  }

  infix fun push(item: Screen) {
    innerNavigator.push(ScreenWrapper(item))
  }

  infix fun push(items: List<Screen>) {
    innerNavigator.push(items.map { ScreenWrapper(it) })
  }

  infix fun replace(item: Screen) {
    innerNavigator.replace(ScreenWrapper(item))
  }

  infix fun replaceAll(item: Screen) {
    innerNavigator.replaceAll(ScreenWrapper(item))
  }

  infix fun replaceAll(items: List<Screen>) {
    innerNavigator.replaceAll(items.map { ScreenWrapper(it) })
  }

  fun pop(): Boolean {
    return innerNavigator.pop()
  }

  fun popAll() {
    innerNavigator.popAll()
  }

  infix fun popUntil(predicate: (Screen) -> Boolean): Boolean {
    return innerNavigator.popUntil { predicate((it as ScreenWrapper).screen) }
  }

  operator fun plusAssign(item: Screen) {
    innerNavigator.plusAssign(ScreenWrapper(item))
  }

  operator fun plusAssign(items: List<Screen>) {
    innerNavigator.plusAssign(items.map { ScreenWrapper(it) })
  }
}

interface Screen {
  @Composable
  fun Content()
}

@Composable
fun Screen.registerOnBackPressed(action: (CoroutineScope.() -> Boolean)? = null) {
  val navigator = LocalNavigator.current
  val coroutineScope = rememberCoroutineScope()
  DisposableEffect(action) {
    if (action == null) return@DisposableEffect onDispose {  }
    val actionWrapper: () -> Boolean = { action(coroutineScope) }
    val screenWrapper = navigator.innerNavigator.items.find {
      (it as ScreenWrapper).screen === this@registerOnBackPressed
    } as ScreenWrapper
    screenWrapper.onBackPressedList.add(actionWrapper)
    onDispose {
      screenWrapper.onBackPressedList.remove(actionWrapper)
    }
  }
}

private class ScreenWrapper(
  val screen: Screen
) : cafe.adriel.voyager.core.screen.Screen {

  val onBackPressedList = mutableListOf<() -> Boolean>()

  @Composable
  override fun Content() {
    screen.Content()
  }

  fun onBackPressed(): Boolean {
    // 创建新集合，防止并发修改
    onBackPressedList.reversed().forEach {
      if (!it.invoke()) {
        return false
      }
    }
    return true
  }
}