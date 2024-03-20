package com.course.pages.course.api.item.lesson

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.ui.toast.toast
import com.course.pages.course.api.item.ICourseItem
import com.course.shared.time.MinuteTimeDate

/**
 * .
 *
 * @author 985892345
 * 2024/3/14 17:55
 */
abstract class LessonItem : ICourseItem {

  @Stable
  abstract val bean: LessonItemData

  @Stable
  abstract val backgroundColor: Color

  @Stable
  abstract val lessonNameColor: Color

  @Stable
  abstract val classroomColor: Color

  override val startTime: MinuteTimeDate
    get() = bean.startTime

  override val minuteDuration: Int
    get() = bean.minuteDuration

  override val rank: Int
    get() = 100

  override val itemKey: Any
    get() = bean.lesson.id

  @Composable
  override fun Content() {
    Card(
      modifier = Modifier.padding(1.6.dp),
      shape = RoundedCornerShape(8.dp),
      elevation = 0.5.dp,
      backgroundColor = backgroundColor
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable { toast("还未实现") }
          .padding(7.dp, 8.dp)
      ) {
        Text(
          text = bean.lesson.lessonName,
          textAlign = TextAlign.Center,
          color = lessonNameColor,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          fontSize = 11.sp,
          modifier = Modifier.fillMaxWidth()
        )
        Text(
          text = bean.lesson.classroom,
          textAlign = TextAlign.Center,
          color = classroomColor,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          fontSize = 11.sp,
          modifier = Modifier.fillMaxWidth()
            .align(Alignment.BottomCenter)
        )
      }
    }
  }
}

data class AmCourseItem(
  override val bean: LessonItemData
) : LessonItem() {
  override val backgroundColor: Color
    get() = Color(0xFFF9E7D8)
  override val lessonNameColor: Color
    get() = Color(0xFFFF8015)
  override val classroomColor: Color
    get() = Color(0xFFFF8015)
}

data class PmCourseItem(
  override val bean: LessonItemData
) : LessonItem() {
  override val backgroundColor: Color
    get() = Color(0xFFF9E3E4)
  override val lessonNameColor: Color
    get() = Color(0xFFFF6262)
  override val classroomColor: Color
    get() = Color(0xFFFF6262)
}

data class NightCourseItem(
  override val bean: LessonItemData
) : LessonItem() {
  override val backgroundColor: Color
    get() = Color(0xFFDDE3F8)
  override val lessonNameColor: Color
    get() = Color(0xFF4066EA)
  override val classroomColor: Color
    get() = Color(0xFF4066EA)
}