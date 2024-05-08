package com.course.source.app.local.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableCardIndicator
import com.course.pages.main.api.IMainPage
import com.course.source.app.local.source.page.SourceScreen
import com.g985892345.provider.api.annotation.ImplProvider
import coursemanagersystem.course_source.app.app_local.generated.resources.Res
import coursemanagersystem.course_source.app.app_local.generated.resources.ic_source_bottom_bar
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 15:51
 */
@ImplProvider(clazz = IMainPage::class, name = "source")
class SourceMainPage : IMainPage {

  override val priority: Int
    get() = 200

  @Composable
  override fun Content(appBarHeight: Dp) {
    Box(modifier = Modifier.padding(bottom = appBarHeight)) {
      SourceScreen().Content()
    }
  }

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  override fun BoxScope.BottomAppBarItem(selectedToPosition: () -> Unit) {
    Box(
      modifier = Modifier.size(32.dp).clickableCardIndicator { selectedToPosition() },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(18.dp),
        painter = painterResource(Res.drawable.ic_source_bottom_bar),
        contentDescription = null,
      )
    }
  }
}