package com.course.pages.course.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.course.components.base.account.Account
import com.course.components.utils.provider.Provider
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.api.data.EmptyCourseDetail
import com.course.pages.main.api.IMainPage
import com.course.source.app.account.AccountBean
import com.course.source.app.account.AccountType
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 15:45
 */
@ImplProvider(clazz = IMainPage::class, name = "course")
class CourseMainPage : IMainPage {

  override val priority: Int
    get() = 0

  private var oldAccount: AccountBean? = null

  private var oldCourseDetail: CourseDetail? = null

  @Composable
  override fun Content() {
    Provider.impl(ICourseService::class).apply {
      val account by Account.observeAccount().collectAsState()
      Content(getCourseDetail(account))
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selectedToPosition: () -> Unit) {
    Text(text = "课表", modifier = Modifier.clickable {
      selectedToPosition.invoke()
    })
  }

  private fun ICourseService.getCourseDetail(account: AccountBean?): CourseDetail {
    if (oldAccount == account) {
      return oldCourseDetail ?: EmptyCourseDetail
    }
    oldCourseDetail = when (account?.type) {
      AccountType.Student -> stuCourseDetail(account.num)
      AccountType.Teacher -> teaCourseDetail(account.num)
      null -> EmptyCourseDetail
    }
    return oldCourseDetail ?: EmptyCourseDetail
  }
}