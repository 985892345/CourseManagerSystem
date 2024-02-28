package com.course.components.view.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.course.components.utils.debug.logd
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/28 15:47
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerCompose(
  state: PagerState,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  pageSize: PageSize = PageSize.Fill,
  beyondBoundsPageCount: Int = PagerDefaults.BeyondBoundsPageCount,
  pageSpacing: Dp = 0.dp,
  verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
  flingBehavior: SnapFlingBehavior = PagerDefaults.flingBehavior(state = state),
  userScrollEnabled: Boolean = true,
  reverseLayout: Boolean = false,
  key: ((index: Int) -> Any)? = null,
  pageNestedScrollConnection: NestedScrollConnection = remember(state) {
    PagerDefaults.pageNestedScrollConnection(state, Orientation.Horizontal)
  },
  pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
  val pagerState by rememberUpdatedState(state)
  val userScrollEnabledState by rememberUpdatedState(userScrollEnabled)
  val coroutineScope = rememberCoroutineScope()
  var layoutWith by remember { mutableIntStateOf(0) }
  var nowOffset by remember { mutableStateOf(0f) }
  HorizontalPager(
    state = state,
    modifier = modifier.onSizeChanged {
      layoutWith = it.width
    }.draggable(
      orientation = Orientation.Horizontal,
      state = rememberDraggableState {
        if (!userScrollEnabledState) return@rememberDraggableState
        val oldOffset = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * layoutWith
        val newOffset = oldOffset - it
        val maxOffset = (pagerState.pageCount - 1) * layoutWith
        logd("it = $it, oldOffset = $oldOffset, newOffset = $newOffset, maxOffset = $maxOffset")
        if (newOffset < 0 || newOffset > maxOffset) {
          nowOffset -= it
          coroutineScope.launch {
            pagerState.scrollBy(oldOffset - nowOffset / 2)
          }
        } else {
          nowOffset = newOffset
          coroutineScope.launch {
            pagerState.scrollBy(-it)
          }
        }
      },
      onDragStarted = {
        nowOffset = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * layoutWith
      },
      onDragStopped = {
        coroutineScope.launch {
//          pagerState.animateScrollToPage(pagerState.animateScrollBy())
        }
      },
    ),
    contentPadding = contentPadding,
    pageSize = pageSize,
    beyondBoundsPageCount = beyondBoundsPageCount,
    pageSpacing = pageSpacing,
    verticalAlignment = verticalAlignment,
    flingBehavior = flingBehavior,
    userScrollEnabled = false,
    reverseLayout = reverseLayout,
    key = key,
    pageNestedScrollConnection = pageNestedScrollConnection,
    pageContent = pageContent
  )
}