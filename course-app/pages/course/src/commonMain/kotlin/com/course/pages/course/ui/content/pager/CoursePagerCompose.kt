package com.course.pages.course.ui.content.pager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.course.pages.course.ui.content.CourseContentCombine
import com.course.pages.course.ui.content.pager.scroll.CourseScrollCompose
import com.course.pages.course.ui.item.ICourseItemBean
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/25 19:37
 */

@Composable
fun CourseContentCombine.CoursePagerCompose(
  modifier: Modifier = Modifier,
  termsVpIndex: Int,
  weeksVpIndex: Int,
  content: @Composable ColumnScope.(CoursePagerCombine) -> Unit = {
    it.CourseWeekdayCompose()
    it.CourseScrollCompose()
  }
) {
  Column(modifier = Modifier.fillMaxSize().then(modifier)) {
    content(this, CoursePagerCombine(
      contentCombine = this@CoursePagerCompose,
      termsVpIndex = termsVpIndex,
      weeksVpIndex = weeksVpIndex,
    ))
  }
}

@Stable
data class CoursePagerCombine(
  val contentCombine: CourseContentCombine,
  val termsVpIndex: Int,
  val weeksVpIndex: Int,
) {
  val weeks = contentCombine.courseCombine.semesterVpData.terms[termsVpIndex]
  val pager = weeks.weeks[weeksVpIndex]

  // 为 0 时默认整学期不显示日期
  val monDate =
    if (weeksVpIndex == 0) null else weeks.firstDate.plus(weeksVpIndex - 1, DateTimeUnit.WEEK)
}

@Stable
data class CoursePagerData(
  val items: SnapshotStateList<ICourseItemBean>
)