package com.course.pages.team.ui.course

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.course.components.base.account.Account
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.ui.toast.toast
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.compose.dialog.showChooseDialog
import com.course.components.utils.compose.showBottomSheetWindow
import com.course.components.utils.debug.logg
import com.course.components.utils.list.fastSumByFloat
import com.course.components.utils.provider.Provider
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onFailure
import com.course.components.view.edit.EditTextCompose
import com.course.components.view.option.OptionSelectBackground
import com.course.components.view.option.OptionSelectCompose
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.item.lesson.LessonItemData
import com.course.pages.course.api.item.lesson.LessonItemGroup
import com.course.pages.course.api.item.lesson.toLessonItemBean
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.course.api.timeline.LessonTimelineData
import com.course.pages.team.ui.course.base.BottomSheetCourseController
import com.course.pages.team.ui.course.base.MemberCourseItemData
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountType
import com.course.source.app.course.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 12:14
 */
class ClassCourseBottomSheet(
  val data: LessonItemData,
  members: List<ClassMember>,
) : BottomSheetCourseController(
  members = members.map {
    MemberCourseItemData.Member(
      name = it.name,
      num = it.num,
      type = AccountType.Student,
    )
  },
  excludeCourseNum = setOf(data.lesson.courseNum),
  controllers = persistentListOf(),
) {

  private var nowLessonList = data.courseBean.copy(
    lessons = data.courseBean.lessons.filter {
      it.courseNum == data.lesson.courseNum
    }
  ).toLessonItemBean().toPersistentList()

  private val lessonItemGroup = LessonItemGroup {
    clickItem(it)
  }.also { group ->
    group.resetData(nowLessonList)
  }

  private val createLessons = mutableSetOf<LessonItemData>()

  private val addLesson = mutableStateOf<AddLesson?>(null)

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    lessonItemGroup.Content(weekBeginDate, timeline, scrollState)
    Spacer(modifier = Modifier.fillMaxSize().zIndex(-99999F).pointerInput(Unit) {
      detectTapGestures {
        val beginLesson = getBeginLesson(timeline, it, size.height)
        if (beginLesson > 0) {
          addLesson.value = AddLesson(
            date = weekBeginDate.plusDays((it.x / (size.width / 7)).toInt()),
            beginLesson = beginLesson,
            length = data.period.length,
          )
        } else {
          addLesson.value = null
        }
      }
    })
    addLesson.value?.Content(weekBeginDate, timeline)
  }

  private fun getBeginLesson(
    timeline: CourseTimeline,
    offset: Offset,
    totalHeight: Int,
  ): Int {
    val totalWeight = timeline.data.fastSumByFloat { it.nowWeight }
    var sumHeight = 0F
    timeline.data.fastForEach {
      val height = it.nowWeight / totalWeight * totalHeight
      if (offset.y in sumHeight..sumHeight + height) {
        if (it is LessonTimelineData) {
          val touch = it.lesson
          val length = data.period.length
          if (length == 4) {
            return (touch - 1) / 4 * 4 + 1
          } else if (length == 3) {
            return (touch - 1) / 4 * 4 + 1 + if (touch % 4 == 0) 1 else 0
          } else if (length == 2) {
            val start = (touch - 1) / 4 * 4 + 1
            return if (touch <= start + 1) start else start + 2
          } else if (length == 1) {
            return touch
          } else return -1
        }
      }
      sumHeight += height
    }
    return -1
  }

  private fun clickItem(
    lesson: LessonItemData,
  ) {
    val editor = CourseEditor(lesson)
    fun isAllowDismiss(dismiss: () -> Unit): Boolean {
      if (createLessons.contains(lesson)) {
        showCancelEditDialog("确定要放弃添加吗？") {
          createLessons.remove(lesson)
          nowLessonList = nowLessonList.remove(lesson)
          lessonItemGroup.resetData(nowLessonList)
          dismiss.invoke()
        }
      } else {
        if (editor.hasChanged()) {
          showCancelEditDialog("确定要放弃修改吗？", dismiss)
        } else return true
      }
      return false
    }
    showBottomSheetWindow(
      dismissOnBackPress = { isAllowDismiss(it) },
      dismissOnClickOutside = { isAllowDismiss(it) }
    ) { dismiss ->
      Card(
        modifier = Modifier.fillMaxWidth().bottomSheetDraggable(),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
      ) {
        Column(
          modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
          TopCompose(lesson, editor.editClassroom, dismiss)
          TimeOptionSelectCompose(editor)
          ButtonBtnCompose(editor = editor, dismiss = dismiss)
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun TopCompose(
    lesson: LessonItemData,
    editClassroom: MutableState<String>,
    dismiss: () -> Unit,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.weight(1F)) {
        Text(
          modifier = Modifier.basicMarquee(),
          text = data.lesson.courseName,
          fontSize = 22.sp,
          color = LocalAppColors.current.tvLv2,
          fontWeight = FontWeight.Bold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          EditTextCompose(
            text = editClassroom,
            hint = "输入教室",
            textStyle = TextStyle(
              fontSize = 13.sp,
              color = LocalAppColors.current.tvLv2,
            )
          )
          Spacer(
            modifier = Modifier.padding(horizontal = 3.dp)
              .size(3.dp, 3.dp)
              .background(Color.Gray, CircleShape)
          )
          Text(
            modifier = Modifier,
            text = data.period.teacher,
            fontSize = 13.sp,
            color = LocalAppColors.current.tvLv2,
          )
        }
      }
      if (lesson.periodDate.isNewly) {
        val coroutineScope = rememberCoroutineScope()
        Box(
          modifier = Modifier.padding(start = 8.dp)
            .size(32.dp)
            .clickableCardIndicator {
              showDeleteCourseDialog(coroutineScope, lesson, dismiss)
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
          )
        }
      }
    }
  }

  @Composable
  private fun ColumnScope.TimeOptionSelectCompose(
    editor: CourseEditor,
  ) {
    Row(
      modifier = Modifier.align(Alignment.CenterHorizontally)
        .padding(top = 16.dp)
        .height(120.dp)
    ) {
      OptionSelectBackground(
        modifier = Modifier.width(80.dp).fillMaxHeight()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            modifier = Modifier.weight(1F),
            text = "第",
            textAlign = TextAlign.Center,
          )
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = editor.weekLine,
            options = editor.weekLines,
          )
          Text(
            modifier = Modifier.weight(1F),
            text = "周",
            textAlign = TextAlign.Center,
          )
        }
      }
      OptionSelectBackground(
        modifier = Modifier.padding(horizontal = 8.dp).width(60.dp).fillMaxHeight()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            modifier = Modifier.weight(1F),
            text = "周",
            textAlign = TextAlign.Center,
          )
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = editor.dayOfWeekLine,
            options = editor.dayOfWeekLines,
          )
        }
      }
      OptionSelectBackground(
        modifier = Modifier.width(80.dp).fillMaxHeight()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = editor.beginLessonLine,
            options = editor.lessonLines,
          ) {
            if (editor.beginLessonLine.value > editor.finalLessonLine.value) {
              coroutineScope.launch {
                editor.finalLessonLine.animateTo(editor.beginLessonLine.value)
              }
            }
          }
          Text(
            modifier = Modifier.weight(1F),
            text = "-",
            textAlign = TextAlign.Center,
          )
          val coroutineScope = rememberCoroutineScope()
          OptionSelectCompose(
            modifier = Modifier.weight(1F),
            selectedLine = editor.finalLessonLine,
            options = editor.lessonLines,
          ) {
            if (editor.beginLessonLine.value > editor.finalLessonLine.value) {
              coroutineScope.launch {
                editor.beginLessonLine.animateTo(editor.finalLessonLine.value)
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun ButtonBtnCompose(
    editor: CourseEditor,
    dismiss: () -> Unit,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp).height(40.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      Card(
        modifier = Modifier.width(80.dp)
          .height(38.dp),
        backgroundColor = Color(0xFF4A44E4),
        shape = RoundedCornerShape(16.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            if (createLessons.contains(editor.lesson)) {
              showCancelEditDialog("确定要放弃添加吗") {
                createLessons.remove(editor.lesson)
                nowLessonList = nowLessonList.remove(editor.lesson)
                lessonItemGroup.resetData(nowLessonList)
                dismiss.invoke()
              }
            } else {
              dismiss.invoke()
            }
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "取消", color = Color.White)
        }
      }
      val coroutineScope = rememberCoroutineScope()
      Card(
        modifier = Modifier.width(80.dp)
          .height(38.dp),
        backgroundColor = Color(0xFFC3D4EE),
        shape = RoundedCornerShape(16.dp),
      ) {
        Box(
          modifier = Modifier.clickable {
            submit(
              editor = editor,
              coroutineScope = coroutineScope,
              dismiss = dismiss,
            )
          },
          contentAlignment = Alignment.Center
        ) {
          Text(text = "确认", textAlign = TextAlign.Center)
        }
      }
    }
  }

  private fun showCancelEditDialog(
    text: String,
    onClickPositive: () -> Unit,
  ) {
    showChooseDialog(
      onClickPositiveBtn = {
        onClickPositive.invoke()
        hide()
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
      }
    }
  }

  private fun showDeleteCourseDialog(
    coroutineScope: CoroutineScope,
    lesson: LessonItemData,
    dismiss: () -> Unit,
  ) {
    if (createLessons.contains(lesson)) {
      createLessons.remove(lesson)
      nowLessonList = nowLessonList.remove(lesson)
      lessonItemGroup.resetData(nowLessonList)
      dismiss.invoke()
      return
    }
    showChooseDialog(
      onClickPositiveBtn = {
        coroutineScope.launch(Dispatchers.IO) {
          runCatching {
            Source.api(CourseApi::class)
              .deleteCourse(data.periodDate.classPlanId)
              .onFailure { logg(it.info) }
              .getOrThrow()
          }.tryThrowCancellationException().onSuccess {
            toast("删除成功")
            nowLessonList = nowLessonList.remove(lesson)
            lessonItemGroup.resetData(nowLessonList)
            Provider.impl(ICourseService::class).forceRefreshMainCourse()
            dismiss.invoke()
            hide()
          }.onFailure {
            toast("网络异常")
            hide()
          }
        }
      }
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "确定删除该课程吗？")
      }
    }
  }

  private fun submit(
    editor: CourseEditor,
    coroutineScope: CoroutineScope,
    dismiss: () -> Unit,
  ) {
    if (!editor.isValid()) {
      return
    }
    if (createLessons.contains(editor.lesson)) {
      submitCreateCourse(
        editor = editor,
        coroutineScope = coroutineScope,
        dismiss = dismiss,
      )
    } else {
      submitChangeCourse(
        editor = editor,
        coroutineScope = coroutineScope,
        dismiss = dismiss,
      )
    }
  }

  private fun submitCreateCourse(
    editor: CourseEditor,
    coroutineScope: CoroutineScope,
    dismiss: () -> Unit,
  ) {
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(CourseApi::class)
          .createCourse(
            classNum = editor.lesson.lesson.classNum,
            date = editor.date.toString(),
            beginLesson = editor.beginLesson,
            length = editor.length,
            classroom = editor.editClassroom.value,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        toast("添加成功")
        createLessons.remove(editor.lesson)
        nowLessonList = nowLessonList.builder().apply {
          remove(editor.lesson)
          add(editor.createLessonItemDate(it, true))
        }.build()
        lessonItemGroup.resetData(nowLessonList)
        Provider.impl(ICourseService::class).forceRefreshMainCourse()
        dismiss.invoke()
      }.onFailure {
        toast("添加失败")
      }
    }
  }

  private fun submitChangeCourse(
    editor: CourseEditor,
    coroutineScope: CoroutineScope,
    dismiss: () -> Unit,
  ) {
    if (!editor.hasChanged()) {
      dismiss.invoke()
      return
    }
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(CourseApi::class)
          .changeCourse(
            classPlanId = editor.lesson.periodDate.classPlanId,
            newDate = editor.date.toString(),
            newBeginLesson = editor.beginLesson,
            newLength = editor.length,
            newClassroom = editor.editClassroom.value,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        toast("修改成功")
        nowLessonList = nowLessonList.builder().apply {
          remove(editor.lesson)
          add(editor.createLessonItemDate(editor.lesson.periodDate.classPlanId, editor.lesson.periodDate.isNewly))
        }.build()
        lessonItemGroup.resetData(nowLessonList)
        Provider.impl(ICourseService::class).forceRefreshMainCourse()
        dismiss.invoke()
      }.onFailure {
        toast("修改失败")
      }
    }
  }

  private inner class CourseEditor(
    val lesson: LessonItemData
  ) {
    val editClassroom = mutableStateOf(lesson.period.classroom)
    val weekLines = List(20) { "${it + 1}" }.toImmutableList()
    val weekLine = Animatable(
      initialValue = (lesson.week - 1).toFloat()
    )
    val week: Int get() = weekLine.value.toInt() + 1

    val dayOfWeekLines = persistentListOf("一", "二", "三", "四", "五", "六", "日")
    val dayOfWeekLine = Animatable(
      initialValue = lesson.periodDate.date.dayOfWeek.ordinal.toFloat()
    )
    val dayOfWeek: DayOfWeek get() = DayOfWeek.entries[dayOfWeekLine.value.toInt()]

    val lessonLines = List(12 - lesson.period.length + 1) { "${it + 1}" }.toImmutableList()
    val beginLessonLine = Animatable(
      initialValue = (lesson.period.beginLesson - 1).toFloat()
    )
    val beginLesson: Int get() = beginLessonLine.value.toInt() + 1

    val finalLessonLine = Animatable(
      initialValue = (lesson.period.beginLesson + lesson.period.length - 2).toFloat()
    )
    val finalLesson: Int get() = finalLessonLine.value.toInt() + 1
    val length: Int get() = finalLesson - beginLesson + 1

    val date: Date
      get() = lesson.courseBean.beginDate.plusWeeks(week - 1).plusDays(dayOfWeek.ordinal)

    fun hasChanged(): Boolean {
      return lesson.week != week ||
          lesson.periodDate.date.dayOfWeek != dayOfWeek ||
          lesson.period.beginLesson != beginLesson ||
          lesson.period.classroom != editClassroom.value
    }

    fun isValid(): Boolean {
      if (finalLesson < beginLesson) {
        toast("结束节数不能小于开始节数")
      } else {
        if (beginLesson <= 4 && finalLesson > 4) {
          toast("不能跨过中午时间段")
        } else if (beginLesson <= 8 && finalLesson > 8) {
          toast("不能跨过傍晚时间段")
        } else if (editClassroom.value.isBlank()) {
          toast("教室不能为空")
        } else {
          for (it in nowLessonList) {
            if (it.week == week && it.periodDate.date.dayOfWeek == dayOfWeek && it !== lesson) {
              val s1 = it.period.beginLesson
              val e1 = s1 + it.period.length - 1
              if (s1 in beginLesson..finalLesson ||
                e1 in beginLesson..finalLesson ||
                beginLesson in s1..e1 ||
                finalLesson in s1..e1
              ) {
                toast("该时间段已有相同课程")
                return false
              }
            }
          }
          return true
        }
      }
      return false
    }

    fun createLessonItemDate(periodId: Int, isNewlyAdded: Boolean): LessonItemData {
      return LessonPeriod(
        beginLesson = beginLesson,
        length = length,
        classroom = editClassroom.value,
        teacher = lesson.period.teacher,
        dateList = listOf(LessonPeriodDate(periodId, date, isNewlyAdded))
      ).toLessonItemBean(lesson.courseBean, lesson.lesson).first()
    }
  }

  private inner class AddLesson(
    val date: Date,
    val beginLesson: Int,
    val length: Int,
  ) {
    val startTime: MinuteTime
      get() = getStartMinuteTime(beginLesson)
    val minuteDuration: Int
      get() = getEndMinuteTime(beginLesson + length - 1).minuteOfDay - startTime.minuteOfDay

    @Composable
    fun Content(weekBeginDate: Date, timeline: CourseTimeline) {
      CardContent(
        backgroundColor = when {
          startTime < MinuteTime(12, 0) -> Color(0xFFF9E7D8)
          startTime < MinuteTime(18, 0) -> Color(0xFFF9E3E4)
          else -> Color(0xFFDDE3F8)
        },
        modifier = Modifier.singleDayItem(
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          startTimeDate = MinuteTimeDate(date, startTime),
          minuteDuration = minuteDuration,
        ).zIndex(999F)
      ) {
        var isClicked by remember { mutableStateOf(false) }
        Box(modifier = Modifier.clickable {
          isClicked = true
          val lessonData = LessonPeriod(
            beginLesson = beginLesson,
            length = length,
            classroom = data.period.classroom,
            teacher = Account.value?.name ?: "",
            dateList = listOf(LessonPeriodDate(classPlanId = 0, date = date, isNewly = true)),
          ).toLessonItemBean(data.courseBean, data.lesson).first()
          nowLessonList = nowLessonList.add(lessonData)
          createLessons.add(lessonData)
          lessonItemGroup.resetData(nowLessonList)
          clickItem(lessonData)
        }) {
          val textColor = when {
            startTime < MinuteTime(12, 0) -> Color(0xFFFF8015)
            startTime < MinuteTime(18, 0) -> Color(0xFFFF6262)
            else -> Color(0xFF4066EA)
          }
          AnimatedVisibility(
            visible = isClicked,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
          ) {
            TopBottomText(
              top = data.lesson.courseName,
              topColor = textColor,
              bottom = data.period.classroom,
              bottomColor = textColor,
            )
          }
          AnimatedVisibility(
            visible = !isClicked,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
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
            DisposableEffect(Unit) {
              onDispose {
                // 之后交给 LessonItemGroup 显示
                addLesson.value = null
              }
            }
          }
        }
      }
    }
  }
}

