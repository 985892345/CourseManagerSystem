package com.course.source.app.local.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableCardIndicator
import com.course.pages.main.api.IMainPage
import com.course.source.app.local.source.page.SourceScreen
import com.g985892345.provider.api.annotation.ImplProvider
import org.jetbrains.compose.resources.DrawableResource
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
  override fun BoxScope.BottomAppBarItem(selected: State<Boolean>, selectToPosition: () -> Unit) {
    Box(
      modifier = Modifier.size(32.dp).clickableCardIndicator { selectToPosition() },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(18.dp),
        painter = painterResource(DrawableResource("drawable/ic_source_bottom_bar.xml")),
        contentDescription = null,
        tint = if (selected.value) Color.Black else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
      )
    }
  }
}