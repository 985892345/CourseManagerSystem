package com.course.source.app.web.source.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.dialog.showChooseDialog
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.view.code.CodeCompose
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.request.RequestUnit
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

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
) : Screen {

  @Transient
  private val requestContent = RequestContent.RequestMap.getValue(requestContentName)

  @Transient
  private val requestUnit = requestUnitIdOrServiceKey.toIntOrNull()?.let { id ->
    requestContent.requestUnits.singleOrNull { it.id == id }
  } ?: RequestUnit(
    title = mutableStateOf(requestContent.name +
        (requestContent.requestUnits.maxOfOrNull { it.id }?.plus(1) ?: 0)),
    serviceKey = requestUnitIdOrServiceKey,
    id = requestContent.requestUnits.maxOfOrNull { it.id }?.plus(1) ?: 0
  )

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  override fun Content() {
    Column(modifier = Modifier.fillMaxSize()) {
      val selectedTabIndexState = remember { mutableIntStateOf(0) }
      Card(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
      ) {
        Column(modifier = Modifier.systemBarsPadding()) {
          ToolbarCompose(requestContent, requestUnit)
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
          0 -> FormatPage(requestContent)
          1 -> CodePage(requestContent, requestUnit)
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
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ToolbarCompose(requestContent: RequestContent<*>, requestUnit: RequestUnit) {
  Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
    val text = remember {
      mutableStateOf(requestUnit.title.value)
    }
    EditTextCompose(
      text = text,
      modifier = Modifier.align(Alignment.Center),
      isShowIndicatorLine = false,
      textStyle = TextStyle(
        color = LocalAppColors.current.tvLv2,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      ),
    ) {
      text.value = it
      requestUnit.title.value = it
    }
    val navigator = LocalNavigator.current
    Box(
      modifier = Modifier.align(Alignment.CenterStart)
        .padding(start = 12.dp)
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable {
          navigator?.pop()
        },
      contentAlignment = Alignment.Center,
    ) {
      Image(
        modifier = Modifier.size(16.dp),
        painter = painterResource(DrawableResource("drawable/ic_back.xml")),
        contentDescription = null,
      )
    }
    if (requestContent.requestUnits.any { it.id == requestUnit.id }) {
      Box(
        modifier = Modifier.align(Alignment.CenterEnd)
          .padding(end = 12.dp)
          .size(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .clickable { clickDelete(requestContent, requestUnit, navigator) },
        contentAlignment = Alignment.Center,
      ) {
        Image(
          modifier = Modifier.size(22.dp),
          painter = painterResource(DrawableResource("drawable/ic_delete.xml")),
          contentDescription = null,
        )
      }
    }
  }
}

private fun clickDelete(
  requestContent: RequestContent<*>,
  requestUnit: RequestUnit,
  navigator: Navigator?
) {
  showChooseDialog(
    onClickPositionBtn = {
      requestContent.requestUnits.remove(requestUnit)
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

@Composable
private fun FormatPage(requestContent: RequestContent<*>) {
  Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
    Text(text = "输入: ")
    if (requestContent.parameterWithHint.isEmpty()) {
      Text(
        text = "无",
        color = Color.DarkGray,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 16.dp)
      )
    } else {
      requestContent.parameterWithHint.forEach {
        Text(
          text = "${it.key}: ${it.value}",
          color = Color.Gray,
          fontSize = 13.sp,
          modifier = Modifier.padding(start = 16.dp, top = 3.dp)
        )
      }
    }
    Text(text = "输出: ", modifier = Modifier.padding(top = 8.dp))
    Card(
      modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
    ) {
      val formatState = remember { mutableStateOf(requestContent.format) }
        .apply { value = requestContent.format }
      CodeCompose(
        text = formatState,
        editable = false,
      )
    }
  }
}

@Composable
private fun CodePage(requestContent: RequestContent<*>, requestUnit: RequestUnit) {
  val dataSourceService = remember(requestUnit.serviceKey) {
    Provider.impl(IDataSourceService::class, requestUnit.serviceKey)
  }
  val dataSourceConfig = remember(requestUnit.sourceData) {
    dataSourceService.config(requestUnit.sourceData)
  }
  Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
    val editTitleHintContent = remember(dataSourceConfig) {
      dataSourceConfig.editTitleHintContent.map {
        Triple(it.first, it.second, mutableStateOf(it.third ?: ""))
      }
    }
    editTitleHintContent.forEach {
      Row(modifier = Modifier.padding(vertical = 6.dp).height(IntrinsicSize.Min)) {
        Text(text = it.first + ": ", fontSize = 14.sp, modifier = Modifier)
        EditTextCompose(
          text = it.third,
          modifier = Modifier.weight(1F).align(Alignment.Bottom),
          textStyle = TextStyle(
            fontSize = 14.sp,
          ),
          hint = it.second,
        )
      }
    }
    Box(
      modifier = Modifier.padding(top = 8.dp).fillMaxWidth().weight(1F)
    ) {
      val codeState = remember { mutableStateOf(dataSourceConfig.codeContent ?: "") }
      Card(modifier = Modifier.padding(bottom = 90.dp).fillMaxSize()) {
        CodeCompose(
          text = codeState,
          hint = dataSourceConfig.codeHint,
        )
      }
      Row(
        modifier = Modifier.align(Alignment.BottomCenter)
          .padding(bottom = 16.dp)
          .fillMaxWidth()
          .height(50.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Card(
          modifier = Modifier.size(100.dp, 40.dp),
          shape = MaterialTheme.shapes.large,
        ) {
          val navigator = LocalNavigator.current
          Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable {
            val newSourceData = dataSourceService.createSourceData(
              code = codeState.value,
              editContents = editTitleHintContent.map { it.third.value },
            )
            if (newSourceData != null) {
              requestUnit.sourceData = newSourceData
              if (requestContent.requestUnits.all { it.id != requestUnit.id }) {
                requestContent.requestUnits.add(requestUnit)
              }
              requestContent.save()
              navigator?.pop()
            }
          }) {
            Text(text = "确认", modifier = Modifier)
          }
        }
        Card(
          modifier = Modifier.size(100.dp, 40.dp),
          shape = MaterialTheme.shapes.large,
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable {

          }) {
            Text(text = "测试", modifier = Modifier)
          }
        }
      }
    }
  }
}