package com.course.pages.course.api.data

import androidx.compose.runtime.Stable
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

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    dataProviders.forEach { it.onComposeInit(coroutineScope) }
  }

  open fun onClickTitle() {}
  open fun onClickSubtitle() {}

  init {
    dataProviders.forEach {
      it.addDataChangedListener(this)
    }
  }
}
