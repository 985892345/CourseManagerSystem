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
import com.course.pages.course.api.data.EmptyCourseDetail
import com.course.pages.main.api.IMainPage
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

  @Composable
  override fun Content() {
    Provider.impl(ICourseService::class).apply {
      val account by Account.observeAccount().collectAsState()
      Content(
        account?.let {
          if (it.isStuOrElseTea) stuCourseDetail(it.num)
          else teaCourseDetail(it.num)
        } ?: EmptyCourseDetail
      )
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selectedToPosition: () -> Unit) {
    Text(text = "课表", modifier = Modifier.clickable {
      selectedToPosition.invoke()
    })
  }
}