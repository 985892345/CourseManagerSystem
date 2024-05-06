package com.course.pages.course.ui.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.ui.pager.CoursePagerState

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:41
 */
@Composable
fun CoursePagerState.CourseItemGroupCompose(
  modifier: Modifier = Modifier.fillMaxSize(),
) {
  Box(
    modifier = Modifier.then(modifier)
  ) {
    // 使用 toList 避免并发修改
    itemGroups.toList().fastForEach {
      key(it) {
        it.Content(
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          scrollState = scrollState,
        )
      }
    }
  }
}



