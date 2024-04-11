package com.course.components.utils.compose

import androidx.compose.runtime.Stable

/**
 * .
 *
 * @author 985892345
 * 2024/4/10 19:03
 */

// 一个简单的包装类，用于 compose 中保存数据
@Stable
data class Wrapper<T>(var value: T)