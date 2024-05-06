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

  // 获取当前用户的学期与开学日期
  abstract fun getTerms(): List<Pair<Int, Date>>

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    controllers.forEach { it.onChangedClickDate(date) }
  }

  override fun onRequestTerm(termIndex: Int) {
    super.onRequestTerm(termIndex)
    controllers.forEach { it.onRequestTerm(termIndex) }
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
