package com.course.pages.exam.service.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.base.account.Account
import com.course.components.utils.compose.clickableCardIndicator
import com.course.pages.exam.ui.ExamScreen
import com.course.pages.main.api.IMainPage
import com.course.source.app.account.AccountType
import com.g985892345.provider.api.annotation.ImplProvider
import coursemanagersystem.course_app.pages.exam.generated.resources.Res
import coursemanagersystem.course_app.pages.exam.generated.resources.ic_exam_bottom_bar
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 12:23
 */
@ImplProvider(clazz = IMainPage::class, name = "exam")
class ExamMainPage : IMainPage {

  override val priority: Int
    get() = 10

  override val visibility: Boolean
//    get() = Account.observeAccount().value?.type == AccountType.Student
    get() = false // tab 过多，暂时不显示

  @Composable
  override fun Content(appBarHeight: Dp) {
    val account by Account.observeAccount().collectAsState()
    account?.let {
      if (it.type == AccountType.Student) {
        ExamScreen(it.num, false).Content()
      }
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selected: State<Boolean>, selectToPosition: () -> Unit) {
    Box(
      modifier = Modifier.size(32.dp).clickableCardIndicator { selectToPosition() },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.padding(top = 1.dp, start = 1.dp).size(18.dp),
        painter = painterResource(Res.drawable.ic_exam_bottom_bar),
        contentDescription = null,
        tint = if (selected.value) Color.Black else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
      )
    }
  }
}