package com.course.components.utils.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.screen.Screen
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * @date 2024/3/4 10:37
 */

/**
 * 用于跨模块标记页面入口，如果只是模块里面的私有界面，使用 [Screen] 即可
 *
 * 有如下约定:
 * - 只能是顶级函数
 * - 有且只能有一个函数参数，并且是 [RemoteScreen] 子类，KSP 会自动生成映射代码
 *
 * 需要在 build.gradle.kts 中添加 id("app.function.navigator") 插件
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RemoteScreenEnter

/**
 * Composable 页面入口函数的数据类，即页面启动参数
 *
 * ```
 * // 实现模块中
 * @RemoteScreenEnter
 * fun XXXRemoteScreenEnter(screen: XXXRemoteScreen) {
 *   // ...
 * }
 *
 * // 之后会由 ksp 生成该函数与 XXXRemoteScreen 关联的代码
 * ```
 *
 * 有如下约定:
 * - 必须打上 [ObjectSerializable] 注解，因此所有变量需要支持序列化
 * - 其子类建议统一放在 base 模块 /navigator/screen 下
 */
@Stable
abstract class RemoteScreen : BaseScreen() {
  @Composable
  final override fun ScreenContent() {
    RemoteScreenEnterCollector.getScreenContent(this::class).invoke(this)
  }
}

// 由 ksp 实现
interface RemoteScreenEnterCollector {
  val collected: Map<KClass<out RemoteScreen>, @Composable (RemoteScreen) -> Unit>

  companion object {
    private val AllImpl: Map<KClass<out RemoteScreen>, @Composable (RemoteScreen) -> Unit> by lazy {
      val collected =
        Provider.getAllImpl(RemoteScreenEnterCollector::class).map { it.value.get().collected }
      buildMap {
        collected.forEach { map ->
          map.forEach {
            check(put(it.key, it.value) == null) {
              "${it.key} 被作为 @ScreenEnter 函数参数多次使用"
            }
          }
        }
      }
    }

    internal fun getScreenContent(data: KClass<out RemoteScreen>): @Composable (RemoteScreen) -> Unit {
      return AllImpl[data]!!
    }
  }
}
