package com.course.pages.course.service

import androidx.compose.runtime.Composable
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.model.StuCourseDetailDataProvider
import com.course.pages.course.model.TeaCourseDetailDataProvider
import com.course.pages.course.ui.CourseContentCompose
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/15 18:58
 */
@ImplProvider
class CourseServiceImpl : ICourseService {

  @Composable
  override fun Content(detail: CourseDetail) {
    CourseContentCompose(detail)
  }

  override fun stuCourseDetail(
    stuNum: String,
    vararg dataProviders: CourseDataProvider
  ): CourseDetail {
    return StuCourseDetailDataProvider(stuNum, *dataProviders)
  }

  override fun teaCourseDetail(
    teaNum: String,
    vararg dataProviders: CourseDataProvider
  ): CourseDetail {
    return TeaCourseDetailDataProvider(teaNum, *dataProviders)
  }
}

