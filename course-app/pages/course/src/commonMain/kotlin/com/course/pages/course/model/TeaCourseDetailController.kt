package com.course.pages.course.model

import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.time.Date
import kotlinx.collections.immutable.ImmutableList

/**
 * .
 *
 * @author 985892345
 * 2024/3/17 17:42
 */
class TeaCourseDetailController(
  val teaNum: String,
  controllers: ImmutableList<CourseController>,
  val onlyOneTerm: Boolean,
  val onClickItem: ((LessonItemData) -> Unit)?,
) : CourseDetail(controllers) {
  override val startDate: Date
    get() = TODO("Not yet implemented")
  override val title: String
    get() = TODO("Not yet implemented")
  override val subtitle: String
    get() = TODO("Not yet implemented")

  override fun getTerms(): List<Pair<Int, Date>> {
    return emptyList()
  }
}