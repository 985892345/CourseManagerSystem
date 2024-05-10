package com.course.pages.schedule.ui.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.serializable.ColorArgbSerializable
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.item.BottomSheetScheduleItem
import com.course.pages.schedule.api.item.edit.ScheduleColorData
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/5/5 17:24
 */
class PlaceholderScheduleItemGroup(
  colorData: ScheduleColorData,
  weekBeginDate: Date,
  columnIndex: Int,
  val initialTimeInt: Int,
  val deleteCallback: (PlaceholderScheduleItemGroup) -> Unit,
  val successCallback: suspend (ScheduleBean) -> Unit,
  val onClick: (
    item: BottomSheetScheduleItem,
    repeatCurrent: Int,
    weekBeginDate: Date,
    timeline: CourseTimeline,
  ) -> Unit,
) : BottomSheetScheduleItem {
  override val id: Int
    get() = 0

  override val title: MutableState<String> = mutableStateOf("")
  override val description: MutableState<String> = mutableStateOf("")

  override var repeat: ScheduleRepeat by mutableStateOf(ScheduleRepeat.Once)

  private val beginTimeIntState = mutableIntStateOf(initialTimeInt)
  private val finalTimeIntState = mutableIntStateOf(initialTimeInt)

  private val startTimeState = derivedStateOfStructure {
    MinuteTimeDate(
      if (beginTimeIntState.intValue < 24 * 60) {
        weekBeginDate.plusDays(columnIndex)
      } else {
        weekBeginDate.plusDays(columnIndex + 1)
      },
      MinuteTime(
        beginTimeIntState.intValue / 60 % 24,
        beginTimeIntState.intValue % 60
      )
    )
  }
  private val minuteDurationState = derivedStateOfStructure {
    finalTimeIntState.intValue - beginTimeIntState.intValue
  }

  override val startTime: MinuteTimeDate
    get() = startTimeState.value

  override val minuteDuration: Int
    get() = minuteDurationState.value

  override var textColor: Color by mutableStateOf(colorData.textColor)
  override var backgroundColor: Color by mutableStateOf(colorData.backgroundColor)

  // 相对于 scroll 外高度的偏移量，在长按移动时设置
  // 并且在手指抬起时需要设置成 Float.POSITIVE_INFINITY 表示结束
  val offsetForScrollOuter = mutableFloatStateOf(Float.POSITIVE_INFINITY)

  private val itemShow = ScheduleItemShow(
    isShowTopBottomTime = derivedStateOfStructure {
      offsetForScrollOuter.floatValue != Float.POSITIVE_INFINITY
    },
    startTimeState = startTimeState,
    minuteDurationState = minuteDurationState,
    getHeightOffset = { timeline, totalHeight, scrollState ->
      getHeightOffset(
        totalHeight = totalHeight,
        timeline = timeline,
        scrollState = scrollState,
      )
    }
  ) { item, weekBeginDate, timeline, scrollState ->
    ItemContent(
      item = item,
      weekBeginDate = weekBeginDate,
      timeline = timeline,
    )
  }

  private lateinit var heightOffsetAnim: Animatable<Offset, *>
  private var oldWeekBeginDate: Date? = null
  private lateinit var oldTimeline: CourseTimeline

  private fun getHeightOffset(
    totalHeight: Int,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ): Offset {
    return if (offsetForScrollOuter.floatValue != Float.POSITIVE_INFINITY) {
      val nowTimeInt = getMinuteTimeByOffset(
        timeline,
        totalHeight,
        offsetForScrollOuter.floatValue + scrollState.value
      )
      if (nowTimeInt > initialTimeInt) {
        beginTimeIntState.intValue = initialTimeInt
        finalTimeIntState.intValue = nowTimeInt
        ICourseItemGroup.calculateItemHeightOffset0(
          timeline = timeline,
          beginTimeInt = initialTimeInt,
          finalTimeInt = nowTimeInt,
        )
      } else {
        beginTimeIntState.intValue = nowTimeInt
        finalTimeIntState.intValue = initialTimeInt
        ICourseItemGroup.calculateItemHeightOffset0(
          timeline = timeline,
          beginTimeInt = nowTimeInt,
          finalTimeInt = initialTimeInt,
        )
      }
    } else if (this::heightOffsetAnim.isInitialized && heightOffsetAnim.isRunning) {
      heightOffsetAnim.value
    } else {
      // 在不执行动画时会走该分支，从而自动观察 timeline 的变化
      ICourseItemGroup.calculateItemHeightOffset0(
        timeline = timeline,
        beginTimeInt = beginTimeIntState.intValue,
        finalTimeInt = finalTimeIntState.intValue,
      )
    }
  }

  override fun checkBeginFinalTime(startTime: MinuteTimeDate, minuteDuration: Int): Boolean {
    if (minuteDuration < 5) {
      toast("不能小于5分钟")
      return false
    }
    return true
  }

  override suspend fun changeBeginFinalTime(
    startTime: MinuteTimeDate,
    minuteDuration: Int
  ) {
    oldWeekBeginDate ?: return
    val beginTimeInt = startTime.time.minuteOfDay +
        if (startTime.time >= oldTimeline.delayMinuteTime) 0 else 24 * 60
    val initialValue = ICourseItemGroup.calculateItemHeightOffset0(
      timeline = oldTimeline,
      beginTimeInt = beginTimeIntState.intValue,
      finalTimeInt = finalTimeIntState.intValue,
    )
    if (!this::heightOffsetAnim.isInitialized) {
      heightOffsetAnim = Animatable(
        typeConverter = Offset.VectorConverter,
        initialValue = initialValue,
      )
    } else {
      // 这里需要重置动画的开始值
      heightOffsetAnim.snapTo(initialValue)
    }
    beginTimeIntState.intValue = beginTimeInt
    finalTimeIntState.intValue = beginTimeInt + minuteDuration
    heightOffsetAnim.animateTo(
      targetValue = ICourseItemGroup.calculateItemHeightOffset0(
        timeline = oldTimeline,
        beginTimeInt = beginTimeIntState.intValue,
        finalTimeInt = finalTimeIntState.intValue,
      ),
    )
  }

  override fun changeRepeat(repeat: ScheduleRepeat) {
    val weekBeginDate = oldWeekBeginDate ?: return
    this.repeat = repeat
    itemShow.changeRepeat(
      weekBeginDate = weekBeginDate,
      timeline = oldTimeline,
      newRepeat = repeat,
      startTime = startTime,
    )
  }

  override fun success(coroutineScope: CoroutineScope, dismiss: () -> Unit) {
    if (title.value.isBlank()) {
      toast("标题不能为空")
      return
    }
    if (finalTimeIntState.intValue - beginTimeIntState.intValue < 5) {
      toast("日程长度不能小于 5 分钟")
      return
    }
    coroutineScope.launch {
      successCallback.invoke(
        ScheduleBean(
          id = 0,
          title = title.value,
          description = description.value,
          startTime = startTime,
          minuteDuration = minuteDuration,
          repeat = repeat,
          textColor = ColorArgbSerializable.colorToArgbStr(textColor),
          backgroundColor = ColorArgbSerializable.colorToArgbStr(backgroundColor),
        )
      )
      deleteCallback.invoke(this@PlaceholderScheduleItemGroup)
      dismiss.invoke()
    }
  }

  override fun delete(coroutineScope: CoroutineScope, dismiss: () -> Unit) {
    coroutineScope.launch {
      cancelShow()
      deleteCallback.invoke(this@PlaceholderScheduleItemGroup)
      dismiss.invoke()
    }
  }

  override fun dismissOnBackPress(dismiss: () -> Unit): Boolean {
    return true
  }

  override fun dismissOnClickOutside(dismiss: () -> Unit): Boolean {
    return true
  }

  suspend fun cancelShow() {
    itemShow.cancelShow()
  }

  @Composable
  fun ICourseItemGroup.ItemGroupContent(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    oldWeekBeginDate = weekBeginDate
    oldTimeline = timeline
    with(itemShow) {
      ShowContent(
        zIndex = 99999F, // 显示在其他内容之上
        weekBeginDate = weekBeginDate,
        timeline = timeline,
        scrollState = scrollState,
      )
    }
  }

  @Composable
  private fun ICourseItemGroup.ItemContent(
    item: ScheduleItemShow.ShowItem,
    weekBeginDate: Date,
    timeline: CourseTimeline,
  ) {
    CardContent(
      backgroundColor = backgroundColor,
      modifier = Modifier.fillMaxSize()
    ) {
      Box(modifier = Modifier.fillMaxSize().clickable {
        onClick(
          this@PlaceholderScheduleItemGroup,
          item.repeatCurrent,
          weekBeginDate,
          timeline,
        )
      }) {
        AnimatedVisibility(
          visible = title.value.isNotEmpty(),
        ) {
          TopBottomText(
            top = title.value,
            topColor = textColor,
            bottom = description.value,
            bottomColor = textColor,
          )
        }
        AnimatedVisibility(
          visible = title.value.isEmpty(),
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = textColor,
            )
          }
        }
      }
    }
  }


  companion object {

    // 根据 offset 计算出时间，结果可能会大于 24 * 60
    fun getMinuteTimeByOffset(
      timeline: CourseTimeline,
      totalHeight: Int,
      offset: Float,
    ): Int {
      var totalWeight = 0F
      timeline.data.fastForEach { totalWeight += it.nowWeight }
      val weightHeight = totalHeight / totalWeight
      var accHeight = 0F
      timeline.data.fastForEach { data ->
        val height = data.nowWeight * weightHeight
        if (offset in accHeight..accHeight + height) {
          val minuteUntil = data.startTime.minutesUntil(data.endTime, true)
          val minuteDiff = ((offset - accHeight) / height * minuteUntil).roundToInt()
          return data.startTime.plusMinutes(minuteDiff).let {
            it.hour * 60 + it.minute +
                if (minuteDiff != 0 && it <= timeline.delayMinuteTime) 24 * 60 else 0
          }
        }
        accHeight += height
      }
      return if (offset <= 0F) timeline.data.first().startTimeInt else timeline.data.last().endTimeInt
    }
  }
}