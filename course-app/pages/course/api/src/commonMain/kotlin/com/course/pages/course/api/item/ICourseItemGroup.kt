package com.course.pages.course.api.item

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import kotlin.math.roundToInt

/**
 * 使用 [singleDayItem] [CardContent] [TopBottomText] 统一样式
 *
 * @author 985892345
 * @date 2024/1/25 13:03
 */
@Stable
interface ICourseItemGroup {

  /**
   * @param weekBeginDate 当前周的开始日期
   * @param scrollState 滚动状态
   */
  @Composable
  fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {}

  /**
   * 显示在单天的 item
   */
  fun Modifier.singleDayItem(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    startTimeDate: MinuteTimeDate,
    minuteDuration: Int,
  ): Modifier = this then layout { measurable, constraints ->
    val heightOffset = calculateItemHeightOffset(
      timeline = timeline,
      startTime = startTimeDate.time,
      minuteDuration = minuteDuration,
    )
    val placeable = measurable.measure(
      Constraints(
        maxWidth = constraints.maxWidth / 7,
        maxHeight = (constraints.maxHeight * heightOffset.y).roundToInt()
      )
    )
    layout(constraints.maxWidth, constraints.maxHeight) {
      val date = timeline.getItemWhichDate(startTimeDate)
      placeable.place(
        x = date.dayOfWeekOrdinal * (constraints.maxWidth / 7),
        y = (constraints.maxHeight * heightOffset.x).roundToInt(),
      )
    }
  }

  companion object {
    fun calculateItemHeightOffsetByMinuteInt(
      timeline: CourseTimeline,
      beginTimeInt: Int,
      finalTimeInt: Int,
    ): Offset {
      return calculateItemHeightOffsetInternal(
        timeline = timeline,
        beginTimeInt = beginTimeInt,
        finalTimeInt = finalTimeInt,
      )
    }

    fun calculateItemHeightOffset(
      timeline: CourseTimeline,
      beginTime: MinuteTime,
      finalTime: MinuteTime,
    ): Offset {
      return calculateItemHeightOffsetInternal(
        timeline = timeline,
        beginTimeInt = beginTime.let {
          if (it < timeline.delayMinuteTime) (24 + it.hour) * 60 + it.minute
          else it.hour * 60 + it.minute
        },
        finalTimeInt = finalTime.let {
          if (it <= timeline.delayMinuteTime) (24 + it.hour) * 60 + it.minute
          else it.hour * 60 + it.minute
        }
      )
    }

    fun calculateItemHeightOffset(
      timeline: CourseTimeline,
      startTime: MinuteTime,
      minuteDuration: Int,
    ): Offset {
      return calculateItemHeightOffset(
        timeline = timeline,
        beginTime = startTime,
        finalTime = startTime.plusMinutes(minuteDuration),
      )
    }
  }
}

/**
 * 添加统一样式的圆角和边距
 */
@Composable
inline fun ICourseItemGroup.CardContent(
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  crossinline content: @Composable () -> Unit
) {
  Card(
    modifier = modifier.padding(1.6.dp),
    shape = RoundedCornerShape(8.dp),
    elevation = 0.5.dp,
    backgroundColor = backgroundColor
  ) {
    content.invoke()
  }
}

/**
 * 添加统一样式的顶部和底部文字
 */
@Composable
fun ICourseItemGroup.TopBottomText(
  top: String,
  topColor: Color,
  bottom: String,
  bottomColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize()
      .padding(horizontal = 7.dp, vertical = 7.dp)
  ) {
    Text(
      text = top,
      textAlign = TextAlign.Center,
      color = topColor,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
      fontSize = 11.sp,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = bottom,
      textAlign = TextAlign.Center,
      color = bottomColor,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      fontSize = 11.sp,
      modifier = Modifier.fillMaxWidth()
        .align(Alignment.BottomCenter)
    )
  }
}

/**
 * 计算 item 顶部偏移量和自身高度
 * x: item 顶部偏移量
 * y: item 高度
 */
private fun calculateItemHeightOffsetInternal(
  timeline: CourseTimeline,
  beginTimeInt: Int,
  finalTimeInt: Int,
): Offset {
  var startWeight = 0F
  var endWeight = 0F
  var allWeight = 0F
  timeline.data.fastForEach {
    allWeight += it.nowWeight
    val startLine = if (it.startTime < timeline.delayMinuteTime) {
      (24 + it.startTime.hour) * 60 + it.startTime.minute
    } else it.startTime.hour * 60 + it.startTime.minute
    val endLine = if (it.endTime <= timeline.delayMinuteTime) {
      (24 + it.endTime.hour) * 60 + it.endTime.minute
    } else it.endTime.hour * 60 + it.endTime.minute
    if (beginTimeInt >= endLine) {
      startWeight += it.nowWeight
    } else if (beginTimeInt >= startLine) {
      startWeight += (beginTimeInt - startLine) / (endLine - startLine).toFloat() * it.nowWeight
    }
    if (finalTimeInt >= endLine) {
      endWeight += it.nowWeight
    } else if (finalTimeInt >= startLine) {
      endWeight += (finalTimeInt - startLine) / (endLine - startLine).toFloat() * it.nowWeight
    }
  }
  return Offset(
    x = startWeight / allWeight,
    y = ((endWeight - startWeight) / allWeight).coerceAtLeast(0F),
  )
}