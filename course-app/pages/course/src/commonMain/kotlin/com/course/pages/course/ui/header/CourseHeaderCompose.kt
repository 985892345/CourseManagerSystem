package com.course.pages.course.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.course.pages.course.ui.CourseCombine

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:05
 */

@Composable
fun CourseCombine.CourseTopCompose(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = Modifier.fillMaxWidth()
      .height(40.dp)
//      .background(LocalCourseColor.current.background)
      .background(Color.Gray)
      .then(modifier)
  ) {

  }
}

interface CourseTopCombine {
  companion object : CourseTopCombine
}