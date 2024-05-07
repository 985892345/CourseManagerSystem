package com.course.source.app.local.source.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.utils.navigator.BaseScreen
import com.course.components.view.code.CodeCompose
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/25 15:22
 */
@Serializable
class RequestUnitFormatScreen(
  val format: String,
  val parameterWithHint: Map<String, String>,
) : BaseScreen() {

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
      Text(text = "输入: ")
      if (parameterWithHint.isEmpty()) {
        Text(
          text = "无",
          color = Color.DarkGray,
          fontSize = 13.sp,
          modifier = Modifier.padding(start = 16.dp)
        )
      } else {
        parameterWithHint.forEach {
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
        val formatState = remember { mutableStateOf(format) }
          .apply { value = format }
        CodeCompose(
          text = formatState,
          editable = false,
        )
      }
    }
  }
}