package com.course.pages.course.ui.pager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.course.pages.course.ui.CourseContentCombine
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.scroll.CourseScrollCompose
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
  modifier: Modifier = Modifier.padding(end = 12.dp),
  termsVpIndex: Int = 0,
  weeksVpIndex: Int = 0,
  content: @Composable ColumnScope.(CoursePagerCombine) -> Unit = {
    it.CourseScrollCompose()
  }
) {
  Column(modifier = Modifier.then(modifier)) {
    content(
      this, CoursePagerCombine(
        contentCombine = this@CoursePagerCompose,
        termsVpIndex = termsVpIndex,
        weeksVpIndex = weeksVpIndex,
      )
    )
  }
}

@Stable
data class CoursePagerCombine(
  val contentCombine: CourseContentCombine,
  val termsVpIndex: Int,
  val weeksVpIndex: Int,
) {
  val weeks = contentCombine.semesterVpData.terms[termsVpIndex]
  val pager = weeks.weeks[weeksVpIndex]

  // 为 0 时默认整学期不显示日期
  val monDate =
    if (weeksVpIndex == 0) null else weeks.firstDate.plus(weeksVpIndex - 1, DateTimeUnit.WEEK)
}

@Stable
data class CoursePagerData(
  val items: SnapshotStateList<ICourseItemBean>
)