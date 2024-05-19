package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.item.BottomSheetScheduleItem
import com.course.pages.schedule.api.item.IScheduleCourseItemGroup
import com.course.pages.schedule.api.item.edit.ScheduleColorData
import com.course.pages.schedule.ui.item.ScheduleItem
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:44
 */
class ScheduleCourseItemGroup(
  val colorData: ScheduleColorData? = null,
  val onCreate: (suspend (ScheduleBean) -> Unit)?,
  val onUpdate: (suspend (ScheduleBean) -> Unit)?,
  val onDelete: (suspend (ScheduleBean) -> Unit)?,
  val onClick: (
    item: BottomSheetScheduleItem,
    repeatCurrent: Int,
    weekBeginDate: Date,
    timeline: CourseTimeline,
  ) -> Unit,
) : IScheduleCourseItemGroup {

  private val scheduleItemGroups = mutableStateOf(mutableMapOf<Int, ScheduleItem>())

  private val addScheduleCourseItemGroup by lazy {
    AddScheduleCourseItemGroup(
      colorData = colorData ?: ScheduleColorData(Color(0xFF546E7A), Color(0xFFCFD8DC)),
      onCreate = { onCreate?.invoke(it) },
      onClick = onClick,
    )
  }

  override fun resetData(data: Collection<ScheduleBean>) {
    val oldMap = scheduleItemGroups.value
    val newMap = mutableMapOf<Int, ScheduleItem>()
    data.forEach {
      val oldItemGroup = oldMap[it.id]
      if (oldItemGroup != null) {
        oldItemGroup.changeBean(it)
        newMap[it.id] = oldItemGroup
      } else {
        newMap[it.id] = ScheduleItem(
          bean = it,
          onUpdate = onUpdate,
          onDelete = onDelete,
          onClick = onClick,
        )
      }
    }
    scheduleItemGroups.value = newMap
  }

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    if (onCreate != null) {
      addScheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
    }
    scheduleItemGroups.value.forEach {
      key(it.key) {
        with(it.value) {
          ItemGroupContent(
            weekBeginDate = weekBeginDate,
            timeline = timeline,
            scrollState = scrollState,
          )
        }
      }
    }
  }
}