package com.course.pages.course.service

import androidx.compose.runtime.Composable
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.pages.course.model.CourseDetailController
import com.course.pages.course.model.LessonRepository
import com.course.pages.course.service.main.CourseMainPage
import com.course.pages.course.ui.CourseContentCompose
import com.course.source.app.account.AccountType
import com.course.source.app.course.CourseBean
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

  override fun courseDetail(
    num: String,
    type: AccountType,
    controllers: ImmutableList<CourseController>,
    onClickItem: ((LessonItemData) -> Unit)?
  ): CourseDetail {
    return CourseDetailController(num, controllers, onClickItem)
  }

  override suspend fun requestCourseBean(num: String, type: AccountType): CourseBean {
    return LessonRepository.requestCourseBean(num)
  }

  override fun forceRefreshMainCourse() {
    CourseMainPage.forceRefreshCourse()
  }
}

