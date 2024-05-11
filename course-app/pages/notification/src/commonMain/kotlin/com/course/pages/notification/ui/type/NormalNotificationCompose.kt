package com.course.pages.notification.ui.type

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
 * 2024/5/11 11:01
 */
@Composable
fun NormalNotificationCompose(content: NotificationContent.Normal) {
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
    Text(
      modifier = Modifier,
      text = content.title,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      modifier = Modifier.padding(top = 4.dp),
      text = content.content,
      fontSize = 13.sp,
      color = Color.Gray,
    )
  }
}