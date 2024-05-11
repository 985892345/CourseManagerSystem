package com.course.pages.course.service

import androidx.compose.runtime.Composable
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.pages.course.model.StuCourseDetailController
import com.course.pages.course.model.StuLessonRepository
import com.course.pages.course.model.TeaCourseDetailController
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
    onlyOneTerm: Boolean,
    onClickItem: ((LessonItemData) -> Unit)?
  ): CourseDetail {
    return when (type) {
      AccountType.Student -> StuCourseDetailController(num, controllers, onlyOneTerm, onClickItem)
      AccountType.Teacher -> TeaCourseDetailController(num, controllers, onlyOneTerm, onClickItem)
    }
  }

  override suspend fun requestCourseBean(num: String, type: AccountType, termIndex: Int): CourseBean {
    return when (type) {
      AccountType.Student -> StuLessonRepository.requestCourseBean(num, termIndex)
      AccountType.Teacher -> TODO()
    }
  }

  override fun forceRefreshMainCourse() {
    CourseMainPage.forceRefreshCourse()
  }
}

