package com.course.pages.course.ui.pager.scroll.timeline

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.course.shared.time.MinuteTime
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

  /**
   * 时间轴存在越过24点的情况，添加该变量用于表示是否存在明天的时间段
   */
  val hasTomorrow: Boolean

  fun copyData(): CourseTimelineData

  @Composable
  fun ColumnScope.Content()
}

@Stable
@Serializable
class CourseTimeline(
  val delayMinuteTime: MinuteTime = TimelineDelayMinuteTime,
  val data: List<CourseTimelineData> = Timeline
)

private val TimelineDelayMinuteTime = MinuteTime(4, 0)

private val Timeline = listOf(
  MutableTimelineData(
    text = "···",
    startTime = TimelineDelayMinuteTime,
    endTime = MinuteTime(8, 0),
    maxWeight = 4F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(1, false),
  LessonTimelineData(2, false),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(9, 40),
    endTime = MinuteTime(10, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
    false
  ),
  LessonTimelineData(3, false),
  LessonTimelineData(4, false),
  MutableTimelineData(
    text = "中午",
    startTime = MinuteTime(11, 55),
    endTime = MinuteTime(14, 0),
    maxWeight = 2F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(5, false),
  LessonTimelineData(6, false),
  FixedTimelineData(
    text = "大课间",
    startTime = MinuteTime(15, 40),
    endTime = MinuteTime(16, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(7, false),
  LessonTimelineData(8, false),
  MutableTimelineData(
    text = "傍晚",
    startTime = MinuteTime(17, 55),
    endTime = MinuteTime(19, 0),
    maxWeight = 1F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(9, false),
  LessonTimelineData(10, false),
  LessonTimelineData(11, false),
  LessonTimelineData(12, false),
  MutableTimelineData(
    text = "···",
    startTime = MinuteTime(22, 30),
    endTime = TimelineDelayMinuteTime,
    maxWeight = 5.5F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
    hasTomorrow = true,
  ),
)