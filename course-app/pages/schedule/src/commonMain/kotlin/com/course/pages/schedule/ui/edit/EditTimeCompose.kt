package com.course.pages.schedule.ui.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.Wrapper
import com.course.components.utils.compose.getValue
import com.course.components.utils.compose.setValue
import com.course.components.view.edit.EditTextCompose
import com.course.components.view.option.OptionScrollCompose
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.ui.item.BottomSheetScheduleItem
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/4/30 11:19
 */
@Composable
fun EditTimeCompose(
  modifier: Modifier,
  item: BottomSheetScheduleItem,
  timeline: CourseTimeline,
) {
  val coroutineScope = rememberCoroutineScope()
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val beginTimeIntSetter = remember {
      val beginTimeInt = item.startTime.time.minuteOfDay +
          if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
      mutableIntStateOf(beginTimeInt)
    }
    val finalTimeIntSetter = remember {
      val beginTimeInt = item.startTime.time.minuteOfDay +
          if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
      mutableIntStateOf(beginTimeInt + item.minuteDuration)
    }
    TimeSelectCompose(
      modifier = Modifier,
      item = item,
      isBegin = true,
      timeline = timeline,
      timeIntSetter = beginTimeIntSetter,
    ) { it, restore ->
      val oldStartTime = item.startTime
      val oldTimeIsTomorrow = oldStartTime.time < timeline.delayMinuteTime
      val newTimeIsTomorrow = it >= 24 * 60
      val startTime = MinuteTimeDate(
        if (oldTimeIsTomorrow == newTimeIsTomorrow) {
          oldStartTime.date
        } else if (oldTimeIsTomorrow) {
          oldStartTime.date.minusDays(1)
        } else {
          oldStartTime.date.plusDays(1)
        },
        MinuteTime(it / 60 % 24, it % 60)
      )
      if (item.checkBeginFinalTime(startTime = startTime)) {
        coroutineScope.launch {
          item.changeBeginFinalTime(startTime = startTime)
        }
      } else {
        restore.invoke()
      }
    }
    DurationCompose(
      modifier = Modifier.weight(1F).padding(horizontal = 4.dp),
      item = item,
      timeline = timeline,
    ) {
      val beginTimeInt = item.startTime.time.minuteOfDay +
          if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
      if (item.checkBeginFinalTime(minuteDuration = it - beginTimeInt)) {
        finalTimeIntSetter.intValue = it
        coroutineScope.launch {
          item.changeBeginFinalTime(minuteDuration = it - beginTimeInt)
        }
      }
    }
    TimeSelectCompose(
      modifier = Modifier,
      item = item,
      isBegin = false,
      timeline = timeline,
      timeIntSetter = finalTimeIntSetter,
    ) { it, restore ->
      val beginTimeInt = item.startTime.time.minuteOfDay +
          if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
      if (item.checkBeginFinalTime(minuteDuration = it - beginTimeInt)) {
        coroutineScope.launch {
          item.changeBeginFinalTime(minuteDuration = it - beginTimeInt)
        }
      } else {
        restore.invoke()
      }
    }
  }
}

@Composable
private fun DurationCompose(
  modifier: Modifier,
  item: BottomSheetScheduleItem,
  timeline: CourseTimeline,
  onFinalTimeIntChanged: (Int) -> Unit,
) {
  val hourDurationText = remember { mutableStateOf("0") }
  val minuteDurationText = remember { mutableStateOf("0") }
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        EditTextCompose(
          modifier = Modifier.width(IntrinsicSize.Min),
          text = hourDurationText,
          isShowIndicatorLine = false,
          singleLine = true,
          keyboardType = KeyboardType.Number,
          textStyle = TextStyle(
            fontSize = 12.sp,
          ),
          onValueChange = {
            val hour = it.toIntOrNull()
            if (hour != null && hour >= 0) {
              val duration = hour * 60 + minuteDurationText.value.toInt()
              val beginTimeInt = item.startTime.time.minuteOfDay +
                  if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
              val finalTimeIneTargetValue = beginTimeInt + duration
              if (finalTimeIneTargetValue <= timeline.data.last().endTimeInt) {
                onFinalTimeIntChanged.invoke(finalTimeIneTargetValue)
              } else {
                toast("超出范围")
              }
            } else {
              if (it.isEmpty()) {
                hourDurationText.value = it
              }
            }
          }
        )
        Text(
          modifier = Modifier.padding(start = 2.dp),
          text = "小时",
          fontSize = 12.sp,
        )
      }
      Spacer(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(Color.Black))
      Row(verticalAlignment = Alignment.CenterVertically) {
        EditTextCompose(
          modifier = Modifier.width(IntrinsicSize.Min),
          text = minuteDurationText,
          isShowIndicatorLine = false,
          singleLine = true,
          keyboardType = KeyboardType.Number,
          textStyle = TextStyle(
            fontSize = 12.sp,
          ),
          onValueChange = {
            val minute = it.toIntOrNull()
            if (minute != null && minute >= 0) {
              val duration = hourDurationText.value.toInt() * 60 + minute
              val beginTimeInt = item.startTime.time.minuteOfDay +
                  if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
              val finalTimeIneTargetValue = beginTimeInt + duration
              if (finalTimeIneTargetValue <= timeline.data.last().endTimeInt) {
                onFinalTimeIntChanged.invoke(finalTimeIneTargetValue)
              } else {
                toast("超出范围")
              }
            } else {
              if (it.isEmpty()) {
                minuteDurationText.value = it
              }
            }
          }
        )
        Text(
          modifier = Modifier.padding(start = 2.dp),
          text = "分钟",
          fontSize = 12.sp,
        )
      }
    }
  }
  LaunchedEffect(item) {
    snapshotFlow {
      item.minuteDuration / 60
    }.onEach {
      hourDurationText.value = it.toString()
    }.launchIn(this)
    snapshotFlow {
      item.minuteDuration % 60
    }.onEach {
      minuteDurationText.value = it.toString()
    }.launchIn(this)
  }
}

private val SelectTextStyle = TextStyle(
  fontSize = 12.sp,
  textAlign = TextAlign.Center,
  color = Color.Black,
)

@Composable
private fun TimeSelectCompose(
  modifier: Modifier,
  item: BottomSheetScheduleItem,
  isBegin: Boolean,
  timeline: CourseTimeline,
  timeIntSetter: IntState,
  onDraggedStopped: (Int, restore: () -> Unit) -> Unit,
) {
  val hourLines = remember(timeline) {
    if (timeline.delayMinuteTime.hour == 0) {
      (0..24).map { it.toString().padStart(2, '0') }.toImmutableList()
    } else {
      ((timeline.delayMinuteTime.hour..24)
        .toMutableList().map { it.toString().padStart(2, '0') }
          + (1..timeline.delayMinuteTime.hour)
        .map { "24+${it}" }).toImmutableList()
    }
  }
  val minuteLines = remember {
    (0..59).map { it.toString().padStart(2, '0') }.toImmutableList()
  }
  val hourLine = remember(timeline) {
    val beginTimeInt = item.startTime.time.minuteOfDay +
        if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
    val timeInt = if (isBegin) beginTimeInt else beginTimeInt + item.minuteDuration
    val value = timeInt / 60 - timeline.delayMinuteTime.hour
    Animatable(value.toFloat())
  }
  val minuteLine = remember(timeline) {
    val beginTimeInt = item.startTime.time.minuteOfDay +
        if (item.startTime.time >= timeline.delayMinuteTime) 0 else 24 * 60
    val timeInt = if (isBegin) beginTimeInt else beginTimeInt + item.minuteDuration
    val value = timeInt % 60 - timeline.delayMinuteTime.minute
    Animatable(value.toFloat())
  }
  val timelineOptions = remember(timeline) {
    timeline.data.map {
      it.optionText
    }.toImmutableList()
  }
  val selectedTimeline = remember(timeline) {
    Animatable(
      getSelectedTimeline(
        timeline = timeline,
        isBegin = isBegin,
        timeInt = getTimeInt(
          hourLine = hourLine.value,
          hourLines = hourLines,
          minuteLine = minuteLine.value,
          minuteLines = minuteLines,
        )
      )
    )
  }
  val coroutineScope = rememberCoroutineScope()
  fun dragLine(hourTargetValue: Float, minuteTargetValue: Float) {
    coroutineScope.launch {
      launch { hourLine.animateTo(targetValue = hourTargetValue) }
      launch { minuteLine.animateTo(targetValue = minuteTargetValue) }
      launch {
        selectedTimeline.animateTo(
          getSelectedTimeline(
            timeline = timeline,
            isBegin = isBegin,
            timeInt = getTimeInt(
              hourLine = hourTargetValue,
              hourLines = hourLines,
              minuteLine = minuteTargetValue,
              minuteLines = minuteLines,
            )
          )
        )
      }
    }
  }
  TimeSelectBackground(modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {

      var startDragHourLine by remember { Wrapper(hourLine.value) }
      var startDragMinuteLine by remember { Wrapper(minuteLine.value) }

      val dragRestore = remember { { dragLine(startDragHourLine, startDragMinuteLine) } }

      @Composable
      fun TimelineOption() {
        OptionScrollCompose(
          modifier = Modifier.size(50.dp, 120.dp),
          selectedLine = selectedTimeline,
          options = timelineOptions,
          selectedTextSizeRatio = 1.2F,
          textStyle = SelectTextStyle,
          onDragStart = {
            startDragHourLine = hourLine.value
            startDragMinuteLine = minuteLine.value
          },
          onDraggedStopped = {
            val hourTargetValue = (timeline.data[selectedTimeline.value.roundToInt()].let {
              if (isBegin) it.startTimeInt else it.endTimeInt
            } / 60 - timeline.delayMinuteTime.hour).toFloat()
            val minuteTargetValue = ((timeline.data[selectedTimeline.value.roundToInt()].let {
              if (isBegin) it.startTime else it.endTime
            }.minute - timeline.delayMinuteTime.minute).toFloat())
            coroutineScope.launch {
              // 由具体的时间轴事件滚动到对应时间
              launch { hourLine.animateTo(targetValue = hourTargetValue) }
              launch { minuteLine.animateTo(targetValue = minuteTargetValue) }
            }
            onDraggedStopped.invoke(
              getTimeInt(
                hourLine = hourTargetValue,
                hourLines = hourLines,
                minuteLine = minuteTargetValue,
                minuteLines = minuteLines,
              ),
              dragRestore,
            )
          }
        )
      }

      @Composable
      fun TimeOption(line: Animatable<Float, *>, lines: ImmutableList<String>) {
        OptionScrollCompose(
          modifier = Modifier.size(40.dp, 120.dp),
          selectedLine = line,
          options = lines,
          selectedTextSizeRatio = 1.2F,
          textStyle = SelectTextStyle,
          onDragStart = {
            startDragHourLine = hourLine.value
            startDragMinuteLine = minuteLine.value
          },
          onDraggedStopped = {
            val timeInt = getTimeInt(
              hourLine = hourLine.value,
              hourLines = hourLines,
              minuteLine = minuteLine.value,
              minuteLines = minuteLines,
            )
            onDraggedStopped.invoke(timeInt, dragRestore)
            coroutineScope.launch {
              selectedTimeline.animateTo(
                getSelectedTimeline(
                  timeline = timeline,
                  isBegin = isBegin,
                  timeInt = timeInt,
                )
              )
            }
          },
        )
      }
      if (isBegin) {
        TimelineOption()
      }
      TimeOption(line = hourLine, lines = hourLines)
      Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = ":",
        fontSize = 16.sp,
      )
      TimeOption(line = minuteLine, lines = minuteLines)
      if (!isBegin) {
        TimelineOption()
      }
    }
  }
  LaunchedEffect(timeline, timeIntSetter, hourLine, hourLines, minuteLine, minuteLines) {
    snapshotFlow { timeIntSetter.value }.onEach {
      val hourTargetValue = (it / 60 - timeline.delayMinuteTime.hour).toFloat()
      val minuteTargetValue = (it % 60 - timeline.delayMinuteTime.minute).toFloat()
      dragLine(hourTargetValue, minuteTargetValue)
    }.launchIn(this)
  }
}

@Composable
private fun TimeSelectBackground(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Card(modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min)) {
    content()
    Column(modifier = Modifier.fillMaxSize()) {
      Spacer(
        modifier = Modifier.weight(1F).fillMaxWidth().background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.05F),
              Color.Transparent,
            )
          )
        )
      )
      Spacer(modifier = Modifier.weight(1F))
      Spacer(
        modifier = Modifier.weight(1F).fillMaxWidth().background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color.Black.copy(alpha = 0.05F),
            )
          )
        )
      )
    }
  }
}

private fun getTimeInt(
  hourLine: Float,
  hourLines: ImmutableList<String>,
  minuteLine: Float,
  minuteLines: ImmutableList<String>,
): Int {
  val hourStr = hourLines[hourLine.roundToInt()]
  val hour = if (hourStr.contains("+")) {
    hourStr.substringAfter("+").toInt() + 24
  } else hourStr.toInt()
  return hour * 60 + minuteLines[minuteLine.roundToInt()].toInt()
}

private fun getSelectedTimeline(
  timeline: CourseTimeline,
  timeInt: Int,
  isBegin: Boolean,
): Float {
  if (isBegin) {
    if (timeInt >= timeline.data.last().startTimeInt) return timeline.data.size - 1F
  } else {
    if (timeInt <= timeline.data.first().endTimeInt) return 0F
  }
  timeline.data.forEachIndexed { index, data ->
    if (isBegin) {
      if (timeInt in data.startTimeInt..<data.endTimeInt) {
        return index.toFloat()
      }
    } else {
      if (timeInt in data.startTimeInt + 1..data.endTimeInt) {
        return index.toFloat()
      }
    }
  }
  return if (isBegin) timeline.data.size - 1F else 0F
}