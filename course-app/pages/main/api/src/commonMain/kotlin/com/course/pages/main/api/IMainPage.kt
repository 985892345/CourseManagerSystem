package com.course.pages.main.api

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 15:31
 */
interface IMainPage {

  val priority: Int

  val appBarVisibility: Boolean
    get() = true

  @Composable
  fun Content(appBarHeight: Dp)

  @Composable
  fun BoxScope.BottomAppBarItem(
    selectedToPosition: () -> Unit
  )

  fun onUnselected() {}
}