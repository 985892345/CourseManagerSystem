package com.course.pages.attendance.service.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.account.Account
import com.course.components.base.theme.LocalAppColors
import com.course.pages.attendance.ui.dialog.AttendanceStudentDialog
import com.course.pages.attendance.ui.dialog.AttendanceTeacherDialog
import com.course.pages.course.api.item.lesson.ILessonPopBottom
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.source.app.account.AccountType
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 13:32
 */
@ImplProvider(clazz = ILessonPopBottom::class, name = "AttendanceLessonPopBottom")
object AttendanceLessonPopBottom : ILessonPopBottom {

  override val priority: Int
    get() = 0

  @Composable
  override fun Content(data: LessonItemData, dismiss: () -> Unit) {
    Card(
      shape = RoundedCornerShape(8.dp),
      backgroundColor = Color(0xFFE8F0FC),
      elevation = 0.5.dp,
    ) {
      val navigator = LocalNavigator.current
      Box(
        modifier = Modifier.height(30.dp).clickable {
          dismiss.invoke()
          when (Account.value?.type) {
            AccountType.Student -> AttendanceStudentDialog(data, navigator).show()
            AccountType.Teacher -> AttendanceTeacherDialog(data, navigator).show()
            null -> Unit
          }
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