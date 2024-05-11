package com.course.pages.course.api

import androidx.compose.runtime.Composable
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.source.app.account.AccountType
import com.course.source.app.course.CourseBean
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

  fun courseDetail(
    num: String,
    type: AccountType,
    controllers: ImmutableList<CourseController> = persistentListOf(),
    onlyOneTerm: Boolean = false,
    onClickItem: ((LessonItemData) -> Unit)? = null,
  ): CourseDetail

  suspend fun requestCourseBean(
    num: String,
    type: AccountType,
    termIndex: Int = -1,
  ): CourseBean

  /**
   * 强制刷新主页课表
   */
  fun forceRefreshMainCourse()
}
