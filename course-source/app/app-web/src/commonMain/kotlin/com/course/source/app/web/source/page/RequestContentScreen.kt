package com.course.source.app.web.source.page

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.dialog.showChooseDialog
import com.course.components.base.ui.dialog.showDialog
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.view.code.CodeCompose
import com.course.components.view.drag.DragItemState
import com.course.components.view.drag.DraggableColumn
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.request.RequestUnit
import com.course.source.app.web.request.RequestUnit.RequestUnitStatus.Failure
import com.course.source.app.web.request.RequestUnit.RequestUnitStatus.None
import com.course.source.app.web.request.RequestUnit.RequestUnitStatus.Requesting
import com.course.source.app.web.request.RequestUnit.RequestUnitStatus.Success
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 21:03
 */
@Serializable
@ObjectSerializable
class RequestContentScreen(
  private val requestContentKey: String
) : Screen {

  @Transient
  private val requestContent = RequestContent.find(requestContentKey)!!

  @Transient
  private val floatBtnAnimFraction = mutableFloatStateOf(0F)

  @Composable
  override fun Content() {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.pointerInput(Unit) {
      awaitEachGesture {
        awaitFirstDown(false, PointerEventPass.Initial)
        // 接收每次 ACTION_DOWN 事件
        if (floatBtnAnimFraction.value == 1F) {
          coroutineScope.launch { closeFloatBtn() }
        }
      }
    }) {
      Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        ToolbarCompose(requestContent)
        ListCompose(requestContent)
      }
      FloatingActionButtonCompose()
    }
  }

  @Composable
  private fun BoxScope.FloatingActionButtonCompose() {
    val dataSourceServices = remember {
      Provider.getAllImpl(IDataSourceService::class).map { it.value.name to it.value.get() }
    }
    val navigator = LocalNavigator.current
    if (dataSourceServices.size >= 2) {
      dataSourceServices.fastForEachIndexed { index, pair ->
        FloatingActionButton(
          modifier = Modifier.align(Alignment.BottomEnd)
            .padding(end = 46.dp, bottom = 66.dp)
            .size(44.dp)
            .graphicsLayer {
              translationY = (-(index + 1) * 56.dp.toPx() - 8.dp.toPx()) * floatBtnAnimFraction.value
              alpha = minOf(floatBtnAnimFraction.value * 1.25F, 1F) // 因阴影会失效，所以 alpha 特殊设置
            },
          onClick = { navigator?.push(RequestUnitScreen(requestContentKey, pair.first)) }
        ) {
          Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
              rotationZ = -90F * (1 - floatBtnAnimFraction.value)
            },
            contentAlignment = Alignment.Center
          ) {
            pair.second.Identifier()
          }
        }
      }
    }
    val coroutineScope = rememberCoroutineScope()
    FloatingActionButton(
      modifier = Modifier.align(Alignment.BottomEnd)
        .padding(end = 40.dp, bottom = 60.dp),
      onClick = {
        if (dataSourceServices.size >= 2) {
          if (floatBtnAnimFraction.value == 0F) {
            coroutineScope.launch { openFloatBtn() }
          }
        } else if (dataSourceServices.isEmpty()) {
          toast("代码异常，不存在任何数据源服务")
        } else {
          navigator?.push(RequestUnitScreen(requestContentKey, dataSourceServices.first().first))
        }
      },
    ) {
      Image(
        modifier = Modifier.graphicsLayer {
          rotationZ = 45F * floatBtnAnimFraction.value
        },
        painter = rememberVectorPainter(Icons.Filled.Add),
        contentDescription = null
      )
    }
  }

  private suspend fun openFloatBtn() {
    if (floatBtnAnimFraction.value == 0F) {
      animate(0F, 1F, animationSpec = tween()) { value, _ ->
        floatBtnAnimFraction.value = value
      }
    }
  }

  private suspend fun closeFloatBtn() {
    if (floatBtnAnimFraction.value == 1F) {
      animate(1F, 0F, animationSpec = tween()) { value, _ ->
        floatBtnAnimFraction.value = value
      }
    }
  }
}

@Composable
private fun ToolbarCompose(requestContent: RequestContent<*>) {
  Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
    val title = remember { mutableStateOf(requestContent.title.value) }
    if (requestContent.editable) {
      DisposableEffect(Unit) {
        onDispose { requestContent.title.value = title.value }
      }
      EditTextCompose(
        modifier = Modifier.align(Alignment.Center),
        text = title,
        textStyle = TextStyle(
          fontSize = 21.sp,
          fontWeight = FontWeight.Bold,
          color = LocalAppColors.current.tvLv2,
          textAlign = TextAlign.Center,
        ),
        isShowIndicatorLine = false,
      )
    } else {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = requestContent.name,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
    }
    val navigator = LocalNavigator.current
    Box(
      modifier = Modifier.align(Alignment.CenterStart)
        .padding(start = 12.dp)
        .size(32.dp)
        .clickableCardIndicator {
          if (requestContent.editable) {
            requestContent.title.value = title.value
          }
          navigator?.pop()
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = null,
      )
    }
    Spacer(
      modifier = Modifier.align(Alignment.BottomStart)
        .background(Color(0xDDDEDEDE))
        .fillMaxWidth()
        .height(1.dp)
    )
    Box(
      modifier = Modifier.align(Alignment.CenterEnd)
        .padding(end = 12.dp)
        .size(32.dp)
        .clickableCardIndicator {
          showSettingDialog(requestContent)
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = null,
      )
    }
  }
}

private fun showSettingDialog(requestContent: RequestContent<*>) {
  val cacheExpiration = mutableStateOf(
    (requestContent.cacheExpiration.milliseconds.inWholeMinutes / 60F).toString()
  )
  showChooseDialog(
    width = 280.dp,
    height = 160.dp,
    properties = DialogProperties(
      dismissOnClickOutside = false,
    ),
    onClickPositionBtn = {
      requestContent.cacheExpiration =
        cacheExpiration.value.toDouble().hours.inWholeMilliseconds
      hide()
    }
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
    ) {
      Row(modifier = Modifier.height(IntrinsicSize.Min).align(Alignment.Center)) {
        Text(text = "缓存过期时间：")
        EditTextCompose(
          modifier = Modifier.width(60.dp).fillMaxHeight(),
          text = cacheExpiration,
          textStyle = TextStyle(
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
          ),
          keyboardType = KeyboardType.Decimal,
        )
        Text(text = "小时")
      }
    }
  }
}

@Composable
private fun ListCompose(requestContent: RequestContent<*>) {
  DraggableColumn(
    items = requestContent.requestUnits,
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ListItemCompose(
      requestContent = requestContent,
    )
  }
}

@Composable
private fun DragItemState<RequestUnit>.ListItemCompose(
  requestContent: RequestContent<*>,
) {
  val requestUnit = item
  val elevation by animateDpAsState(if (isDragging) 4.dp else 2.dp)
  Card(
    modifier = Modifier.fillMaxWidth()
      .wrapContentHeight(),
    elevation = elevation,
  ) {
    val navigator = LocalNavigator.current
    Box(modifier = Modifier.clickable {
      if (Provider.implOrNull(IDataSourceService::class, requestUnit.serviceKey) != null) {
        navigator?.push(RequestUnitScreen(requestContent.key, requestUnit.id.toString()))
      } else {
        toast("代码异常，该数据源服务不存在\nkey=${requestUnit.serviceKey}")
      }
    }) {
      Column(modifier = Modifier.padding(start = 14.dp, top = 14.dp)) {
        val title = remember { mutableStateOf(requestUnit.title.value) }
        DisposableEffect(Unit) {
          onDispose {
            requestUnit.title.value = title.value
          }
        }
        EditTextCompose(
          text = title,
          textStyle = TextStyle(
            fontSize = 18.sp,
            color = LocalAppColors.current.tvLv1,
            fontWeight = FontWeight.Bold
          ),
          isShowIndicatorLine = false,
        )
        Text(
          modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
          text = AnnotatedString.Builder().append(
            AnnotatedString(
              "状态: ",
              SpanStyle(color = LocalAppColors.current.tvLv3),
            ),
            getRequestStatue(requestUnit)
          ).toAnnotatedString(),
          fontSize = 14.sp,
        )
      }
      RequestResultImageCompose(requestContent, requestUnit)
      Box(
        modifier = Modifier.align(Alignment.CenterEnd)
          .padding(end = 4.dp)
          .size(32.dp)
          .draggableItem(false),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.DragHandle,
          contentDescription = null,
        )
      }
    }
  }
}

private val PrettyPrintJson = Json(RequestContent.Json) {
  prettyPrint = true
}

@Composable
private fun BoxScope.RequestResultImageCompose(requestContent: RequestContent<*>, requestUnit: RequestUnit) {
  if (requestUnit.requestUnitStatus == None || requestUnit.requestUnitStatus == Requesting) return
  Box(
    modifier = Modifier.align(Alignment.CenterEnd)
      .padding(end = 36.dp)
      .clickableCardIndicator {
        showDialog {
          CodeCompose(
            modifier = Modifier.width(320.dp).heightIn(min = 400.dp, max = 600.dp),
            text = remember {
              @Suppress("UNCHECKED_CAST")
              mutableStateOf(
                when (requestUnit.requestUnitStatus) {
                  None, Requesting -> ""
                  Success -> PrettyPrintJson.encodeToString(
                    requestContent.resultSerializer as KSerializer<Any>,
                    PrettyPrintJson.decodeFromString(
                      requestContent.resultSerializer,
                      (requestUnit.response1 ?: "") + requestUnit.response2
                    )
                  )
                  Failure -> "请求失败\n返回值: ${requestUnit.response1}\n\n异常信息: ${requestUnit.error}"
                }
              )
            },
            editable = false,
          )
        }
      }
  ) {
    Icon(
      modifier = Modifier.padding(4.dp),
      painter = when (requestUnit.requestUnitStatus) {
        None, Requesting -> ColorPainter(Color.Gray)
        Success -> rememberVectorPainter(Icons.Default.DataObject)
        Failure -> rememberVectorPainter(Icons.Default.ErrorOutline)
      },
      contentDescription = null,
    )
  }
}

@Stable
private fun getRequestStatue(requestUnit: RequestUnit): AnnotatedString {
  return when (requestUnit.requestUnitStatus) {
    None -> AnnotatedString("未触发请求", SpanStyle(Color(0xFF0099CC)))
    Requesting -> AnnotatedString("请求中", SpanStyle(Color(0xFFFF8800)))
    Success -> AnnotatedString("成功", SpanStyle(Color(0xFF669900)))
    Failure -> AnnotatedString("失败", SpanStyle(Color(0xFFCC0000)))
  }
}
