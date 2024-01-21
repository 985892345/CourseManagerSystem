package com.course.pages.course.vp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.course.components.base.IComposePresenter

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 21:48
 */
abstract class CourseVpCompose : IComposePresenter {

  @Composable
  override fun Content() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
  }
}

@Composable
fun rememberCoursePagerState(): CourseVpCompose = remember {
  object : CourseVpCompose() {
  }
}.apply {

}