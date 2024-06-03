package com.course.pages.schedule.api.item

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.course.pages.schedule.api.item.AddScheduleBottomSheetState.Description
import com.course.pages.schedule.api.item.AddScheduleBottomSheetState.EditColor
import com.course.pages.schedule.api.item.AddScheduleBottomSheetState.EditRepeat
import com.course.pages.schedule.api.item.AddScheduleBottomSheetState.EditTime
import com.course.pages.schedule.api.item.edit.EditColorCompose
import com.course.pages.schedule.api.item.edit.EditRepeatCompose
import com.course.pages.schedule.api.item.edit.EditTimeCompose
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleRepeat

/**
 * .
 *
 * @author 985892345
 * 2024/4/26 15:51
 */

fun showAddScheduleBottomSheet(
  item: BottomSheetScheduleItem,
  repeatCurrent: Int,
  weekBeginDate: Date,
  timeline: CourseTimeline,
  bottomContent: (@Composable () -> Unit)? = null,
) {
  val type = mutableStateOf(Description)
  showBottomSheetWindow(
    dismissOnBackPress = { item.dismissOnBackPress(it) },
    dismissOnClickOutside = { item.dismissOnClickOutside(it) },
  ) { dismiss ->
    Card(
      modifier = Modifier.fillMaxWidth().imePadding(),
      shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
    ) {
      Column(
        modifier = Modifier.navigationBarsPadding()
          .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (item.updatable) {
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
          } else {
            SelectionContainer(
              modifier = Modifier.weight(1F),
            ) {
              Text(
                text = item.title.value,
                style = TextStyle(
                  fontSize = 22.sp,
                  color = LocalAppColors.current.tvLv2,
                  fontWeight = FontWeight.Bold,
                )
              )
            }
          }
          val coroutineScope = rememberCoroutineScope()
          if (item.deletable) {
            Box(
              modifier = Modifier.padding(start = 8.dp)
                .size(32.dp)
                .clickableCardIndicator {
                  item.delete(coroutineScope, dismiss)
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
              )
            }
          }
          if (item.updatable) {
            Box(
              modifier = Modifier.padding(start = 8.dp)
                .size(32.dp)
                .clickableCardIndicator {
                  when (type.value) {
                    Description -> {
                      item.success(coroutineScope, dismiss)
                    }

                    EditTime, EditRepeat, EditColor -> {
                      type.value = Description
                    }
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (type.value) {
                  Description -> Icons.Rounded.Check
                  EditTime, EditRepeat, EditColor -> Icons.AutoMirrored.Rounded.ArrowBack
                },
                contentDescription = null,
              )
            }
          }
        }
        Row(
          modifier = Modifier.padding(top = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TimeTextCompose(item) {
            type.value = EditTime
          }
          RepeatTextCompose(
            item = item,
            repeatCurrent = repeatCurrent,
            modifier = Modifier.padding(start = 12.dp)
          ) {
            type.value = EditRepeat
          }
          // 暂时没想好该配置哪些颜色
//          if (item.updatable) {
//            ItemColorCompose(item) {
//              type.value = EditColor
//            }
//          }
        }
        AnimatedContent(targetState = type.value) {
          it.Content(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            item = item,
            weekBeginDate = weekBeginDate,
            timeline = timeline,
          )
        }
        bottomContent?.invoke()
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
      modifier = Modifier.run {
        if (item.updatable) {
          clickable {
            onClick.invoke()
          }
        } else this
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
      modifier = Modifier.run {
        if (item.updatable) {
          clickable {
            onClick.invoke()
          }
        } else this
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

@Composable
private fun ItemColorCompose(item: BottomSheetScheduleItem, onClick: () -> Unit) {
  Box(
    modifier = Modifier.padding(start = 12.dp)
      .size(14.dp)
      .clip(RoundedCornerShape(3.dp))
      .background(item.backgroundColor)
      .clickable {
        onClick.invoke()
      },
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "A",
      fontWeight = FontWeight.Bold,
      color = item.textColor,
      fontSize = 12.sp,
    )
  }
}

internal enum class AddScheduleBottomSheetState {
  Description {
    @Composable
    override fun Content(
      modifier: Modifier,
      item: BottomSheetScheduleItem,
      weekBeginDate: Date,
      timeline: CourseTimeline
    ) {
      if (item.updatable) {
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
      } else {
        SelectionContainer {
          Text(
            modifier = modifier.padding(top = 8.dp).fillMaxSize(),
            text = item.description.value,
            style = TextStyle(
              fontSize = 13.sp,
              color = LocalAppColors.current.tvLv2,
            ),
          )
        }
      }
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
  },
  EditColor {
    @Composable
    override fun Content(
      modifier: Modifier,
      item: BottomSheetScheduleItem,
      weekBeginDate: Date,
      timeline: CourseTimeline
    ) {
      EditColorCompose(modifier, item)
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
