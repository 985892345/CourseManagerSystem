package com.course.pages.schedule.ui.item

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/5/5 16:10
 */
class ScheduleItemShow(
  val isShowTopBottomTime: State<Boolean>,
  val startTimeState: State<MinuteTimeDate>,
  val minuteDurationState: State<Int>,
  val getHeightOffset: (
    timeline: CourseTimeline,
    totalHeight: Int,
    scrollState: ScrollState
  ) -> Offset,
  val itemContent: @Composable ICourseItemGroup.(
    item: ShowItem,
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState
  ) -> Unit
) {

  private val items = mutableStateListOf<ShowItem>()

  /**
   * @param weekBeginDate 当前显示的周开始日期，用于决定是否启动动画
   */
  fun changeRepeat(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    newRepeat: ScheduleRepeat,
    startTime: MinuteTimeDate,
  ) {
    val repeatItems = items.drop(1)
    val initialColumnIndex = timeline.getItemWhichDate(startTime).dayOfWeekOrdinal
    var addedSize = 1
    for (item in repeatItems) {
      if (addedSize < newRepeat.count) {
        if (item.exist) {
          val date = newRepeat.getDate(startTime.date, addedSize)
          val newColumnIndex = (initialColumnIndex + startTime.date.daysUntil(date)) % 7
          item.repeatCurrent = addedSize
          item.timeDate = MinuteTimeDate(date, startTime.time)
          AppComposeCoroutineScope.launch {
            if (timeline.getItemWhichDate(item.timeDate).weekBeginDate == weekBeginDate) {
              item.columnIndexAnim.animateTo(newColumnIndex.toFloat())
            } else {
              item.columnIndexAnim.snapTo(newColumnIndex.toFloat())
            }
          }
          addedSize++
        }
      } else {
        if (timeline.getItemWhichDate(item.timeDate).weekBeginDate == weekBeginDate) {
          item.exist = false
          AppComposeCoroutineScope.launch {
            item.alphaAnim.animateTo(0F)
            items.remove(item)
          }
        } else {
          item.exist = false
          items.remove(item)
        }
      }
    }
    for (i in addedSize until newRepeat.count) {
      val date = newRepeat.getDate(startTime.date, i)
      val timeDate = MinuteTimeDate(date, startTime.time)
      val newColumnIndex = (initialColumnIndex + startTime.date.daysUntil(date)) % 7
      val newItem = ShowItem(
        initialColumnIndex = newColumnIndex,
        repeatCurrent = i,
        initialAlpha = if (timeline.getItemWhichDate(timeDate).weekBeginDate == weekBeginDate) 0F else 1F,
        timeDate = timeDate,
      )
      items.add(newItem)
      if (newItem.alphaAnim.value == 0F) {
        AppComposeCoroutineScope.launch {
          newItem.alphaAnim.animateTo(1F)
        }
      }
    }
  }

  suspend fun cancelShow() {
    supervisorScope {
      items.fastForEach {
        launch {
          it.alphaAnim.animateTo(0F)
        }
      }
    }
  }

  @Composable
  fun ICourseItemGroup.ShowContent(
    zIndex: Float,
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    if (items.isEmpty()) {
      // 添加默认的第一个 item
      items.add(
        ShowItem(
          initialColumnIndex = timeline.getItemWhichDate(startTimeState.value).dayOfWeekOrdinal,
          repeatCurrent = 0,
          timeDate = startTimeState.value,
        )
      )
    }
    items.fastForEach {
      if (timeline.getItemWhichDate(it.timeDate).weekBeginDate == weekBeginDate) {
        ItemContent(
          item = it,
          zIndex = zIndex,
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          scrollState = scrollState,
        )
      }
    }
  }

  @Composable
  private fun ICourseItemGroup.ItemContent(
    item: ShowItem,
    zIndex: Float,
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    Layout(
      modifier = Modifier.graphicsLayer {
        alpha = item.alphaAnim.value
      }.zIndex(zIndex),
      content = {
        itemContent(item, weekBeginDate, timeline, scrollState)
        if (isShowTopBottomTime.value) {
          TopTimeCompose()
          BottomTimeCompose()
        }
      },
      measurePolicy = remember(item) {
        { measurables, constraints ->
          val heightOffset = getHeightOffset(
            timeline,
            constraints.maxHeight,
            scrollState,
          )
          val itemPlaceable = measurables[0].measure(
            Constraints(
              maxWidth = constraints.maxWidth / 7,
              maxHeight = (constraints.maxHeight * heightOffset.y).roundToInt()
            )
          )
          val topTimePlaceable = measurables.getOrNull(1)?.measure(constraints)
          val bottomTimePlaceable = measurables.getOrNull(2)?.measure(constraints)
          layout(constraints.maxWidth, constraints.maxHeight) {
            if (itemPlaceable.height > 10) {
              val x = ((constraints.maxWidth / 7) * item.columnIndexAnim.value).roundToInt()
              val y = (constraints.maxHeight * heightOffset.x).roundToInt()
              itemPlaceable.place(x = x, y = y)
              if (topTimePlaceable != null && bottomTimePlaceable != null) {
                if (y < topTimePlaceable.height) {
                  topTimePlaceable.place(
                    x = x + (itemPlaceable.width - topTimePlaceable.width) / 2,
                    y = y + 2.dp.roundToPx(),
                  )
                } else {
                  topTimePlaceable.place(
                    x = x + (itemPlaceable.width - topTimePlaceable.width) / 2,
                    y = y - topTimePlaceable.height
                  )
                }
                val end = y + itemPlaceable.height
                if (end > constraints.maxHeight - bottomTimePlaceable.height) {
                  bottomTimePlaceable.place(
                    x = x + (itemPlaceable.width - bottomTimePlaceable.width) / 2,
                    y = end - bottomTimePlaceable.height - 2.dp.roundToPx()
                  )
                } else {
                  bottomTimePlaceable.place(
                    x = x + (itemPlaceable.width - bottomTimePlaceable.width) / 2,
                    y = end
                  )
                }
              }
            }
          }
        }
      }
    )
  }

  @Composable
  private fun TopTimeCompose() {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = startTimeState.value.time.toString(),
      textAlign = TextAlign.Center,
      fontSize = 10.sp,
    )
  }

  @Composable
  private fun BottomTimeCompose() {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = startTimeState.value.time.plusMinutes(minuteDurationState.value).toString(),
      textAlign = TextAlign.Center,
      fontSize = 10.sp,
    )
  }

  @Stable
  class ShowItem(
    initialColumnIndex: Int,
    var repeatCurrent: Int,
    var timeDate: MinuteTimeDate,
    initialAlpha: Float = 1F,
    var exist: Boolean = true,
  ) {
    val columnIndexAnim = Animatable(initialColumnIndex.toFloat())
    val alphaAnim = Animatable(initialAlpha)
  }
}