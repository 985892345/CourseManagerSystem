package com.course.pages.attendance.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastSumBy
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.onSuccess
import com.course.components.utils.time.toChinese
import com.course.components.view.fan.FanDiagramData
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.attendance.AttendanceApi
import com.course.source.app.attendance.AttendanceHistory
import com.course.source.app.attendance.AttendanceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 19:02
 */
@Serializable
@ObjectSerializable
class AttendanceHistoryScreen(
  val data: LessonItemData,
) : BaseScreen() {

  private var oldHistory: List<AttendanceHistory>? = null

  @Composable
  override fun ScreenContent() {
    var history by remember {
      mutableStateOf(oldHistory?.toImmutableList() ?: persistentListOf())
    }
    Column(
      modifier = Modifier.fillMaxSize().systemBarsPadding()
    ) {
      ToolbarCompose()
      TopStatusCompose(history)
      Card(
        modifier = Modifier.fillMaxWidth().weight(1F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        backgroundColor = Color(0xFFFAFAFA)
      ) {
        HistoryListCompose(
          modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp),
          history = history,
        )
      }
    }
    LaunchedEffect(Unit) {
      if (history.isEmpty()) {
        withContext(Dispatchers.IO) {
          runCatching {
            Source.api(AttendanceApi::class)
              .getAttendanceHistory(data.lesson.courseNum)
          }.tryThrowCancellationException().onSuccess { wrapper ->
            wrapper.onSuccess {
              history = it.toImmutableList()
              oldHistory = it
            }
          }
        }
      }
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Row(modifier = Modifier.fillMaxHeight()) {
        val navigator = LocalNavigator.current
        Box(
          modifier = Modifier.align(Alignment.CenterVertically)
            .padding(start = 12.dp)
            .size(32.dp)
            .clickableCardIndicator {
              navigator?.pop()
            },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
          )
        }
        Text(
          modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp),
          text = "考勤记录",
          fontSize = 21.sp,
          fontWeight = FontWeight.Bold,
          color = LocalAppColors.current.tvLv2
        )
      }
      Text(
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
        text = data.courseBean.term,
        color = Color(0xFF697C9B),
        fontSize = 12.sp,
      )
    }
  }

  @Composable
  private fun TopStatusCompose(history: ImmutableList<AttendanceHistory>) {
    Layout(
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      content = {
        LessonInformationCompose(
          modifier = Modifier.padding(start = 16.dp)
        )
        FanDiagramCompose(
          modifier = Modifier.size(160.dp),
          history = history,
        )
        HistorySumUpCompose(
          modifier = Modifier.padding(end = 16.dp),
          history = history,
        )
      },
      measurePolicy = { measurables, constraints ->
        val newConstraints =
          Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val lessonPlaceable = measurables[0].measure(newConstraints)
        val fanPlaceable = measurables[1].measure(newConstraints)
        val sumPlaceable = measurables[2].measure(newConstraints)
        var height = maxOf(lessonPlaceable.height, fanPlaceable.height, sumPlaceable.height)
        if (lessonPlaceable.width > constraints.maxWidth / 2) {
          // 课程名称实在太长(比如体育课)，扇形图放在课程名称下方显示
          height += lessonPlaceable.height / 3
        }
        layout(constraints.maxWidth, height) {
          lessonPlaceable.place(0, 0)
          val fanX = if (lessonPlaceable.width > constraints.maxWidth / 2) {
            (constraints.maxWidth - fanPlaceable.width) / 2
          } else {
            maxOf(
              lessonPlaceable.width - 4.dp.roundToPx(),
              (constraints.maxWidth - fanPlaceable.width) / 2
            ).coerceAtMost(constraints.maxWidth - sumPlaceable.width - fanPlaceable.width - 8.dp.roundToPx())
          }
          fanPlaceable.place(
            x = fanX,
            y = height - fanPlaceable.height,
          )
          sumPlaceable.place(
            x = minOf(
              constraints.maxWidth - sumPlaceable.width,
              fanX + fanPlaceable.width + 24.dp.roundToPx()
            ),
            y = height - sumPlaceable.height,
          )
        }
      }
    )
  }

  @Composable
  private fun LessonInformationCompose(
    modifier: Modifier
  ) {
    Column(modifier) {
      Text(
        text = data.lesson.course,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2,
      )
      Text(
        modifier = Modifier.padding(top = 3.dp),
        text = data.lesson.teacher,
        fontSize = 14.sp,
        color = LocalAppColors.current.tvLv2,
      )
      val alreadyAndRemainCount = getAlreadyAndRemainCount()
      Text(
        modifier = Modifier.padding(top = 3.dp),
        text = "已上${alreadyAndRemainCount.x}次",
        fontSize = 12.sp,
        color = Color.Gray,
      )
      Text(
        modifier = Modifier.padding(top = 3.dp),
        text = "剩余${alreadyAndRemainCount.y}次",
        fontSize = 12.sp,
        color = Color.Gray,
      )
    }
  }

  private fun getAlreadyAndRemainCount(): IntOffset {
    var already = 0
    var remain = 0
    val nowTimeDate = MinuteTimeDate.now()
    data.courseBean.lessons.fastForEach { bean ->
      if (bean.courseNum == data.lesson.courseNum) {
        val startTime = LessonItemData.getStartMinuteTime(bean.beginLesson)
        bean.weeks.fastForEach { week ->
          val startTimeDate = MinuteTimeDate(
            data.courseBean.beginDate.plusWeeks(week - 1).plusDays(bean.dayOfWeek.ordinal),
            startTime
          )
          if (startTimeDate < nowTimeDate) {
            already++
          } else {
            remain++
          }
        }
      }
    }
    return IntOffset(already, remain)
  }

  private var isFanDiagramDataNeedAnim: Boolean = true

  @Composable
  private fun FanDiagramCompose(
    modifier: Modifier,
    history: ImmutableList<AttendanceHistory>,
  ) {
    val localAppColors = LocalAppColors.current
    com.course.components.view.fan.FanDiagramCompose(
      modifier = modifier,
      data = remember(history, localAppColors) {
        history.groupBy { it.status }.mapValues {
          FanDiagramData(
            size = it.value.size.toFloat(),
            color = when (it.key) {
              AttendanceStatus.Attendance -> localAppColors.green
              AttendanceStatus.Absent -> localAppColors.red
              AttendanceStatus.Late -> localAppColors.yellow
              AttendanceStatus.AskForLeave -> localAppColors.blue
            },
            isNeedAnim = isFanDiagramDataNeedAnim,
          )
        }.values.sortedByDescending { it.size }.toImmutableList()
      },
    )
    if (history.isNotEmpty()) {
      isFanDiagramDataNeedAnim = false
    }
  }

  @Composable
  private fun HistorySumUpCompose(
    modifier: Modifier,
    history: ImmutableList<AttendanceHistory>
  ) {
    Column(modifier = modifier) {
      Text(
        text = "出勤：${history.fastSumBy { if (it.status == AttendanceStatus.Attendance) 1 else 0 }}次",
        color = LocalAppColors.current.green,
        fontSize = 12.sp,
      )
      Text(
        text = "缺勤：${history.fastSumBy { if (it.status == AttendanceStatus.Absent) 1 else 0 }}次",
        color = LocalAppColors.current.red,
        fontSize = 12.sp,
      )
      Text(
        text = "迟到：${history.fastSumBy { if (it.status == AttendanceStatus.Late) 1 else 0 }}次",
        color = LocalAppColors.current.yellow,
        fontSize = 12.sp,
      )
      Text(
        text = "请假：${history.fastSumBy { if (it.status == AttendanceStatus.AskForLeave) 1 else 0 }}次",
        color = LocalAppColors.current.blue,
        fontSize = 12.sp,
      )
    }
  }

  @Composable
  private fun HistoryListCompose(
    modifier: Modifier,
    history: ImmutableList<AttendanceHistory>,
  ) {
    Column(
      modifier = modifier.verticalScroll(state = rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      history.sortedByDescending { it.time }.fastForEach {
        Card(
          modifier = Modifier,
          elevation = 0.5.dp
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
              Text(
                text = "第${it.week}周  ${it.time.date.dayOfWeek.toChinese()}  ${it.time.time}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  modifier = Modifier,
                  text = "${it.time.date.monthNumber}月${it.time.date.dayOfMonth}号",
                  fontSize = 12.sp,
                  color = Color(0xFF666666),
                )
                Spacer(
                  modifier = Modifier.padding(horizontal = 6.dp)
                    .size(3.dp, 3.dp)
                    .background(Color(0xFF666666), CircleShape)
                )
                Text(
                  modifier = Modifier,
                  text = it.classroomSimplify,
                  fontSize = 12.sp,
                  color = Color(0xFF666666),
                )
              }
            }
            Text(
              modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
              text = when (it.status) {
                AttendanceStatus.Attendance -> "出勤"
                AttendanceStatus.Absent -> "缺勤"
                AttendanceStatus.Late -> "迟到"
                AttendanceStatus.AskForLeave -> "请假"
              },
              color = when (it.status) {
                AttendanceStatus.Attendance -> LocalAppColors.current.green
                AttendanceStatus.Absent -> LocalAppColors.current.red
                AttendanceStatus.Late -> LocalAppColors.current.yellow
                AttendanceStatus.AskForLeave -> LocalAppColors.current.blue
              }
            )
          }
        }
      }
    }
  }
}