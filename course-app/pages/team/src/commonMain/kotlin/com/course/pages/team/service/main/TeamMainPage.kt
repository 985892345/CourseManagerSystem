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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableCardIndicator
import com.course.pages.main.api.IMainPage
import com.course.pages.team.ui.page.TeamListScreen
import com.g985892345.provider.api.annotation.ImplProvider
import coursemanagersystem.course_app.pages.team.generated.resources.Res
import coursemanagersystem.course_app.pages.team.generated.resources.ic_team_bottom_bar
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

  private val teamListScreen = TeamListScreen(backable = false)

  @Composable
  override fun Content(appBarHeight: Dp) {
    Box(modifier = Modifier.padding(bottom = appBarHeight)) {
      teamListScreen.Content()
    }
  }

  @Composable
  override fun BoxScope.BottomAppBarItem(selected: State<Boolean>, selectToPosition: () -> Unit) {
    Box(
      modifier = Modifier.size(32.dp).clickableCardIndicator { selectToPosition() },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(Res.drawable.ic_team_bottom_bar),
        contentDescription = null,
        tint = if (selected.value) Color.Black else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
      )
    }
  }
}