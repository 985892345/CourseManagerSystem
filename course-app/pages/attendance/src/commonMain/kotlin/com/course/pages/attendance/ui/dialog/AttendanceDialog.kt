package com.course.pages.attendance.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.base.ui.toast.toast
import com.course.components.base.ui.toast.toastLong
import com.course.components.utils.compose.dialog.Dialog
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.view.edit.EditTextCompose
import com.course.pages.attendance.ui.page.AskForLeaveScreen
import com.course.pages.attendance.ui.page.AttendanceHistoryScreen
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.source.app.attendance.AttendanceApi
import com.course.source.app.attendance.AttendanceCodeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 13:48
 */
class AttendanceDialog(
  val data: LessonItemData,
  val navigator: Navigator?,
) : Dialog() {
  override val priority: Int
    get() = 100

  override val properties: DialogProperties = DialogProperties(
    dismissOnBackPress = true,
    dismissOnClickOutside = true,
  )

  override val onDismissRequest: Dialog.() -> Unit = {
    hide()
  }

  @Composable
  override fun Content() {
    val code = remember { mutableStateOf("") }
    Column(
      modifier = Modifier.width(300.dp).padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        modifier = Modifier.padding(top = 8.dp),
        text = "考勤",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )
      EditTextCompose(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(24.dp),
        text = code,
        hint = "请输入考勤码",
        keyboardType = KeyboardType.Number,
        textStyle = remember {
          TextStyle(
            textAlign = TextAlign.Center
          )
        },
        onValueChange = {
          if (it.isEmpty() || it.toIntOrNull() != null) {
            code.value = it
          }
        }
      )
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(36.dp),
        backgroundColor = Color(0xFF4A44E4),
        shape = RoundedCornerShape(8.dp),
      ) {
        val coroutineScope = rememberCoroutineScope()
        Box(
          modifier = Modifier.clickable {
            coroutineScope.launch(Dispatchers.IO) {
              runCatching {
                Source.api(AttendanceApi::class)
                  .postAttendanceCode(data.lesson.courseNum, code.value)
                  .getOrThrow()
              }.tryThrowCancellationException().onSuccess {
                when (it) {
                  AttendanceCodeStatus.Success -> {
                    hide()
                    toast("签到成功")
                  }
                  AttendanceCodeStatus.Late -> toastLong("迟到")
                  AttendanceCodeStatus.Invalid -> toast("无效考勤码")
                }
              }.onFailure {
                toast("签到异常")
              }
            }
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "提交考勤", color = Color.White)
        }
      }
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(36.dp),
        backgroundColor = Color(0xFFC3D4EE),
        shape = RoundedCornerShape(8.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            hide()
            navigator?.push(AskForLeaveScreen(data))
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "请假申请", textAlign = TextAlign.Center)
        }
      }
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp).height(36.dp),
        backgroundColor = Color(0xFFC3D4EE),
        shape = RoundedCornerShape(8.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            hide()
            navigator?.push(AttendanceHistoryScreen(data))
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "考勤记录", textAlign = TextAlign.Center)
        }
      }
    }
  }
}