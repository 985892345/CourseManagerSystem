package com.course.pages.course.api.data

import androidx.compose.runtime.Stable
import com.course.components.utils.time.Today
import com.course.shared.time.Date
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 16:17
 */
@Stable
abstract class CourseDetail(
  vararg val dataProviders: CourseDataProvider
) : CourseDataProvider() {

  abstract val startDate: Date

  open val endDate: Date = Date(2099, 12, 31)

  abstract val title: String

  abstract val subtitle: String

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    dataProviders.forEach { it.onChangedClickDate(date) }
  }

  override fun initProvider(coroutineScope: CoroutineScope) {
    super.initProvider(coroutineScope)
    dataProviders.forEach { it.initProvider(coroutineScope) }
  }
}

object EmptyCourseDetail : CourseDetail() {
  override val startDate: Date = Today.firstDate
  override val title: String
    get() = "无数据"
  override val subtitle: String
    get() = ""
}
