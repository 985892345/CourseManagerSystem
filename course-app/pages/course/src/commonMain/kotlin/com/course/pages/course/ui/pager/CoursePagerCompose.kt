package com.course.pages.course.ui.pager

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.scroll.CourseScrollCompose
import com.course.pages.course.ui.pager.scroll.timeline.CourseTimelineData
import com.course.pages.course.ui.pager.scroll.timeline.createTimeline
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
  val items: SnapshotStateList<ICourseItemBean>,
  val timeline: List<CourseTimelineData>
)

@Composable
fun rememberCoursePagerState(
  items: SnapshotStateList<ICourseItemBean> = remember { SnapshotStateList() },
): CoursePagerState {
  val scrollState = rememberScrollState()
  val timeline = rememberSaveable(
    saver = Saver(
      save = { Json.encodeToString(it) },
      restore = { Json.decodeFromString<List<CourseTimelineData>>(it) })
  ) { createTimeline() }
  return remember {
    CoursePagerState(
      scrollState = scrollState,
      items = items,
      timeline = timeline,
    )
  }
}