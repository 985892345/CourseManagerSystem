package com.course.pages.course.ui.pager

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.course.pages.course.api.item.CourseItemClickShow
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.ui.scroll.CourseScrollCompose
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date

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
  content: @Composable BoxScope.(CoursePagerState) -> Unit = {
    it.CourseScrollCompose()
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
  val weekItems: SnapshotStateList<ICourseItem>,
  val timeline: CourseTimeline,
  val itemClickShow: CourseItemClickShow,
)

@Composable
fun rememberCoursePagerState(
  weekBeginDate: Date,
  timeline: CourseTimeline,
  weekItems: SnapshotStateList<ICourseItem>,
  itemClickShow: CourseItemClickShow,
): CoursePagerState {
  val scrollState = rememberScrollState()
  return remember {
    CoursePagerState(
      scrollState = scrollState,
      weekBeginDate = weekBeginDate,
      weekItems = weekItems,
      timeline = timeline,
      itemClickShow = itemClickShow,
    )
  }
}