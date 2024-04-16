package com.course.pages.course.api.item.lesson

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.course.components.base.theme.LocalAppColors
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.CourseItemClickShow
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.api.item.TopBottomText
import com.course.shared.time.MinuteTimeDate

/**
 * .
 *
 * @author 985892345
 * 2024/3/14 17:55
 */
abstract class LessonItem : ICourseItem {

  abstract val bean: LessonItemData

  abstract val backgroundColor: Color

  abstract val lessonNameColor: Color

  abstract val classroomColor: Color

  override val startTime: MinuteTimeDate
    get() = bean.startTime

  override val minuteDuration: Int
    get() = bean.minuteDuration

  override val rank: Int
    get() = 100

  override val itemKey: String
    get() = bean.lesson.courseNum + bean.lesson.classroom + bean.lesson.teacher + bean.startTime

  @Composable
  override fun Content(itemClickShow: CourseItemClickShow) {
    CardContent(backgroundColor) {
      Box(modifier = Modifier.clickable {
        clickItem(itemClickShow)
      }) {
        TopBottomText(
          bean.lesson.course,
          lessonNameColor,
          bean.lesson.classroomSimplify,
          classroomColor
        )
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  private fun clickItem(itemClickShow: CourseItemClickShow) {
    itemClickShow.showItemDetail(this) {
      Box(
        modifier = Modifier.fillMaxWidth()
          .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
      ) {
        Column {
          Text(
            modifier = Modifier.basicMarquee(),
            text = bean.lesson.course,
            fontSize = 22.sp,
            color = LocalAppColors.current.tvLv2,
            fontWeight = FontWeight.Bold,
          )
          Layout(
            content = {
              Text(
                modifier = Modifier.basicMarquee(),
                text = bean.lesson.classroom,
                fontSize = 13.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier,
                text = "⎜${bean.lesson.teacher}",
                fontSize = 13.sp,
                color = LocalAppColors.current.tvLv2,
              )
            },
            measurePolicy = { measurables, constraints ->
              val teacherPlaceable = measurables[1].measure(constraints)
              val classroomPlaceable = measurables[0].measure(Constraints(
                minWidth = 0,
                maxWidth = constraints.maxWidth - teacherPlaceable.width,
              ))
              layout(constraints.maxWidth, teacherPlaceable.height) {
                classroomPlaceable.placeRelative(0, (teacherPlaceable.height - classroomPlaceable.height) / 2)
                teacherPlaceable.placeRelative(classroomPlaceable.width, 0)
              }
            }
          )
          bean.lesson.showOptions.fastForEach {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
              Text(
                modifier = Modifier.align(Alignment.TopStart),
                text = it.first,
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.TopEnd),
                text = it.second,
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
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