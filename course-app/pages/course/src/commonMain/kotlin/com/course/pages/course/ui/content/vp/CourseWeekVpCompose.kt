package com.course.pages.course.ui.content.vp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.course.components.utils.time.Today
import com.course.components.utils.time.diffDays
import com.course.pages.course.ui.content.CourseContentCombine
import com.course.pages.course.ui.content.pager.CoursePagerCompose
import com.course.pages.course.ui.content.pager.CoursePagerData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

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
    initialPage = Today.diffDays(terms[termsVpIndex].firstDate)
      .div(7)
      .plus(1)
      .coerceAtLeast(0)
      .let { if (it >= terms[termsVpIndex].weeks.size) 0 else it }
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
  val firstDate: LocalDate,
  val weeks: ImmutableList<CoursePagerData>
)