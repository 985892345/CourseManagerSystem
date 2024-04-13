package com.course.pages.course.api

import com.course.pages.course.api.data.CourseDataProvider
import com.course.source.app.account.AccountBean

/**
 * 给主页课表添加额外数据
 *
 * @author 985892345
 * 2024/4/11 23:32
 */
interface IMainCourseDataProvider {
  fun createCourseDataProviders(
    account: AccountBean?,
  ): List<CourseDataProvider>
}