package com.course.pages.course.ui.content.pager.scroll

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.course.pages.course.ui.content.pager.CoursePagerCombine

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/27 16:25
 */
@Composable
fun CoursePagerCombine.CourseScrollCompose(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit = {
    CourseTimelineCompose(modifier = Modifier.weight(0.8F))
    CourseItemGroupCompose(modifier = Modifier.weight(7F))
  }
) {
  Row(modifier = Modifier.fillMaxSize().then(modifier)) {
    content(this)
  }
}