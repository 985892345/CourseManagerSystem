package com.course.pages.attendance.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onFailure
import com.course.components.utils.source.onSuccess
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.shared.time.MinuteTimeDate
import com.course.shared.time.toMinuteTimeDate
import com.course.source.app.attendance.AttendanceApi
import com.course.source.app.attendance.AttendanceStatus
import com.course.source.app.attendance.AttendanceStudent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 19:32
 */
@Serializable
@ObjectSerializable
class AttendanceClassHistoryScreen(
  val data: LessonItemData
) : BaseScreen() {

  @Transient
  private val studentData = mutableStateOf<List<AttendanceStudentData>>(emptyList())

  private class AttendanceStudentData(
    val publishTime: MinuteTimeDate,
    val code: String,
    val attendance: SnapshotStateList<AttendanceStudent>,
    val late: SnapshotStateList<AttendanceStudent>,
    val absent: SnapshotStateList<AttendanceStudent>,
    val askForLeave: SnapshotStateList<AttendanceStudent>,
  )

  @Composable
  override fun ScreenContent() {
    Column(modifier = Modifier.fillMaxWidth().systemBarsPadding()) {
      ToolbarCompose()
      ListCompose()
    }
    requestData()
  }

  @Composable
  private fun requestData() {
    LaunchedEffect(Unit) {
      launch(Dispatchers.IO) {
        runCatching {
          Source.api(AttendanceApi::class)
            .getAttendanceStudent(data.periodDate.classPlanId)
            .getOrThrow()
        }.tryThrowCancellationException().onSuccess { studentLists ->
          studentData.value = studentLists.map { studentList ->
            val attendance = mutableListOf<AttendanceStudent>()
            val late = mutableListOf<AttendanceStudent>()
            val absent = mutableListOf<AttendanceStudent>()
            val askForLeave = mutableListOf<AttendanceStudent>()
            studentList.students.forEach {
              when (it.status) {
                AttendanceStatus.Attendance -> attendance.add(it)
                AttendanceStatus.Absent -> absent.add(it)
                AttendanceStatus.Late -> late.add(it)
                AttendanceStatus.AskForLeave -> askForLeave.add(it)
              }
            }
            AttendanceStudentData(
              publishTime = Instant.fromEpochMilliseconds(studentList.publishTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toMinuteTimeDate(),
              code = studentList.code,
              attendance = attendance.toMutableStateList(),
              late = late.toMutableStateList(),
              absent = absent.toMutableStateList(),
              askForLeave = askForLeave.toMutableStateList()
            )
          }
        }.onFailure {
          toast("网络异常")
        }
      }
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "考勤记录",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      val navigator = LocalNavigator.current
      Box(
        modifier = Modifier.align(Alignment.CenterStart)
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
      Spacer(
        modifier = Modifier.align(Alignment.BottomStart)
          .background(Color(0xDDDEDEDE))
          .fillMaxWidth()
          .height(1.dp)
      )
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun ListCompose() {
    LazyColumn(
      modifier = Modifier.fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      studentData.value.forEach { studentData ->
        stickyHeader(key = studentData.publishTime.toString(), contentType = "header") {
          ListHeaderCompose(studentData.publishTime.toString())
        }
        items(studentData.absent, key = { it.stuNum }, contentType = { "content" }) {
          ListContentCompose(it, studentData)
        }
        items(studentData.late, key = { it.stuNum }, contentType = { "content" }) {
          ListContentCompose(it, studentData)
        }
        items(studentData.attendance, key = { it.stuNum }, contentType = { "content" }) {
          ListContentCompose(it, studentData)
        }
        items(studentData.askForLeave, key = { it.stuNum }, contentType = { "content" }) {
          ListContentCompose(it, studentData)
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListHeaderCompose(text: String) {
    Text(
      modifier = Modifier.fillMaxWidth().animateItemPlacement()
        .background(MaterialTheme.colors.background),
      text = text,
      fontSize = 14.sp,
    )
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun LazyItemScope.ListContentCompose(student: AttendanceStudent, studentData: AttendanceStudentData) {
    val navigator = LocalNavigator.current
    Card(
      modifier = Modifier.animateItemPlacement().padding(end = 1.dp), // end padding 为解决 stickyHeader 右边界线问题
      shape = RoundedCornerShape(8.dp),
      elevation = 0.5.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
          .clickable {
            navigator?.push(AttendanceHistoryScreen(data, student.stuNum))
          }
          .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
      ) {
        Icon(
          modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
          imageVector = Icons.Outlined.AccountCircle,
          contentDescription = null,
        )
        Column(modifier = Modifier.padding(start = 8.dp, top = 2.dp).weight(1F)) {
          Text(
            text = student.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LocalAppColors.current.tvLv2,
          )
          Text(
            text = student.stuNum,
            fontSize = 12.sp,
            color = Color(0xFF666666),
          )
        }
        val coroutineScope = rememberCoroutineScope()
        Row(
          Modifier.padding(start = 8.dp, end = 16.dp).align(Alignment.CenterVertically)
        ) {
          Text(
            text = "出勤",
            color = LocalAppColors.current.green,
            fontSize = 14.sp,
            modifier = Modifier.clickToChangeStatus(
              coroutineScope = coroutineScope,
              student = student,
              studentData = studentData,
              newStatus = AttendanceStatus.Attendance,
            )
          )
          Text(
            text = "缺勤",
            color = LocalAppColors.current.red,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp).clickToChangeStatus(
              coroutineScope = coroutineScope,
              student = student,
              studentData = studentData,
              newStatus = AttendanceStatus.Absent,
            )
          )
          Text(
            text = "迟到",
            color = LocalAppColors.current.yellow,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp).clickToChangeStatus(
              coroutineScope = coroutineScope,
              student = student,
              studentData = studentData,
              newStatus = AttendanceStatus.Late,
            )
          )
          Text(
            text = "请假",
            color = LocalAppColors.current.blue,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp).clickToChangeStatus(
              coroutineScope = coroutineScope,
              student = student,
              studentData = studentData,
              newStatus = AttendanceStatus.AskForLeave,
            )
          )
        }
      }
    }
  }

  private fun Modifier.clickToChangeStatus(
    coroutineScope: CoroutineScope,
    student: AttendanceStudent,
    studentData: AttendanceStudentData,
    newStatus: AttendanceStatus,
  ): Modifier = this then Modifier.run {
    if (student.status != newStatus) {
      alpha(0.15F).clickable {
        showChangeStatusDialog(
          coroutineScope = coroutineScope,
          student = student,
          studentData = studentData,
          newStatus = newStatus,
        )
      }
    } else this
  }

  private fun showChangeStatusDialog(
    coroutineScope: CoroutineScope,
    student: AttendanceStudent,
    studentData: AttendanceStudentData,
    newStatus: AttendanceStatus,
  ) {
    showChooseDialog(
      onClickPositiveBtn = {
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(AttendanceApi::class)
              .changeAttendanceStatus(
                classPlanId = data.periodDate.classPlanId,
                code = studentData.code,
                stuNum = student.stuNum,
                status = newStatus,
              )
          }.tryThrowCancellationException().onSuccess { wrapper ->
            wrapper.onSuccess {
              toast("修改成功")
              when (newStatus) {
                AttendanceStatus.Attendance -> studentData.attendance.add(0, student.copy(status = AttendanceStatus.Attendance))
                AttendanceStatus.Absent -> studentData.absent.add(0, student.copy(status = AttendanceStatus.Absent))
                AttendanceStatus.Late -> studentData.late.add(0, student.copy(status = AttendanceStatus.Late))
                AttendanceStatus.AskForLeave -> studentData.askForLeave.add(0, student)
              }
              when (student.status) {
                AttendanceStatus.Attendance -> studentData.attendance.remove(student)
                AttendanceStatus.Absent -> studentData.absent.remove(student)
                AttendanceStatus.Late -> studentData.late.remove(student)
                AttendanceStatus.AskForLeave -> studentData.askForLeave.remove(student)
              }
            }.onFailure {
              toast(it.info)
            }
            hide()
          }.onFailure {
            toast("网络异常")
            hide()
          }
        }
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "确定将${student.name}由${student.status.chinese}改为${newStatus.chinese}吗？",
        )
      }
    }
  }
}