package com.course.components.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * Compose 组件的实现类写为一个抽象类，然后实现 [Content] 函数用于描述组件
 *
 * 使用抽象类的原因:
 * - 抽象类的是为了统一输入源，而不是采取函数参数的形式进行输入，在一个复杂的 Compose 页面，会存在很多参数，
 *   并且有时候 Compose 跨越层级太多，参数需要穿通多层很不方便，所以使用抽象类统一输入源
 * - 使用数据类也可以统一输入源，但数据类会遇到一个问题，可观察对象只能以 State 的形式传入，
 *   但抽象类可以以 abstract val 的形式供数据提供方以 val xxx by mutableStateOf() 方式重写，
 *   从而方便提供可观察变量
 *
 * 抽象类约定：
 * - 构造函数传入的是不可观察变量 (即非 State 变量)
 * - 可观察变量以 abstract val 形式供数据提供方重写
 *
 * @author 985892345
 * @date 2023/12/21 16:25
 */
@Stable
interface IComposePresenter {

  @Composable
  fun Content()
}