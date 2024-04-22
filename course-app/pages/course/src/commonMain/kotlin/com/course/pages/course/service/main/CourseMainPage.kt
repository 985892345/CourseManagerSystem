package com.course.pages.course.service.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.base.account.Account
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.combineClickableCardIndicator
import com.course.components.utils.provider.Provider
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.CourseDetail
import com.course.pages.course.api.item.CourseBottomSheetItemClickShow
import com.course.pages.course.api.item.CourseBottomSheetState
import com.course.pages.main.api.IMainPage
import com.course.shared.time.Date
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

  override var appBarVisibility: Boolean by mutableStateOf(true)

  private var oldAccount: AccountBean? = null

  private var oldCourseDetail: CourseDetail? = null

  private val courseBottomSheetState = MainPageCourseBottomSheetState()

  private val forceRefreshCourse = mutableIntStateOf(0)

  @Composable
  override fun Content(appBarHeight: Dp) {
    CourseBottomSheetItemClickShow(courseBottomSheetState) {
      Box(modifier = Modifier.padding(bottom = appBarHeight)) {
        Provider.impl(ICourseService::class).apply {
          key(forceRefreshCourse.intValue) {
            val account by Account.observeAccount().collectAsState()
            Content(getCourseDetail(account), it)
          }
        }
      }
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selectedToPosition: () -> Unit) {
    Box(
      modifier = Modifier.size(32.dp).combineClickableCardIndicator(
        onClick = { selectedToPosition() },
        onLongClick = {
          oldAccount = null
          oldCourseDetail = null
          forceRefreshCourse.value++
        }
      ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(22.dp),
        painter = painterResource("drawable/ic_course_bottom_bar.xml"),
        contentDescription = null,
      )
    }
  }

  override fun onUnselected() {
    super.onUnselected()
    courseBottomSheetState.cancelShow()
  }

  private fun ICourseService.getCourseDetail(account: AccountBean?): CourseDetail {
    val oldCourseDetail = oldCourseDetail
    if (oldAccount == account && oldCourseDetail != null) {
      return oldCourseDetail
    }
    oldAccount = account
    val dataProvider = Provider.getAllImpl(IMainCourseDataProvider::class)
      .map { it.value.get().createCourseDataProviders(account) }
      .flatten()
    return when (account?.type) {
      AccountType.Student -> stuCourseDetail(account.num, *dataProvider.toTypedArray())
      AccountType.Teacher -> teaCourseDetail(account.num, *dataProvider.toTypedArray())
      null -> EmptyAccountCourseDetail(*dataProvider.toTypedArray())
    }.also { this@CourseMainPage.oldCourseDetail = it }
  }

  private inner class MainPageCourseBottomSheetState : CourseBottomSheetState() {
    override fun startShow() {
      super.startShow()
      appBarVisibility = false
    }

    override fun cancelShow() {
      super.cancelShow()
      appBarVisibility = true
    }

    override fun endShow() {
      super.endShow()
      appBarVisibility = true
    }
  }
}

private class EmptyAccountCourseDetail(
  vararg dataProviders: CourseDataProvider
) : CourseDetail(*dataProviders) {
  override val startDate: Date = Date(1901, 1, 1)
  override val title: String = "未登陆"
  override val subtitle: String = ""

  override fun getTerms(): List<Pair<Int, Date>> {
    return emptyList()
  }

  override fun onClickTitle() {
    toast("请在数据源中设置用户信息")
  }
}