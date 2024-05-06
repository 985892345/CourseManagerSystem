package com.course.components.utils.list

import androidx.compose.ui.util.fastForEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * .
 *
 * @author 985892345
 * 2024/5/4 11:30
 */

@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastSumByFloat(selector: (T) -> Float): Float {
  contract { callsInPlace(selector) }
  var sum = 0F
  fastForEach { element ->
    sum += selector(element)
  }
  return sum
}