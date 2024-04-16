package com.course.pages.course.api

import androidx.compose.runtime.Composable
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.api.item.CourseBottomSheetState
import com.course.pages.course.api.item.CourseItemClickShow

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 16:14
 */
interface ICourseService {

  @Composable
  fun Content(detail: CourseDetail)

  @Composable
  fun Content(
    detail: CourseDetail,
    state: CourseBottomSheetState
  )

  @Composable
  fun Content(
    detail: CourseDetail,
    itemClickShow: CourseItemClickShow
  )

  fun stuCourseDetail(
    stuNum: String,
    vararg dataProviders: CourseDataProvider,
  ): CourseDetail

  fun teaCourseDetail(
    teaNum: String,
    vararg dataProviders: CourseDataProvider,
  ): CourseDetail
}
