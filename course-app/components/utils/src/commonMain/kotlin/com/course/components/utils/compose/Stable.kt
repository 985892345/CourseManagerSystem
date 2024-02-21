package com.course.components.utils.compose

import androidx.compose.runtime.Stable

/**
 * 包一层变成稳定类型，为了让第三方类能过编译
 *
 * 目前 compiler 1.5 添加了 stabilityConfigurationPath 参数，
 * 但是 Compose Multiplatform 没有加这个配置
 *
 * @author 985892345
 * @date 2024/2/19 16:30
 */
@Stable
data class Stab<T>(val le: T)

fun <T> T.stable(): Stab<T> = Stab(this)