package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.course.components.base.ui.toast.toast
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.ui.item.PlaceholderScheduleItemGroup
import com.course.shared.time.Date
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:42
 */
class PlaceholderScheduleCourseItemGroup : ICourseItemGroup {

  private val placeholderScheduleItemGroups = SnapshotStateList<PlaceholderScheduleItemGroup>()

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    TouchCompose(weekBeginDate, timeline, scrollState)
    ItemGroupShowCompose(weekBeginDate, timeline, scrollState)
  }

  @Composable
  private fun TouchCompose(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    val coroutineScope = rememberCoroutineScope()
    Spacer(modifier = Modifier.fillMaxSize().zIndex(-99999F).pointerInput(Unit) {
      awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        val longPressPointer = awaitLongPressOrCancellation(down.id)
        if (longPressPointer == null) {
          // 在空白区域非长按时抬起就取消添加的 Item
          val cancelItems = placeholderScheduleItemGroups.toList()
          coroutineScope.launch {
            cancelItems.map {
              launch { it.cancelShow() }
            }.joinAll()
            placeholderScheduleItemGroups.removeAll(cancelItems)
          }
          return@awaitEachGesture
        }
        longPressPointer.consume()
        toast("长按")
        val columnIndex = (longPressPointer.position.x / (size.width / 7)).toInt()
        val initialTime = PlaceholderScheduleItemGroup
          .getMinuteTimeByOffset(timeline, size.height, longPressPointer.position.y)
        val item = createPlaceholderScheduleItem(
          weekBeginDate = weekBeginDate,
          columnIndex = columnIndex,
          initialTime = initialTime,
        )
        placeholderScheduleItemGroups.add(item)
        item.offsetForScrollOuter.floatValue = longPressPointer.position.y - scrollState.value
        while (true) {
          val event = awaitPointerEvent(pass = PointerEventPass.Initial)
          val pointer = event.changes.fastFirstOrNull { it.id == longPressPointer.id }
          if (pointer == null || pointer.changedToUpIgnoreConsumed() || pointer.isConsumed) {
            item.offsetForScrollOuter.floatValue = Float.POSITIVE_INFINITY
            break
          }
          pointer.consume()
          item.offsetForScrollOuter.floatValue =
            pointer.position.y.coerceIn(0F, size.height.toFloat()) - scrollState.value
        }
      }
    })
  }

  @Composable
  private fun ItemGroupShowCompose(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    placeholderScheduleItemGroups.fastForEach {
      with(it) {
        ItemGroupContent(
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          scrollState = scrollState,
        )
      }
    }
  }

  private fun createPlaceholderScheduleItem(
    weekBeginDate: Date,
    columnIndex: Int,
    initialTime: Int,
  ): PlaceholderScheduleItemGroup {
    return PlaceholderScheduleItemGroup(
      weekBeginDate = weekBeginDate,
      columnIndex = columnIndex,
      initialTimeInt = initialTime,
      deleteCallback = {
        placeholderScheduleItemGroups.remove(it)
      }
    )
  }
}






