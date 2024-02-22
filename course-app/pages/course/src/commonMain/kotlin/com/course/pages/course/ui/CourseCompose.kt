package com.course.pages.course.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.course.components.utils.time.Today
import com.course.pages.course.ui.vp.CourseSemesterVpData
import com.course.pages.course.ui.vp.CourseTermsVpCompose
import kotlinx.datetime.daysUntil

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 13:56
 */

/**
 * ```
 * CourseContentCompose           课表内容
 * |- CourseTermsVpCompose          全部学期
 * |---- CourseWeeksVpCompose         全部周数
 * |------- CoursePagerCompose          课表单页
 *          |-- CourseWeekdayCompose      星期数
 *          |-- CourseScrollCompose       滚轴
 *          |----- CourseTimelineCompose    时间轴
 *          |----- CourseItemGroupCompose   item 容器
 *                 |-- CourseItemCompose      item
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseContentCompose(
  modifier: Modifier = Modifier,
  semesterVpData: CourseSemesterVpData,
  content: @Composable ColumnScope.(CourseContentCombine) -> Unit = {
    it.CourseTermsVpCompose()
  }
) {
  Column(
    modifier = Modifier.background(LocalCourseColor.current.background)
      .then(modifier)
  ) {
    content(
      CourseContentCombine(
        semesterVpData = semesterVpData
      )
    )
  }
}

@Stable
data class CourseContentCombine(
  val semesterVpData: CourseSemesterVpData,
) {

  val terms get() = semesterVpData.terms

  /**
   * 当前学期的周数
   *
   * @return 返回 0，则表示开学前一周（因为第一周开学）; 返回 null 则说明无数据
   *
   * # 注意：存在返回负数的情况！！！
   * ```
   *     -1      0      1      2       3        4             返回值
   *  ----------------------------------------------------------->
   * -14     -7      0      7      14       21       28       天数差
   * ```
   */
  val nowWeek by derivedStateOf(structuralEqualityPolicy()) {
    semesterVpData.terms.lastOrNull()?.let {
      val dayOfTerm = it.firstDate.daysUntil(Today)
      if (dayOfTerm >= 0) dayOfTerm / 7 + 1 else dayOfTerm / 7
    }
  }
}


val LocalCourseColor = staticCompositionLocalOf { CourseColor() }

@Stable
class CourseColor {
  var background by mutableStateOf(Color(0xFFFFFFFF), structuralEqualityPolicy())
  var sheetTip by mutableStateOf(Color(0xFFE2EDFB), structuralEqualityPolicy())
}

