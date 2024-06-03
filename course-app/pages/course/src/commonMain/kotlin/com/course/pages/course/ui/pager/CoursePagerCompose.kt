package com.course.pages.course.ui.pager

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.course.ui.scroll.CourseScrollCompose
import com.course.shared.time.Date
import kotlinx.collections.immutable.ImmutableList

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/25 19:37
 */

@Composable
fun CoursePagerCompose(
  state: CoursePagerState,
  modifier: Modifier = Modifier,
  paddingBottom: Dp = 0.dp,
  content: @Composable BoxScope.(CoursePagerState) -> Unit = {
    it.CourseScrollCompose(paddingBottom = paddingBottom)
  }
) {
  Box(modifier = Modifier.then(modifier)) {
    content(state)
  }
}

@Stable
class CoursePagerState(
  val scrollState: ScrollState,
  val weekBeginDate: Date,
  val timeline: CourseTimeline,
  val itemGroups: ImmutableList<ICourseItemGroup>,
)

@Composable
fun rememberCoursePagerState(
  weekBeginDate: Date,
  timeline: CourseTimeline,
  itemGroups: ImmutableList<ICourseItemGroup>,
): CoursePagerState {
  val scrollState = rememberScrollState()
  return remember {
    CoursePagerState(
      scrollState = scrollState,
      weekBeginDate = weekBeginDate,
      itemGroups = itemGroups,
      timeline = timeline,
    )
  }
}