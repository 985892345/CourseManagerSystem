package com.course.pages.schedule.api.item.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.rememberDerivedStateOfStructure
import com.course.components.view.option.OptionSelectBackground
import com.course.components.view.option.OptionSelectCompose
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.item.BottomSheetScheduleItem
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/4/30 18:28
 */

@Composable
internal fun EditRepeatCompose(
  modifier: Modifier,
  item: BottomSheetScheduleItem,
  weekBeginDate: Date,
  timeline: CourseTimeline,
) {
  val frequencyLine = remember {
    Animatable(item.repeat.frequency.toFloat() - 1)
  }
  val frequencyLines = remember {
    List(100) { (it + 1).toString() }.toImmutableList()
  }
  val unitLine = remember {
    Animatable(
      when (item.repeat) {
        is ScheduleRepeat.Day -> 0F
        is ScheduleRepeat.Week -> 1F
        is ScheduleRepeat.Month -> 2F
      }
    )
  }
  val unitLines = remember {
    persistentListOf("天", "周", "月")
  }
  val countLine = remember {
    Animatable(item.repeat.count.toFloat() - 1)
  }
  val countLines = remember {
    List(100) { (it + 1).toString() }.toImmutableList()
  }
  val ddlDate = rememberDerivedStateOfStructure {
    val diff = (frequencyLine.value.roundToInt() + 1) * countLine.value.roundToInt()
    val date = when (unitLine.value.roundToInt()) {
      0 -> item.startTime.date.plusDays(diff)
      1 -> item.startTime.date.plusWeeks(diff)
      2 -> item.startTime.date.plusMonths(diff)
      else -> error("未知频率 unitLine=${unitLine.value}, unitLines=${unitLines}")
    }
    when (item.startTime.date.daysUntil(date)) {
      0 -> "${date}（今天）"
      1 -> "${date}（明天）"
      2 -> "${date}（后天）"
      else -> date.toString()
    }
  }
  Column(modifier = modifier) {
    Box(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        modifier = Modifier,
        text = "在 ${ddlDate.value}结束"
      )
    }
    Row(
      modifier = Modifier.height(100.dp).fillMaxWidth().padding(top = 16.dp),
      horizontalArrangement = Arrangement.Center,
    ) {
      OptionSelectBackground(
        modifier = Modifier.padding(end = 8.dp)
          .width(100.dp)
          .fillMaxHeight()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            modifier = Modifier.weight(1F),
            text = "每",
            textAlign = TextAlign.Center,
          )
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = frequencyLine,
            options = frequencyLines,
          ) {
            setItemRepeat(
              item = item,
              frequencyLine = frequencyLine.value.roundToInt(),
              unitLine = unitLine.value.roundToInt(),
              countLine = countLine.value.roundToInt(),
            )
          }
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = unitLine,
            options = unitLines,
          ) {
            setItemRepeat(
              item = item,
              frequencyLine = frequencyLine.value.roundToInt(),
              unitLine = unitLine.value.roundToInt(),
              countLine = countLine.value.roundToInt(),
            )
          }
        }
      }
      OptionSelectBackground(
        modifier = Modifier.padding(start = 16.dp)
          .width(100.dp)
          .fillMaxHeight()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            modifier = Modifier.weight(1F),
            text = "共",
            textAlign = TextAlign.Center,
          )
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = countLine,
            options = countLines,
          ) {
            setItemRepeat(
              item = item,
              frequencyLine = frequencyLine.value.roundToInt(),
              unitLine = unitLine.value.roundToInt(),
              countLine = countLine.value.roundToInt(),
            )
          }
          Text(
            modifier = Modifier.weight(1F),
            text = "次",
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

private fun setItemRepeat(
  item: BottomSheetScheduleItem,
  frequencyLine: Int,
  unitLine: Int,
  countLine: Int,
) {
  val frequency = frequencyLine + 1
  val count = countLine + 1
  item.changeRepeat(
    repeat = when (unitLine) {
      0 -> ScheduleRepeat.Day(frequency, count)
      1 -> ScheduleRepeat.Week(frequency, count)
      2 -> ScheduleRepeat.Month(frequency, count)
      else -> ScheduleRepeat.Once
    }
  )
}
