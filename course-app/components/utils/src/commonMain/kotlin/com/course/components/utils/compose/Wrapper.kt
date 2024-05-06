package com.course.components.utils.compose

import androidx.compose.runtime.Stable
import kotlin.reflect.KProperty

/**
 * .
 *
 * @author 985892345
 * 2024/4/10 19:03
 */

// 一个简单的包装类，用于 compose 中保存数据
@Stable
data class Wrapper<T>(var value: T)

operator fun <T> Wrapper<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
  return value
}

operator fun <T> Wrapper<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
  this.value = value
}
