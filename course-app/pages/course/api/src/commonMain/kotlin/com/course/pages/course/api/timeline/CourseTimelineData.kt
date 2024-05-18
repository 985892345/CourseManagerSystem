package com.course.pages.course.api.timeline

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.course.getEndMinuteTime
import com.course.source.app.course.getStartMinuteTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
  val optionText: String
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

  // 开始时间的分钟数，如果存在明天的时间段，则值会大于 24 * 60
  val startTimeInt: Int
    get() = if (!hasTomorrow || startTime > endTime) {
      startTime.hour * 60 + startTime.minute
    } else {
      startTime.hour * 60 + startTime.minute + 24 * 60
    }

  val endTimeInt: Int
    get() = if (!hasTomorrow) {
      endTime.hour * 60 + endTime.minute
    } else {
      endTime.hour * 60 + endTime.minute + 24 * 60
    }

  fun copyData(): CourseTimelineData

  @Composable
  fun ColumnScope.Content()
}

@Stable
@Serializable
class CourseTimeline(
  val delayMinuteTime: MinuteTime = TimelineDelayMinuteTime,
  val data: ImmutableList<CourseTimelineData> = Timeline,
) {
  fun getItemWhichDate(startTimeDate: MinuteTimeDate): Date {
    return if (startTimeDate.time >= delayMinuteTime) {
      startTimeDate.date
    } else {
      startTimeDate.date.minusDays(1)
    }
  }
}

private val TimelineDelayMinuteTime = MinuteTime(4, 0)

private val Timeline = persistentListOf(
  MutableTimelineData(
    text = "···",
    optionText = "凌晨",
    startTime = TimelineDelayMinuteTime,
    endTime = MinuteTime(8, 0),
    maxWeight = 4F,
    initialWeight = 0.1F,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(1, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(1),
    endTime = getStartMinuteTime(2),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(2, false),
  FixedTimelineData(
    text = "大课间",
    optionText = "大课间",
    startTime = MinuteTime(9, 40),
    endTime = MinuteTime(10, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
    hasTomorrow = false
  ),
  LessonTimelineData(3, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(3),
    endTime = getStartMinuteTime(4),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(4, false),
  MutableTimelineData(
    text = "中午",
    optionText = "中午",
    startTime = MinuteTime(11, 55),
    endTime = MinuteTime(14, 0),
    maxWeight = 2F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(5, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(5),
    endTime = getStartMinuteTime(6),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(6, false),
  FixedTimelineData(
    text = "大课间",
    optionText = "大课间",
    startTime = MinuteTime(15, 40),
    endTime = MinuteTime(16, 15),
    weight = 0.05F,
    fontSize = 8.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(7, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(7),
    endTime = getStartMinuteTime(8),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(8, false),
  MutableTimelineData(
    text = "傍晚",
    optionText = "傍晚",
    startTime = MinuteTime(17, 55),
    endTime = MinuteTime(19, 0),
    maxWeight = 1F,
    initialWeight = 0.1F,
    fontSize = 10.sp,
    color = Color.DarkGray,
    hasTomorrow = false,
  ),
  LessonTimelineData(9, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(9),
    endTime = getStartMinuteTime(10),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(10, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(10),
    endTime = getStartMinuteTime(11),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(11, false),
  FixedTimelineData(
    text = "",
    optionText = "课间",
    startTime = getEndMinuteTime(11),
    endTime = getStartMinuteTime(12),
    weight = 0.01F,
    hasTomorrow = false
  ),
  LessonTimelineData(12, false),
  MutableTimelineData(
    text = "···",
    optionText = "深夜",
    startTime = MinuteTime(22, 30),
    endTime = TimelineDelayMinuteTime,
    maxWeight = 5.5F,
    initialWeight = 0.2F,
    color = Color.DarkGray,
    hasTomorrow = true,
  ),
)