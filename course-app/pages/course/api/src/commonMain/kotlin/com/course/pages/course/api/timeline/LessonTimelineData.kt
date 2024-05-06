package com.course.pages.course.api.timeline

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.time.MinuteTime
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/14 16:59
 */
@Serializable
data class LessonTimelineData(
  val lesson: Int,
  override val hasTomorrow: Boolean,
) : CourseTimelineData {

  override val optionText: String = "第${lesson}节"

  override val startTime: MinuteTime = LessonItemData.getStartMinuteTime(lesson)

  override val endTime: MinuteTime = LessonItemData.getEndMinuteTime(lesson)

  override val fontSize: TextUnit
    get() = 12.sp

  override val color: Color
    get() = Color.Unspecified

  override val nowWeight: Float
    get() = 1F
  override val initialWeight: Float
    get() = 1F

  override fun copyData(): CourseTimelineData {
    return copy()
  }

  @Composable
  override fun ColumnScope.Content() {
    Layout(
      modifier = Modifier.weight(nowWeight).fillMaxWidth(),
      content = {
        Text(
          text = lesson.toString(),
          textAlign = TextAlign.Center,
          fontSize = fontSize,
          color = color,
          overflow = TextOverflow.Visible
        )
      },
      measurePolicy = TimelineMeasurePolicy,
    )
  }

  companion object {
    private val TimelineMeasurePolicy = MeasurePolicy { measurables, constraints ->
      val placeable = measurables[0].measure(
        constraints.copy(
          minWidth = 0,
          minHeight = 0,
          maxHeight = Constraints.Infinity
        )
      )
      layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.placeRelative(
          x = (constraints.maxWidth - placeable.width) / 2,
          y = (constraints.maxHeight - placeable.height) / 2
        )
      }
    }
  }
}