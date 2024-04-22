package com.course.pages.course.ui.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.course.ui.pager.CoursePagerState
import com.course.shared.time.MinuteTime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 14:39
 */

@Composable
fun CoursePagerState.CourseTimelineCompose(
  modifier: Modifier = Modifier.width(36.dp),
) {
  Column(
    modifier = Modifier.then(modifier).drawNowTimeLine(timeline)
  ) {
    timeline.data.fastForEach {
      it.apply { Content() }
    }
  }
}

// 绘制当前时间线
@Composable
private fun Modifier.drawNowTimeLine(timeline: CourseTimeline): Modifier {
  val nowTimeState = remember { mutableStateOf(MinuteTime.now()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1.minutes)
      nowTimeState.value = nowTimeState.value.plusMinutes(1)
    }
  }
  return this then drawBehind {
    var allWeight = 0F
    var nowWeight = 0F
    val now = nowTimeState.value.let {
      if (it < timeline.delayMinuteTime) (24 + it.hour) * 60 + it.minute
      else it.hour * 60 + it.minute
    }
    timeline.data.fastForEach {
      allWeight += it.nowWeight
      val start = if (it.startTime < timeline.delayMinuteTime) {
        (24 + it.startTime.hour) * 60 + it.startTime.minute
      } else it.startTime.hour * 60 + it.startTime.minute
      val end = if (it.endTime <= timeline.delayMinuteTime) {
        (24 + it.startTime.hour) * 60 + it.startTime.minute
      } else it.endTime.hour * 60 + it.endTime.minute
      if (now >= end) {
        nowWeight += it.nowWeight
      } else if (now >= start) {
        nowWeight += (now - start) / (end - start).toFloat() * it.nowWeight
      }
    }
    val radius = 3.dp.toPx()
    val y = nowWeight / allWeight * size.height
    drawCircle(
      color = Color.Gray,
      radius = radius,
      center = Offset(x = radius, y = y),
    )
    drawLine(
      color = Color.Gray,
      start = Offset(x = radius, y = y),
      end = Offset(x = size.width, y = y),
      strokeWidth = 1.dp.toPx()
    )
  }
}
