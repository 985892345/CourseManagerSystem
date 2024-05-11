package com.course.pages.course.api

import com.course.pages.course.api.controller.CourseController
import com.course.source.app.account.AccountBean

/**
 * 给主页课表添加额外数据
 *
 * @author 985892345
 * 2024/4/11 23:32
 */
interface IMainCourseController {
  fun createCourseController(
    account: AccountBean?,
  ): List<CourseController>
}