package com.course.pages.course.ui.content.pager.scroll

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.course.pages.course.ui.content.pager.CoursePagerCombine

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:39
 */

@Composable
fun CoursePagerCombine.CourseTimelineCompose(
  modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.fillMaxHeight().then(modifier)) {
    repeat(12) {
      Box(modifier = Modifier.weight(1F).fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = (it + 1).toString(), textAlign = TextAlign.Center, fontSize = 12.sp)
      }
    }
  }
}