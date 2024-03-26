package com.course.source.app.web.source.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.view.code.CodeCompose
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.seconds

/**
 * .
 *
 * @author 985892345
 * 2024/3/25 12:04
 */
@Serializable
@ObjectSerializable
class RequestTestScreen(
  val requestContentName: String,
  val sourceData: String,
  val serviceKey: String,
  val parameterWithHint: LinkedHashMap<String, String>,
) : Screen {

  @Transient
  private val requestContent = RequestContent.RequestMap.getValue(requestContentName)

  @Composable
  override fun Content() {
    Column(modifier = Modifier.systemBarsPadding()) {
      ToolbarCompose()
      Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        val editTitleHintContent = parameterWithHint.map {
          Triple(it.key, it.value, mutableStateOf(""))
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
        ResultCompose(
          requestContent = requestContent,
          sourceData = sourceData,
          serviceKey = serviceKey,
          parameterWithValue = editTitleHintContent.groupBy { it.first }
            .mapValues { it.value.first().third }
        )
      }
    }
  }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ToolbarCompose() {
  Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
    Text(
      modifier = Modifier.align(Alignment.Center),
      text = "测试",
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
        painter = painterResource(DrawableResource("drawable/ic_back.xml")),
        contentDescription = null,
      )
    }
    Spacer(
      modifier = Modifier.align(Alignment.BottomStart)
        .background(Color(0xDDDEDEDE))
        .fillMaxWidth()
        .height(1.dp)
    )
  }
}

private val PrettyPrintJson = Json {
  prettyPrint = true
}

@Composable
private fun ResultCompose(
  requestContent: RequestContent<*>,
  sourceData: String,
  serviceKey: String,
  parameterWithValue: Map<String, State<String>>,
) {
  val resultState = remember { mutableStateOf("") }
  var hint by remember { mutableStateOf("填入上方参数后点击该面板进行请求") }
  val coroutineScope = rememberCoroutineScope()
  Card(modifier = Modifier.padding(top = 6.dp, bottom = 20.dp).fillMaxSize()) {
    CodeCompose(
      modifier = Modifier.clickable {
        if (hint.startsWith("请求中")) {
          return@clickable
        }
        val waitJob = coroutineScope.launch {
          var time = 0
          while (true) {
            hint = "请求中... $time"
            delay(1.seconds)
            time++
          }
        }
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            val result = try {
              Provider.impl(IDataSourceService::class, serviceKey)
                .request(sourceData, parameterWithValue.mapValues { it.value.value })
            } catch (e: CancellationException) {
              hint = "请求被取消"
              throw e
            } catch (e: Exception) {
              hint = "请求失败: \n${e.stackTraceToString()}"
              throw e
            } finally {
              waitJob.cancel()
            }
            try {
              val any = Json.decodeFromString(requestContent.resultSerializer, result)
              @Suppress("UNCHECKED_CAST")
              resultState.value = PrettyPrintJson.encodeToString(
                requestContent.resultSerializer as KSerializer<Any>,
                any
              )
            } catch (e: Exception) {
              hint = "反序列化失败\n返回值: $result\n\n异常信息: ${e.stackTraceToString()}"
            }
          }
        }
      },
      text = resultState,
      hint = hint,
      editable = false,
      minLine = 16,
    )
  }
}

