package com.course.pages.course.ui.pager.scroll

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.ui.pager.CoursePagerState

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:39
 */

@Composable
fun CoursePagerState.CourseTimelineCompose(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = Modifier.then(modifier)
  ) {
    timeline.fastForEach {
      it.apply { Content() }
    }
  }
}
