package com.course.pages.course.model

import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.shared.time.Date

/**
 * .
 *
 * @author 985892345
 * 2024/3/17 17:42
 */
class TeaCourseDetailDataProvider(
  val teaNum: String,
  vararg dataProviders: CourseDataProvider
) : CourseDetail(*dataProviders) {
  override val startDate: Date
    get() = TODO("Not yet implemented")
  override val title: String
    get() = TODO("Not yet implemented")
  override val subtitle: String
    get() = TODO("Not yet implemented")
}