package com.course.components.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 17:03
 */
open class BaseComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    cancelStatusBar()
  }

  private fun cancelStatusBar() {
    val window = this.window

    // 这是 Android 做了兼容的 Compat 包
    // 注意，使用了下面这个方法后，状态栏不会再有东西占位，
    // 可以使用 Modifier.statusBarsPadding()
    // 如果需要单独得到状态栏高度，可以使用 SystemUI.statusBarsHeight
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT //把状态栏颜色设置成透明
  }
}