package com.course.source.app.web.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.course.pages.main.api.IMainPage
import com.course.source.app.web.source.page.SourceScreen
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 15:51
 */
@ImplProvider(clazz = IMainPage::class, name = "source")
class SourceMainPage : IMainPage {

  override val priority: Int
    get() = 10

  @Composable
  override fun Content() {
    SourceScreen().Content()
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selectedToPosition: () -> Unit) {
    Text(text = "数据源", modifier = Modifier.clickable {
      selectedToPosition.invoke()
    })
  }
}