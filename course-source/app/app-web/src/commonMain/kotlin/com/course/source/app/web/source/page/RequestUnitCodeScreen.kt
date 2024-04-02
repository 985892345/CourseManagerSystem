package com.course.source.app.web.source.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.StringStateSerializable
import com.course.components.view.code.CodeCompose
import com.course.components.view.edit.EditTextCompose
import com.course.source.app.web.request.RequestContent
import com.course.source.app.web.request.RequestUnit
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/3/25 15:24
 */
@Serializable
class RequestUnitCodeScreen(
  val requestContentName: String,
  val requestUnit: RequestUnit, // 只是副本
) : BaseScreen() {

  @Transient
  private val requestContent = RequestContent.RequestMap.getValue(requestContentName)

  @Transient
  private val dataSourceService = Provider.impl(IDataSourceService::class, requestUnit.serviceKey)

  @Transient
  private val dataSourceConfig = dataSourceService.config(requestUnit.sourceData)

  @Serializable(StringStateSerializable::class)
  private val codeState = mutableStateOf(dataSourceConfig.codeContent ?: "")

  private val editContents = dataSourceConfig.editTitleHintContent
    .map {
      EditComposeContent(
        it.key,
        it.value.hint,
        mutableStateOf(it.value.content ?: "")
      )
    }
    .toMutableList()

  @Composable
  override fun Content() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
      editContents.forEach {
        Row(modifier = Modifier.padding(vertical = 6.dp).height(IntrinsicSize.Min)) {
          Text(text = it.title + ": ", fontSize = 14.sp, modifier = Modifier)
          EditTextCompose(
            text = it.content,
            modifier = Modifier.weight(1F).align(Alignment.Bottom),
            textStyle = TextStyle(
              fontSize = 14.sp,
            ),
            hint = it.hint,
          )
        }
      }
      Box(
        modifier = Modifier.padding(top = 8.dp).fillMaxWidth().weight(1F)
      ) {
        Card(modifier = Modifier.padding(bottom = 90.dp).fillMaxSize()) {
          CodeCompose(
            text = codeState,
            hint = dataSourceConfig.codeHint,
            minLine = 10,
          )
        }
        BottomBtnCompose()
      }
    }
  }

  @Composable
  private fun BoxScope.BottomBtnCompose() {
    Row(
      modifier = Modifier.align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
        .fillMaxWidth()
        .height(50.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val navigator = LocalNavigator.current
      Card(
        modifier = Modifier.size(100.dp, 40.dp),
        shape = MaterialTheme.shapes.large,
      ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable {
          val newSourceData = dataSourceService.createSourceData(
            code = codeState.value,
            editContents = editContents.map { it.content.value },
          )
          if (newSourceData != null) {
            requestUnit.sourceData = newSourceData
            val origin = requestContent.requestUnits.find { it.id == requestUnit.id }
            if (origin != null) {
              origin.sourceData = newSourceData
            } else {
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
          val newSourceData = dataSourceService.createSourceData(
            code = codeState.value,
            editContents = editContents.map { it.content.value },
          )
          if (newSourceData != null) {
            navigator?.push(
              RequestTestScreen(
                requestContent.name,
                newSourceData,
                requestUnit.serviceKey,
                requestContent.parameterWithHint
              )
            )
          }
        }) {
          Text(text = "测试", modifier = Modifier)
        }
      }
    }
  }
}

@Stable
@Serializable
class EditComposeContent(
  val title: String,
  val hint: String,
  @Serializable(StringStateSerializable::class)
  val content: MutableState<String>,
)