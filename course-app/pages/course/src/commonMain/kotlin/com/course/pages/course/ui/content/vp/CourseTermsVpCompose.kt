package com.course.pages.course.ui.content.vp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.course.pages.course.ui.content.CourseContentCombine
import kotlinx.collections.immutable.ImmutableList

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:38
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseContentCombine.CourseTermsVpCompose(
  modifier: Modifier = Modifier,
  semesterPagerState: PagerState = rememberPagerState(initialPage = terms.lastIndex) { terms.size },
  content: @Composable PagerScope.(Int) -> Unit = {
    CourseWeekVpCompose(
      termsVpIndex = it
    )
  }
) {
  HorizontalPager(
    state = semesterPagerState,
    modifier = Modifier.then(modifier),
  ) {
    content(it)
  }
}

@Stable
data class CourseSemesterVpData(
  val terms: ImmutableList<CourseWeeksVpData>
)