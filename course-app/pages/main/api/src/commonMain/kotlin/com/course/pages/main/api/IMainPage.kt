package com.course.pages.main.api

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 15:31
 */
interface IMainPage {

  val priority: Int

  @Composable
  fun Content()

  @Composable
  fun BoxScope.BottomAppBarItem(
    selectedToPosition: () -> Unit
  )
}