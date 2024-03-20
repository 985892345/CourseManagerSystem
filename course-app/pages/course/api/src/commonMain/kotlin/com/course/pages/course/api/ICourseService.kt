package com.course.pages.course.api

import androidx.compose.runtime.Composable
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 16:14
 */
interface ICourseService {

  @Composable
  fun Content(detail: CourseDetail)

  fun stuCourseDetail(
    stuNum: String,
    vararg dataProviders: CourseDataProvider,
  ): CourseDetail

  fun teaCourseDetail(
    teaNum: String,
    vararg dataProviders: CourseDataProvider,
  ): CourseDetail
}
