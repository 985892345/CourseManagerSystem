package com.course.pages.course.service

import androidx.compose.runtime.Composable
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.model.StuCourseDetailController
import com.course.pages.course.model.TeaCourseDetailController
import com.course.pages.course.ui.CourseContentCompose
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.collections.immutable.ImmutableList

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 18:58
 */
@ImplProvider
object CourseServiceImpl : ICourseService {

  @Composable
  override fun Content(detail: CourseDetail) {
    CourseContentCompose(detail)
  }

  override fun stuCourseDetail(
    stuNum: String,
    controllers: ImmutableList<CourseController>
  ): CourseDetail {
    return StuCourseDetailController(stuNum, controllers)
  }

  override fun teaCourseDetail(
    teaNum: String,
    controllers: ImmutableList<CourseController>
  ): CourseDetail {
    return TeaCourseDetailController(teaNum, controllers)
  }
}

