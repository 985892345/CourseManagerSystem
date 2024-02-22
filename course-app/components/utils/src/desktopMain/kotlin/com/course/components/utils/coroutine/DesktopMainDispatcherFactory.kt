package com.course.components.utils.coroutine

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.internal.MainDispatcherFactory
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 13:45
 */
@OptIn(InternalCoroutinesApi::class)
class DesktopMainDispatcherFactory : MainCoroutineDispatcher(), MainDispatcherFactory {

  override val loadPriority: Int
    get() = Int.MAX_VALUE

  override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher {
    return this
  }

  override val immediate: MainCoroutineDispatcher
    get() = this

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    AppCoroutineScope.launch {
      block.run()
    }
  }
}