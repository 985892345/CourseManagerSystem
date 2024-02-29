package com.course.applications.pro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.course.components.base.navigator.Screen
import com.course.components.base.theme.AppTheme
import com.course.pages.course.page.CourseScreen
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/29 22:38
 */
@OptIn(ExperimentalFoundationApi::class)
class ProMainScreen : Screen {

  private lateinit var pagerState: PagerState

  @Composable
  override fun Content() {
    pagerState = rememberPagerState { 3 }
    AppTheme(darkTheme = false) {
      ProMainScreenContent(pagerState)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProMainScreenContent(pagerState: PagerState) {
  Column {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.weight(1F),
      beyondBoundsPageCount = 2,
      userScrollEnabled = false,
    ) {
      when (it) {
        0 -> remember { CourseScreen("2020214988") }.Content()
        else -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(text = it.toString())
        }
      }
    }
    BottomAppBar(
      backgroundColor = Color.White,
      elevation = 2.dp,
    ) {
      val coroutineScope = rememberCoroutineScope()
      Box(modifier = Modifier.weight(1F), contentAlignment = Alignment.Center) {
        Text(text = "课表", modifier = Modifier.clickable {
          coroutineScope.launch {
            pagerState.animateScrollToPage(0)
          }
        })
      }
      Box(modifier = Modifier.weight(1F), contentAlignment = Alignment.Center) {
        Text(text = "功能", modifier = Modifier.clickable {
          coroutineScope.launch {
            pagerState.animateScrollToPage(1)
          }
        })
      }
      Box(modifier = Modifier.weight(1F), contentAlignment = Alignment.Center) {
        Text(text = "设置", modifier = Modifier.clickable {
          coroutineScope.launch {
            pagerState.animateScrollToPage(2)
          }
        })
      }
    }
  }
}