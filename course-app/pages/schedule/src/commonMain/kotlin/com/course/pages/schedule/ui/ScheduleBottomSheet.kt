package com.course.pages.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.showBottomSheetWindow
import com.course.components.view.edit.EditTextCompose
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.ui.edit.EditRepeatCompose
import com.course.pages.schedule.ui.edit.EditTimeCompose
import com.course.pages.schedule.ui.item.BottomSheetScheduleItem
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleRepeat

/**
 * .
 *
 * @author 985892345
 * 2024/4/26 15:51
 */

fun showAddAffairBottomSheet(
  item: BottomSheetScheduleItem,
  repeatCurrent: Int,
  weekBeginDate: Date,
  timeline: CourseTimeline,
) {
  val type = mutableStateOf(AddAffairBottomSheetState.Description)
  showBottomSheetWindow(
    dismissOnBackPress = { item.dismissOnBackPress(it) },
    dismissOnClickOutside = { item.dismissOnClickOutside(it) },
  ) { dismiss ->
    Card(
      modifier = Modifier.fillMaxWidth().imePadding(),
      shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
    ) {
      Column(
        modifier = Modifier.padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          EditTextCompose(
            modifier = Modifier.weight(1F),
            text = item.title,
            hint = "请输入标题",
            singleLine = true,
            isShowIndicatorLine = false,
            textStyle = TextStyle(
              fontSize = 22.sp,
              color = LocalAppColors.current.tvLv2,
              fontWeight = FontWeight.Bold,
            ),
          )
          Box(modifier = Modifier.padding(horizontal = 8.dp).size(32.dp).clickableCardIndicator {
            item.delete(dismiss)
          }, contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Rounded.DeleteOutline,
              contentDescription = null,
            )
          }
          Box(modifier = Modifier.size(32.dp).clickableCardIndicator {
            when (type.value) {
              AddAffairBottomSheetState.Description -> {
                item.success(dismiss)
              }

              AddAffairBottomSheetState.EditTime, AddAffairBottomSheetState.EditRepeat -> {
                type.value = AddAffairBottomSheetState.Description
              }
            }
          }, contentAlignment = Alignment.Center) {
            Icon(
              imageVector = when (type.value) {
                AddAffairBottomSheetState.Description -> Icons.Rounded.Check
                AddAffairBottomSheetState.EditTime -> Icons.AutoMirrored.Rounded.ArrowBack
                AddAffairBottomSheetState.EditRepeat -> Icons.AutoMirrored.Rounded.ArrowBack
              },
              contentDescription = null,
            )
          }
        }
        Row(
          modifier = Modifier.padding(top = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TimeTextCompose(item) {
            type.value = AddAffairBottomSheetState.EditTime
          }
          RepeatTextCompose(
            item = item,
            repeatCurrent = repeatCurrent,
            modifier = Modifier.padding(start = 12.dp)
          ) {
            type.value = AddAffairBottomSheetState.EditRepeat
          }
        }
        type.value.Content(
          modifier = Modifier.fillMaxWidth().height(160.dp),
          item = item,
          weekBeginDate = weekBeginDate,
          timeline = timeline,
        )
      }
    }
  }
}

@Composable
private fun TimeTextCompose(
  item: BottomSheetScheduleItem,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      modifier = Modifier.size(16.dp),
      imageVector = Icons.Rounded.Schedule,
      contentDescription = null,
    )
    Row(
      modifier = Modifier.clickable {
        onClick.invoke()
      },
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        modifier = Modifier.padding(start = 2.dp),
        text = item.startTime.time.toString(),
        fontSize = 13.sp,
        color = LocalAppColors.current.tvLv2,
      )
      Spacer(
        modifier = Modifier.padding(horizontal = 4.dp)
          .size(6.dp, 1.dp).background(color = Color.Black)
      )
      Text(
        modifier = Modifier,
        text = item.startTime.time.plusMinutes(item.minuteDuration).toString(),
        fontSize = 13.sp,
        color = LocalAppColors.current.tvLv2,
      )
    }
  }
}

@Composable
private fun RepeatTextCompose(
  item: BottomSheetScheduleItem,
  repeatCurrent: Int,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      modifier = Modifier.size(16.dp),
      imageVector = Icons.Rounded.Repeat,
      contentDescription = null,
    )
    val fraction = "（${repeatCurrent + 1}/${item.repeat.count}）"
    Text(
      modifier = Modifier.clickable {
        onClick.invoke()
      },
      text = when (val repeat = item.repeat) {
        is ScheduleRepeat.Day -> if (repeat.count == 1) "一次性" else {
          if (repeat.frequency == 1) "每天${fraction}" else {
            "每${repeat.frequency}天${fraction}"
          }
        }

        is ScheduleRepeat.Week -> if (repeat.count == 1) "一次性" else {
          if (repeat.frequency == 1) "每周${fraction}" else {
            "每${repeat.frequency}周${fraction}"
          }
        }

        is ScheduleRepeat.Month -> if (repeat.count == 1) "一次性" else {
          if (repeat.frequency == 1) "每月${fraction}" else {
            "每${repeat.frequency}月${fraction}"
          }
        }

        else -> ""
      },
      fontSize = 13.sp,
      color = LocalAppColors.current.tvLv2,
    )
  }
}

enum class AddAffairBottomSheetState {
  Description {
    @Composable
    override fun Content(
      modifier: Modifier,
      item: BottomSheetScheduleItem,
      weekBeginDate: Date,
      timeline: CourseTimeline
    ) {
      EditTextCompose(
        modifier = modifier.padding(top = 8.dp).fillMaxSize(),
        text = item.description,
        hint = "描述",
        isShowIndicatorLine = false,
        textStyle = TextStyle(
          fontSize = 13.sp,
          color = LocalAppColors.current.tvLv2,
        ),
      )
    }
  },
  EditTime {
    @Composable
    override fun Content(
      modifier: Modifier,
      item: BottomSheetScheduleItem,
      weekBeginDate: Date,
      timeline: CourseTimeline
    ) {
      EditTimeCompose(
        modifier = modifier,
        item = item,
        timeline = timeline,
      )
    }
  },
  EditRepeat {
    @Composable
    override fun Content(
      modifier: Modifier,
      item: BottomSheetScheduleItem,
      weekBeginDate: Date,
      timeline: CourseTimeline
    ) {
      EditRepeatCompose(
        modifier = modifier,
        item = item,
        weekBeginDate = weekBeginDate,
        timeline = timeline,
      )
    }
  };

  @Composable
  abstract fun Content(
    modifier: Modifier,
    item: BottomSheetScheduleItem,
    weekBeginDate: Date,
    timeline: CourseTimeline
  )
}
