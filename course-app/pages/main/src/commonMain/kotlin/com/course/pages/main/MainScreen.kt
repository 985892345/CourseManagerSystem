package com.course.pages.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.compose.rememberDerivedStateOfStructure
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import com.course.pages.main.api.IMainPage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/29 22:38
 */

@Serializable
@ObjectSerializable
class MainScreen : BaseScreen() {

  private val mainPages = Provider.getAllImpl(IMainPage::class)
    .mapValues { it.value.get() }

  @Composable
  override fun ScreenContent() {
    ProMainScreenContent()
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun ProMainScreenContent() {
    val navigator = LocalNavigator.current
    val sortedPageKeys by remember {
      derivedStateOfStructure {
        mainPages.filter { it.value.visibility }.keys
          .sortedBy { mainPages.getValue(it).priority }
      }
    }
    val pagerState = rememberPagerState { sortedPageKeys.size }
    var appBarHeight by mutableStateOf(56.dp)
    Box(modifier = Modifier.fillMaxSize()) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondBoundsPageCount = 2,
        userScrollEnabled = false,
        key = { sortedPageKeys[it] }
      ) { page ->
        val key = sortedPageKeys[page]
        mainPages.getValue(key).Content(appBarHeight)
      }
      AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = sortedPageKeys.getOrNull(pagerState.currentPage)
          ?.let { mainPages.getValue(it).appBarVisibility }
          ?: true,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
      ) {
        Box {
          BottomAppBar(
            modifier = Modifier.layout { measurable, constraints ->
              val placeable = measurable.measure(constraints)
              appBarHeight = placeable.height.toDp()
              layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
              }
            },
            backgroundColor = Color.White,
            elevation = 2.dp
          ) {
            val coroutineScope = rememberCoroutineScope()
            sortedPageKeys.fastForEachIndexed { index, key ->
              key(key) {
                Box(modifier = Modifier.weight(1F), contentAlignment = Alignment.Center) {
                  mainPages.getValue(key).apply {
                    BottomAppBarItem(
                      selected = rememberDerivedStateOfStructure {
                        pagerState.targetPage == index
                      },
                    ) {
                      mainPages.forEach { if (it.key != key) it.value.onUnselected() }
                      coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                      }
                    }
                  }
                }
              }
            }
          }
          val loginScreen = remember {
            Provider.implOrNull(BaseScreen::class, "login")
          }
          if (loginScreen != null) {
            Icon(
              modifier = Modifier.align(Alignment.TopEnd)
                .size(22.dp)
                .clickableCardIndicator(4.dp) {
                  showLogoutDialog(navigator, loginScreen)
                }.padding(4.dp),
              imageVector = Icons.AutoMirrored.Rounded.Logout,
              contentDescription = null,
            )
          }
        }
      }
    }
  }

  private fun showLogoutDialog(navigator: Navigator?, loginScreen: BaseScreen) {
    showChooseDialog(
      onClickPositiveBtn = {
        navigator?.replaceAll(loginScreen)
        hide()
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "确定要退出登录吗？")
      }
    }
  }
}
