package com.course.pages.attendance.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.navigator.Navigator
import com.course.components.utils.compose.dialog.Dialog
import com.course.pages.attendance.ui.page.AttendanceClassHistoryScreen
import com.course.pages.attendance.ui.page.AttendancePostScreen
import com.course.pages.course.api.item.lesson.LessonItemData

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 19:28
 */
class AttendanceTeacherDialog(
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
    Column(
      modifier = Modifier.width(300.dp).padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(36.dp),
        backgroundColor = Color(0xFF4A44E4),
        shape = RoundedCornerShape(8.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            hide()
            navigator?.push(AttendancePostScreen(data))
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "发布考勤", color = Color.White)
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
            navigator?.push(AttendanceClassHistoryScreen(data))
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "考勤记录")
        }
      }
    }
  }
}