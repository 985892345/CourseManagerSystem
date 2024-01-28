package com.course.pages.course.ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.course.pages.course.ui.CourseCombine
import com.course.pages.course.ui.content.vp.CourseTermsVpCompose

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:02
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCombine.CourseContentCompose(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.(CourseContentCombine) -> Unit = {
    it.CourseTermsVpCompose()
  }
) {
  Box(modifier = Modifier.fillMaxSize().then(modifier)) {
    content(CourseContentCombine(
      courseCombine = this@CourseContentCompose,
    ))
  }
}

@Stable
data class CourseContentCombine(
  val courseCombine: CourseCombine
) {
  val terms get() = courseCombine.semesterVpData.terms
}