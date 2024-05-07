package com.course.pages.attendance.service.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.pages.attendance.ui.dialog.AttendanceDialog
import com.course.pages.course.api.item.lesson.ILessonPopBottom
import com.course.pages.course.api.item.lesson.LessonItemData
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 13:32
 */
@ImplProvider(clazz = ILessonPopBottom::class, name = "AttendanceLessonPopBottom")
class AttendanceLessonPopBottom : ILessonPopBottom {

  override val priority: Int
    get() = 0

  @Composable
  override fun Content(data: LessonItemData, dismiss: () -> Unit) {
    Card(
      shape = CircleShape,
      backgroundColor = Color(0xFFE8F0FC),
    ) {
      val navigator = LocalNavigator.current
      Box(
        modifier = Modifier.height(30.dp).clickable {
          AttendanceDialog(data, navigator).show()
          dismiss.invoke()
        }.padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "考勤",
          fontSize = 14.sp,
          color = LocalAppColors.current.tvLv2
        )
      }
    }
  }
}