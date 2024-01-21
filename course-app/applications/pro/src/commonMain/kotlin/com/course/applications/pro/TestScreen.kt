package com.course.applications.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.course.pages.course.BottomSheetCourseCompose

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 21:02
 */
class TestScreen : Screen {
  @Composable
  override fun Content() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Blue))
    val bottomSheet = remember { BottomSheetCourseCompose() }
    bottomSheet.Content()
  }
}
