package com.course.pages.notification.ui.type

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.source.app.notification.NotificationContent

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 11:02
 */
@Composable
fun AddScheduleNotificationCompose(content: NotificationContent.AddSchedule) {
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    Text(
      modifier = Modifier,
      text = "新的日程: ${content.scheduleTitle}",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier.padding(top = 4.dp),
      text = "时间: ${content.scheduleStartTime}-${
        content.scheduleStartTime.time.plusMinutes(
          content.scheduleMinuteDuration
        )
      }",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier.padding(top = 4.dp),
      text = content.scheduleDescription,
      fontSize = 13.sp,
      color = Color.Gray,
    )
    Text(
      modifier = Modifier.padding(top = 8.dp).align(Alignment.End),
      text = "由${content.teamName}—${content.teamSenderName}创建",
      fontSize = 11.sp,
      color = Color.LightGray,
    )
  }
}