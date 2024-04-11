package com.course.components.utils.init

import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 16:47
 */
interface IInitialService {

  /**
   * 在应用初始化时的回调
   */
  fun onAppInit() {}

  @Composable
  fun onComposeInit() {}
}