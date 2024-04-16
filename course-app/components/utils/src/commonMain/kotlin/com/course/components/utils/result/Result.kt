package com.course.components.utils.result

import kotlinx.coroutines.CancellationException

/**
 * .
 *
 * @author 985892345
 * 2024/4/16 00:05
 */

fun <T> Result<T>.tryThrowCancellationException(): Result<T> {
  return onFailure {
    if (it is CancellationException) throw it
  }
}