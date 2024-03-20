package com.course.pages.course.ui.pager.scroll.timeline

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.course.pages.course.api.data.CourseDataProvider.Companion.TimelineDelayMinuteTime
import com.course.shared.time.MinuteTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
  val nowWeight: Float
  val initialWeight: Float

  fun copyData(): CourseTimelineData

  @Composable
  fun ColumnScope.Content()
}


fun createTimeline(): ImmutableList<CourseTimelineData> = Timeline.map { it.copyData() }.toImmutableList()

val Timeline = listOf(
  MutableTimelineData(
    text = "···",
    startTime = TimelineDelayMinuteTime,
    endTime = MinuteTime(8, 0),
    maxWeight = 4F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
  ),
  LessonTimelineData(1),
  LessonTimelineData(2),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(9, 40),
    endTime = MinuteTime(10, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
  ),
  LessonTimelineData(3),
  LessonTimelineData(4),
  MutableTimelineData(
    text = "中午",
    startTime = MinuteTime(11, 55),
    endTime = MinuteTime(14, 0),
    maxWeight = 2F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
  ),
  LessonTimelineData(5),
  LessonTimelineData(6),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(15, 40),
    endTime = MinuteTime(16, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
  ),
  LessonTimelineData(7),
  LessonTimelineData(8),
  MutableTimelineData(
    text = "傍晚",
    startTime = MinuteTime(17, 55),
    endTime = MinuteTime(19, 0),
    maxWeight = 1F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
  ),
  LessonTimelineData(9),
  LessonTimelineData(10),
  LessonTimelineData(11),
  LessonTimelineData(12),
  MutableTimelineData(
    text = "···",
    startTime = MinuteTime(22, 30),
    endTime = TimelineDelayMinuteTime,
    maxWeight = 5.5F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
  ),
)