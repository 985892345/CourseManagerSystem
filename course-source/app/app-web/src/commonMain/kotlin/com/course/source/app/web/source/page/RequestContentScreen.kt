package com.course.source.app.web.source.page

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.dialog.showChooseDialog
import com.course.components.base.ui.toast.toast
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.view.calendar.edit.EditTextCompose
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
  private val requestContent = RequestContent.RequestMap.getValue(requestContentKey)

  @Composable
  override fun Content() {
    Box {
      Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        ToolbarCompose(requestContent)
        ListCompose(requestContent)
      }
      FloatingActionButtonCompose(requestContentKey)
    }
  }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BoxScope.FloatingActionButtonCompose(requestContentKey: String) {
  val dataSourceServices = remember {
    Provider.getAllImpl(IDataSourceService::class).map { it.value.name to it.value.get() }
  }
  var animationFraction by remember { mutableFloatStateOf(0F) }
  val navigator = LocalNavigator.current
  if (dataSourceServices.size >= 2) {
    dataSourceServices.fastForEachIndexed { index, pair ->
      FloatingActionButton(
        modifier = Modifier.align(Alignment.BottomEnd)
          .padding(end = 46.dp, bottom = 66.dp)
          .size(44.dp)
          .graphicsLayer {
            translationY = (-(index + 1) * 56.dp.toPx() - 8.dp.toPx()) * animationFraction
            alpha = minOf(animationFraction * 1.25F, 1F) // 因阴影会失效，所以 alpha 特殊设置
          },
        onClick = { navigator?.push(RequestUnitScreen(requestContentKey, pair.first)) }
      ) {
        Box(
          modifier = Modifier.fillMaxSize().graphicsLayer {
            rotationZ = -90F * (1 - animationFraction)
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
        if (animationFraction == 0F || animationFraction == 1F)
          coroutineScope.launch {
            animate(
              animationFraction,
              abs(animationFraction - 1),
              animationSpec = tween(),
            ) { value, _ ->
              animationFraction = value
            }
          }
      } else if (dataSourceServices.isEmpty()) {
        toast("代码异常，未找到数据源服务")
      } else {
        navigator?.push(RequestUnitScreen(requestContentKey, dataSourceServices.first().first))
      }
    },
  ) {
    Image(
      modifier = Modifier.graphicsLayer {
        rotationZ = 45F * animationFraction
      },
      painter = painterResource(DrawableResource("ic_add.xml")),
      contentDescription = null
    )
  }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ToolbarCompose(requestContent: RequestContent<*>) {
  Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
    Text(
      modifier = Modifier.align(Alignment.Center),
      text = requestContent.name,
      fontSize = 21.sp,
      fontWeight = FontWeight.Bold,
      color = LocalAppColors.current.tvLv2
    )
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
        painter = painterResource(DrawableResource("ic_back.xml")),
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
        .clip(RoundedCornerShape(8.dp))
        .clickable {
          showSettingDialog(requestContent)
        },
      contentAlignment = Alignment.Center,
    ) {
      Image(
        modifier = Modifier.size(22.dp),
        painter = painterResource(DrawableResource("ic_settings.xml")),
        contentDescription = null,
      )
    }
  }
}

@Composable
private fun ListCompose(requestContent: RequestContent<*>) {
}

private fun showSettingDialog(requestContent: RequestContent<*>) {
  val cacheExpiration = mutableStateOf(
    TextFieldValue((requestContent.cacheExpiration.milliseconds.inWholeMinutes / 60F).toString())
  )
  showChooseDialog(
    width = 280.dp,
    properties = DialogProperties(
      dismissOnClickOutside = false,
    ),
    onClickPositionBtn = {
      requestContent.cacheExpiration =
        cacheExpiration.value.text.toDouble().hours.inWholeMilliseconds
      hide()
    }
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
        .padding(start = 16.dp, end = 16.dp, top = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(modifier = Modifier.height(IntrinsicSize.Min)) {
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

