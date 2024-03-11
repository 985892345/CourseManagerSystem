package com.course.pages.course.ui.pager.scroll.timeline

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.course.components.utils.time.MinuteTime
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/11 19:19
 */
@Stable
@Serializable
sealed interface CourseTimelineData {
  val text: String
  val fontSize: TextUnit
  val color: Color
  val startTime: MinuteTime
  val endTime: MinuteTime
  val maxWeight: Float
  val nowWeight: Float
  val initialWeight: Float

  fun copyData(): CourseTimelineData

  @Composable
  fun ColumnScope.Content()
}


val TimelineDelayMinuteTime = MinuteTime(4, 0)

fun createTimeline(): List<CourseTimelineData> = Timeline.map { it.copyData() }

val Timeline = listOf(
  MutableTimelineData(
    text = "···",
    startTime = TimelineDelayMinuteTime,
    endTime = MinuteTime(8, 0),
    maxWeight = 4F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
  ),
  FixedTimelineData(
    text = "1",
    startTime = MinuteTime(8, 0),
    endTime = MinuteTime(8, 45),
  ),
  FixedTimelineData(
    text = "2",
    startTime = MinuteTime(8, 55),
    endTime = MinuteTime(9, 40),
  ),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(9, 40),
    endTime = MinuteTime(10, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
  ),
  FixedTimelineData(
    text = "3",
    startTime = MinuteTime(10, 15),
    endTime = MinuteTime(11, 0),
  ),
  FixedTimelineData(
    text = "4",
    startTime = MinuteTime(11, 10),
    endTime = MinuteTime(11, 55),
  ),
  MutableTimelineData(
    text = "中午",
    startTime = MinuteTime(11, 55),
    endTime = MinuteTime(14, 0),
    maxWeight = 2F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
  ),
  FixedTimelineData(
    text = "5",
    startTime = MinuteTime(14, 0),
    endTime = MinuteTime(14, 45),
  ),
  FixedTimelineData(
    text = "6",
    startTime = MinuteTime(14, 55),
    endTime = MinuteTime(15, 40),
  ),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(15, 40),
    endTime = MinuteTime(16, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
  ),
  FixedTimelineData(
    text = "7",
    startTime = MinuteTime(16, 15),
    endTime = MinuteTime(17, 0),
  ),
  FixedTimelineData(
    text = "8",
    startTime = MinuteTime(17, 10),
    endTime = MinuteTime(17, 55),
  ),
  MutableTimelineData(
    text = "傍晚",
    startTime = MinuteTime(17, 55),
    endTime = MinuteTime(19, 0),
    maxWeight = 1F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
  ),
  FixedTimelineData(
    text = "9",
    startTime = MinuteTime(19, 0),
    endTime = MinuteTime(19, 45),
  ),
  FixedTimelineData(
    text = "10",
    startTime = MinuteTime(19, 55),
    endTime = MinuteTime(20, 40),
  ),
  FixedTimelineData(
    text = "11",
    startTime = MinuteTime(20, 50),
    endTime = MinuteTime(21, 35),
  ),
  FixedTimelineData(
    text = "12",
    startTime = MinuteTime(21, 45),
    endTime = MinuteTime(22, 30),
  ),
  MutableTimelineData(
    text = "···",
    startTime = MinuteTime(22, 30),
    endTime = TimelineDelayMinuteTime,
    maxWeight = 5.5F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
  ),
)