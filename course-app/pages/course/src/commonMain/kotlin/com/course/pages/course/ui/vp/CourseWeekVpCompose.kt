package com.course.pages.course.ui.vp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.course.components.utils.time.Date
import com.course.pages.course.ui.CourseContentCombine
import com.course.pages.course.ui.pager.CoursePagerCompose
import com.course.pages.course.ui.pager.CoursePagerData
import kotlinx.collections.immutable.ImmutableList

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:38
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseContentCombine.CourseWeekVpCompose(
  modifier: Modifier = Modifier,
  termsVpIndex: Int,
  weekPagerState: PagerState = rememberPagerState(
    initialPage = if (termsVpIndex == terms.lastIndex) nowWeek ?: 0 else 0
  ) { terms[termsVpIndex].weeks.size },
  content: @Composable PagerScope.(Int) -> Unit = {
    CoursePagerCompose(
      termsVpIndex = termsVpIndex,
      weeksVpIndex = it,
    )
  },
) {
  HorizontalPager(
    state = weekPagerState,
    modifier = Modifier.then(modifier),
  ) {
    content(it)
  }
}

@Stable
data class CourseWeeksVpData(
  val firstDate: Date,
  val weeks: ImmutableList<CoursePagerData>
)