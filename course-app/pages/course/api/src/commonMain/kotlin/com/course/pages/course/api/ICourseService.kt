package com.course.pages.course.api

import androidx.compose.runtime.Composable
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    controllers: ImmutableList<CourseController> = persistentListOf(),
  ): CourseDetail

  fun teaCourseDetail(
    teaNum: String,
    controllers: ImmutableList<CourseController> = persistentListOf(),
  ): CourseDetail
}
