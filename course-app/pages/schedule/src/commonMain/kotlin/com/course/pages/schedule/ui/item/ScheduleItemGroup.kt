package com.course.pages.schedule.ui.item

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.model.ScheduleRepository
import com.course.pages.schedule.ui.showAddAffairBottomSheet
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/5 15:51
 */
class ScheduleItemGroup(
  var bean: ScheduleBean,
) : BottomSheetScheduleItem {

  override val title: MutableState<String> = mutableStateOf(bean.title)
  override val description: MutableState<String> = mutableStateOf(bean.description)

  private val startTimeState = mutableStateOf(bean.startTime)
  private val minuteDurationState = mutableIntStateOf(bean.minuteDuration)

  override val startTime: MinuteTimeDate
    get() = startTimeState.value
  override val minuteDuration: Int
    get() = minuteDurationState.value

  override var repeat: ScheduleRepeat by mutableStateOf(bean.repeat)

  private val itemShow = ScheduleItemShow(
    isShowTopBottomTime = mutableStateOf(false),
    startTimeState = startTimeState,
    minuteDurationState = minuteDurationState,
    getHeightOffset = { timeline, _, _ ->
      if (!this::heightOffsetAnim.isInitialized) {
        heightOffsetAnim = Animatable(
          typeConverter = Offset.VectorConverter,
          initialValue = ICourseItemGroup.calculateItemHeightOffset(
            timeline = timeline,
            startTime = startTime.time,
            minuteDuration = minuteDuration,
          )
        )
      }
      heightOffsetAnim.value
    },
  ) { item, weekBeginDate, timeline, _ ->
    ItemContent(
      item = item,
      weekBeginDate = weekBeginDate,
      timeline = timeline,
    )
  }

  private lateinit var heightOffsetAnim: Animatable<Offset, *>
  private var oldWeekBeginDate: Date? = null
  private lateinit var oldTimeline: CourseTimeline

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
    startTimeState.value = startTime
    minuteDurationState.intValue = minuteDuration
    heightOffsetAnim.animateTo(
      targetValue = ICourseItemGroup.calculateItemHeightOffset(
        timeline = oldTimeline,
        startTime = startTime.time,
        minuteDuration = minuteDuration,
      )
    )
  }

  override fun changeRepeat(
    repeat: ScheduleRepeat
  ) {
    val weekBeginDate = oldWeekBeginDate ?: return
    this.repeat = repeat
    itemShow.changeRepeat(
      weekBeginDate = weekBeginDate,
      timeline = oldTimeline,
      newRepeat = repeat,
      startTime = startTime,
    )
  }

  fun changeBean(newBean: ScheduleBean) {
    bean = newBean
    title.value = newBean.title
    description.value = newBean.description
    startTimeState.value = newBean.startTime
    minuteDurationState.intValue = newBean.minuteDuration
    repeat = newBean.repeat
    if (oldWeekBeginDate?.let { it.daysUntil(newBean.startTime.date) < 7 } == true) {
      // 正在当前页面显示
      AppComposeCoroutineScope.launch {
        changeBeginFinalTime(
          startTime = newBean.startTime,
          minuteDuration = newBean.minuteDuration,
        )
      }
      AppComposeCoroutineScope.launch {
        changeRepeat(newBean.repeat)
      }
    }
  }

  override fun success(dismiss: () -> Unit) {
    bean = bean.copy(
      title = title.value,
      description = description.value,
      startTime = startTime,
      minuteDuration = minuteDuration,
      repeat = repeat,
    )
    ScheduleRepository.updateSchedule(bean)
    dismiss.invoke()
  }

  override fun delete(dismiss: () -> Unit) {
    AppComposeCoroutineScope.launch {
      dismiss.invoke()
      itemShow.cancelShow()
      ScheduleRepository.removeSchedule(bean.id)
    }
  }

  override fun dismissOnBackPress(dismiss: () -> Unit): Boolean {
    return dismissOnClickOutside(dismiss)
  }

  override fun dismissOnClickOutside(dismiss: () -> Unit): Boolean {
    if (title.value != bean.title ||
      description.value != bean.description ||
      startTime != bean.startTime ||
      minuteDuration != bean.minuteDuration ||
      repeat != bean.repeat
    ) {
      showChooseDialog(
        onClickPositionBtn = {
          dismiss.invoke()
          changeBean(bean)
          hide()
        }
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "放弃编辑此日程吗?"
          )
        }
      }
      return false
    }
    return true
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
      backgroundColor = Color.LightGray,
      modifier = Modifier.fillMaxSize(),
    ) {
      TopBottomText(
        modifier = Modifier.clickable {
          showAddAffairBottomSheet(
            item = this@ScheduleItemGroup,
            repeatCurrent = item.repeatCurrent,
            timeline = timeline,
            weekBeginDate = weekBeginDate,
          )
        },
        top = title.value,
        topColor = Color.Black,
        bottom = description.value,
        bottomColor = Color.Black,
      )
    }
  }
}