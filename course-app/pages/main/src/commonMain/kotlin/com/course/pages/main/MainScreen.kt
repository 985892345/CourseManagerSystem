package com.course.pages.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import cafe.adriel.voyager.core.screen.Screen
import com.course.components.utils.compose.derivedStateOfStructure
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
data object MainScreen : Screen {

  @Composable
  override fun Content() {
    ProMainScreenContent()
  }
}

private val mainPages = Provider.getAllImpl(IMainPage::class)
  .mapValues { it.value.get() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProMainScreenContent() {
  val sortedPageKeys by remember {
    derivedStateOfStructure {
      mainPages.keys.sortedBy { mainPages.getValue(it).priority }
    }
  }
  val pagerState = rememberPagerState { mainPages.size }
  Column {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.weight(1F),
      beyondBoundsPageCount = 2,
      userScrollEnabled = false,
      key = { sortedPageKeys[it] }
    ) { page ->
      val key = sortedPageKeys[page]
      mainPages.getValue(key).Content()
    }
    BottomAppBar(
      backgroundColor = Color.White,
      elevation = 2.dp,
    ) {
      val coroutineScope = rememberCoroutineScope()
      sortedPageKeys.fastForEachIndexed { index, key ->
        key(key) {
          Box(modifier = Modifier.weight(1F), contentAlignment = Alignment.Center) {
            mainPages.getValue(key).apply {
              BottomAppBarItem {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(index)
                }
              }
            }
          }
        }
      }
    }
  }
}