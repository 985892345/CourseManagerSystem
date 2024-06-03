package com.course.source.app.local.source.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.serializable.IntStateSerializable
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.local.request.RequestContent
import com.course.source.app.local.request.RequestUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/3/23 13:30
 */
@Serializable
@ObjectSerializable
class RequestUnitScreen(
  val requestContentName: String,
  val requestUnitIdOrServiceKey: String,
) : BaseScreen() {

  @Transient
  private val requestContent = RequestContent.find(requestContentName)!!

  @Transient
  private val requestUnit = requestUnitIdOrServiceKey.toIntOrNull()?.let { id ->
    requestContent.requestUnits.singleOrNull { it.id == id }
  } ?: RequestUnit.create(
    contentKey = requestContent.key,
    serviceKey = requestUnitIdOrServiceKey,
    id = requestContent.requestUnits.maxOfOrNull { it.id }?.plus(1) ?: 0,
  )

  @Serializable(IntStateSerializable::class)
  private val selectedTabIndexState = mutableIntStateOf(0)

  private val formatScreen =
    RequestUnitFormatScreen(requestContent.format, requestContent.parameterWithHint)

  private val codeScreen = RequestUnitCodeScreen(requestContent.key, requestUnit)

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxSize()) {
      Card(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        elevation = 0.5.dp,
        backgroundColor = Color.White,
      ) {
        Column(modifier = Modifier.statusBarsPadding()) {
          ToolbarCompose()
          TabLayoutCompose(selectedTabIndexState)
        }
      }
      val pagerState = rememberPagerState(
        initialPage = Snapshot.withoutReadObservation { selectedTabIndexState.intValue },
      ) { 2 }
      HorizontalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
      ) {
        when (it) {
          0 -> formatScreen.Content()
          1 -> codeScreen.Content()
        }
      }
      LaunchedEffect(Unit) {
        snapshotFlow { selectedTabIndexState.value }.collect {
          if (it != pagerState.currentPage) {
            pagerState.animateScrollToPage(it)
          }
        }
      }
      LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }.collect {
          selectedTabIndexState.intValue = it
        }
      }
    }
  }

  @Composable
  private fun ToolbarCompose() {
    val title = remember { mutableStateOf(requestUnit.title.value) }
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      EditTextCompose(
        text = title,
        modifier = Modifier.align(Alignment.Center),
        isShowIndicatorLine = false,
        textStyle = TextStyle(
          color = LocalAppColors.current.tvLv2,
          fontSize = 21.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        ),
      )
      val navigator = LocalNavigator.current
      Box(
        modifier = Modifier.align(Alignment.CenterStart)
          .padding(start = 12.dp)
          .size(32.dp)
          .clickableCardIndicator {
            requestUnit.title.value = title.value
            navigator?.pop()
          },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier,
          painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
          contentDescription = null,
        )
      }
      if (requestContent.requestUnits.any { it.id == requestUnit.id }) {
        Box(
          modifier = Modifier.align(Alignment.CenterEnd)
            .padding(end = 12.dp)
            .size(32.dp)
            .clickableCardIndicator { clickDelete(requestUnit, navigator) },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            modifier = Modifier,
            painter = rememberVectorPainter(Icons.Default.DeleteOutline),
            contentDescription = null,
          )
        }
      }
    }
  }
}

private fun clickDelete(
  requestUnit: RequestUnit,
  navigator: Navigator?
) {
  showChooseDialog(
    onClickPositiveBtn = {
      requestUnit.delete()
      navigator?.pop()
      hide()
    }
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = "确认删除吗？",
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun TabLayoutCompose(selectedTabIndexState: MutableIntState) {
  TabRow(
    modifier = Modifier.fillMaxWidth().height(46.dp),
    selectedTabIndex = selectedTabIndexState.intValue,
    backgroundColor = Color.Transparent,
    divider = {},
    indicator = {
      Box(
        modifier = Modifier.tabIndicatorOffset(it[selectedTabIndexState.intValue]),
        contentAlignment = Alignment.Center,
      ) {
        Spacer(
          modifier = Modifier.size(80.dp, 3.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.secondary)
        )
      }
    }
  ) {
    TabCompose(
      text = "请求格式",
      tabIndex = 0,
      selectedTabIndexState = selectedTabIndexState,
    )
    TabCompose(
      text = "请求脚本",
      tabIndex = 1,
      selectedTabIndexState = selectedTabIndexState,
    )
  }
}

@Composable
private fun TabCompose(
  text: String,
  tabIndex: Int,
  selectedTabIndexState: MutableIntState,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Box(
      modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
        selectedTabIndexState.intValue = tabIndex
      },
      contentAlignment = Alignment.Center,
    ) {
      val selectedTextColor = LocalAppColors.current.tvLv2
      val unSelectedTextColor = selectedTextColor.copy(alpha = 0.6F)
      Text(
        text = text,
        fontSize = 18.sp,
        color = if (selectedTabIndexState.intValue == tabIndex) selectedTextColor else unSelectedTextColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )
    }
  }
}


