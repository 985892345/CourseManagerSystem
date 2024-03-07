package com.course.pages.course.ui.pager

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.course.components.utils.time.Date
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.scroll.CourseScrollCompose

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
  val beginDateState: State<Date?>, // 为 null 时将不与日期关联，根据星期数来显示
  val items: SnapshotStateList<ICourseItemBean>,
)

@Composable
fun rememberCoursePagerState(
  beginDate: Date?,
  items: SnapshotStateList<ICourseItemBean> = remember { SnapshotStateList() },
): CoursePagerState {
  val scrollState = rememberScrollState()
  val beginDateState = rememberUpdatedState(beginDate?.minusDays(beginDate.dayOfWeekOrdinal))
  return remember {
    CoursePagerState(
      scrollState = scrollState,
      beginDateState = beginDateState,
      items = items
    )
  }
}