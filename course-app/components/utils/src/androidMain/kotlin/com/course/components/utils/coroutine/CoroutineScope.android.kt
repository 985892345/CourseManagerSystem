package com.course.components.utils.coroutine

import com.g985892345.android.extensions.android.processLifecycleScope
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/19 13:45
 */

actual val AppCoroutineScope: CoroutineScope
  get() = processLifecycleScope