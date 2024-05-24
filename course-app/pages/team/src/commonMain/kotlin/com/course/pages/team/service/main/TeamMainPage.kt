package com.course.pages.team.service.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableCardIndicator
import com.course.pages.main.api.IMainPage
import com.course.pages.team.ui.page.TeamListScreen
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:00
 */
@ImplProvider(clazz = IMainPage::class, name = "team")
class TeamMainPage : IMainPage {

  override val priority: Int
    get() = 100

//  override val visibility: Boolean
//    get() = false

  private val teamListScreen by lazy { TeamListScreen(backable = false) }

  @Composable
  override fun Content(appBarHeight: Dp) {
    Box(modifier = Modifier.padding(bottom = appBarHeight)) {
      teamListScreen.Content()
    }
  }

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  override fun BoxScope.BottomAppBarItem(selected: State<Boolean>, selectToPosition: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Box(
      modifier = Modifier.size(32.dp).clickableCardIndicator {
        coroutineScope.launch {
          teamListScreen.requestTeamList()
        }
        selectToPosition()
      },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(DrawableResource("drawable/ic_team_bottom_bar.xml")),
        contentDescription = null,
        tint = if (selected.value) Color.Black else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
      )
    }
  }
}