package com.course.components.utils.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 13:45
 */

actual val AppCoroutineScope: CoroutineScope
  get() = AppComposeCoroutineScope
