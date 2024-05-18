package com.course.pages.course.api.controller

import androidx.compose.runtime.Stable
import com.course.components.utils.time.Today
import com.course.shared.time.Date
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 16:17
 */
@Stable
abstract class CourseDetail(
  val controllers: ImmutableList<CourseController>,
) : CourseController() {

  abstract val startDate: Date

  open val endDate: Date = Date(2099, 12, 31)

  abstract val title: String

  abstract val subtitle: String

  open val initialClickDate: Date
    get() = Today

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    controllers.forEach { it.onChangedClickDate(date) }
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    controllers.forEach { it.onComposeInit(coroutineScope) }
  }

  override fun onComposeDispose() {
    super.onComposeDispose()
    controllers.forEach { it.onComposeDispose() }
  }

  open fun onClickTitle() {}
  open fun onClickSubtitle() {}
}
