package com.course.pages.course.api.item.lesson

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import com.course.components.utils.compose.showBottomSheetDialog
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime

/**
 * .
 *
 * @author 985892345
 * 2024/3/14 17:55
 */
class LessonItemGroup : ICourseItemGroup {

  /**
   * 重置数据
   */
  fun resetData(data: Collection<LessonItemData>) {
    oldBeans = data.toList()
    val timeline = oldTimeline
    if (timeline != null) {
      beanMapState.value = data.groupBy { timeline.getItemWhichDate(it.startTime).weekBeginDate }
    }
  }

  private var oldTimeline: CourseTimeline? = null
  private var oldBeans: List<LessonItemData> = emptyList()
  private val beanMapState: MutableState<Map<Date, List<LessonItemData>>> =
    mutableStateOf(emptyMap())

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    if (oldTimeline != timeline) {
      oldTimeline = timeline
      resetData(oldBeans)
    }
    beanMapState.value[weekBeginDate]?.fastForEach {
      CardContent(
        backgroundColor = when {
          it.startTime.time < MinuteTime(12, 0) -> Color(0xFFF9E7D8)
          it.startTime.time < MinuteTime(18, 0) -> Color(0xFFF9E3E4)
          else -> Color(0xFFDDE3F8)
        },
        modifier = Modifier.singleDayItem(
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          startTimeDate = it.startTime,
          minuteDuration = it.minuteDuration,
        )
      ) {
        Box(modifier = Modifier.clickable {
          clickItem(it)
        }) {
          val textColor = when {
            it.startTime.time < MinuteTime(12, 0) -> Color(0xFFFF8015)
            it.startTime.time < MinuteTime(18, 0) -> Color(0xFFFF6262)
            else -> Color(0xFF4066EA)
          }
          TopBottomText(
            it.lesson.course,
            textColor,
            it.lesson.classroomSimplify,
            textColor
          )
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  private fun clickItem(data: LessonItemData) {
    showBottomSheetDialog {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
      ) {
        Column(
          modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
          Text(
            modifier = Modifier.basicMarquee(),
            text = data.lesson.course,
            fontSize = 22.sp,
            color = LocalAppColors.current.tvLv2,
            fontWeight = FontWeight.Bold,
          )
          Layout(
            content = {
              Text(
                modifier = Modifier.basicMarquee(),
                text = data.lesson.classroom,
                fontSize = 13.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier,
                text = "⎜${data.lesson.teacher}",
                fontSize = 13.sp,
                color = LocalAppColors.current.tvLv2,
              )
            },
            measurePolicy = { measurables, constraints ->
              val teacherPlaceable = measurables[1].measure(constraints)
              val classroomPlaceable = measurables[0].measure(
                Constraints(
                  minWidth = 0,
                  maxWidth = constraints.maxWidth - teacherPlaceable.width,
                )
              )
              layout(constraints.maxWidth, teacherPlaceable.height) {
                classroomPlaceable.placeRelative(
                  0,
                  (teacherPlaceable.height - classroomPlaceable.height) / 2
                )
                teacherPlaceable.placeRelative(classroomPlaceable.width, 0)
              }
            }
          )
          data.lesson.showOptions.fastForEach {
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