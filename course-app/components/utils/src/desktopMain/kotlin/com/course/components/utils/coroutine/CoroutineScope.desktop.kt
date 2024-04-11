package com.course.components.utils.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 13:45
 */

actual val AppCoroutineScope: CoroutineScope
  get() = AppCoroutineScopeInternal

private lateinit var AppCoroutineScopeInternal: CoroutineScope

fun runApp(block: suspend CoroutineScope.() -> Unit) {
  runBlocking {
    // AppCoroutineScopeInternal 使用 SupervisorJob 避免异常传播
    val supervisor = SupervisorJob(coroutineContext[Job])
    val coroutineScope = CoroutineScope(supervisor)
    AppCoroutineScopeInternal = coroutineScope
    block()
  }
  exitProcess(0)
}
