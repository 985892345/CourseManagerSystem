package com.course.pages.team.ui.course.base

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.derivedStateOfStructure
import com.course.components.utils.compose.showBottomSheetWindow
import com.course.components.utils.debug.logg
import com.course.components.utils.provider.Provider
import com.course.components.utils.time.Today
import com.course.pages.course.api.ICourseService
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.controller.CourseDetail
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 12:30
 */
abstract class BottomSheetCourseController(
  val members: List<MemberCourseItemData.Member>,
  excludeCourseNum: Set<String>,
  controllers: ImmutableList<CourseController>,
) : CourseDetail(controllers) {

  protected val courseService = Provider.impl(ICourseService::class)

  fun showCourseBottomSheet() {
    showBottomSheetWindow(
      scrimColor = Color.Transparent,
    ) { dismiss ->
      Box(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
      ) {
        Spacer(
          modifier = Modifier.fillMaxWidth().height(70.dp).background(
            brush = Brush.verticalGradient(
              colors = listOf(Color(0x00365789), Color(0x3D365789))
            )
          ).clickableNoIndicator {
            dismiss.invoke()
          }
        )
        Card(
          modifier = Modifier.padding(top = 15.dp).fillMaxSize(),
          shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
          elevation = 0.5.dp,
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            Box(
              modifier = Modifier.fillMaxWidth().height(18.dp).clickableNoIndicator {
                dismiss.invoke()
              }.bottomSheetDraggable(),
            ) {
              Spacer(
                modifier = Modifier.align(Alignment.BottomCenter)
                  .size(38.dp, 5.dp)
                  .background(
                    color = Color(0xFFE2EDFB),
                    shape = CircleShape,
                  )
              )
            }
            courseService.Content(this@BottomSheetCourseController)
          }
        }
      }
    }
  }

  private var beginDate by mutableStateOf<Date?>(null)

  override val startDate: Date by derivedStateOfStructure {
    beginDate ?: Today.firstDate
  }

  private var clickDate by mutableStateOf(Today)

  override val title: String by derivedStateOfStructure {
    beginDate?.let { getWeekStr(it, clickDate) } ?: "无数据"
  }

  override val subtitle: String
    get() = ""

  private val itemGroup = MemberCourseItemGroup(excludeCourseNum)

  override fun onChangedClickDate(date: Date) {
    super.onChangedClickDate(date)
    clickDate = date
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    itemGroup.Content(weekBeginDate, timeline, scrollState)
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
      logg("throwable = ${throwable.stackTraceToString()}")
    }) {
      members.map {
        it to async(Dispatchers.IO) {
          courseService.requestCourseBean(it.num, it.type, -1)
        }
      }.map {
        val bean = it.second.await()
        if (beginDate == null) {
          beginDate = bean.beginDate
        } else {
          if (beginDate != bean.beginDate) {
            throw IllegalStateException("beginDate 不一致, member=${it.first}")
          }
        }
        it.first to bean
      }.let {
        itemGroup.resetData(it)
      }
    }
  }

  private fun getWeekStr(start: Date, date: Date): String {
    val number = start.daysUntil(date) / 7 + 1
    if (number < 1) return ""
    if (number >= 100) return "第${number}周"
    val a = when (number / 10) {
      1 -> "十"
      2 -> "二十"
      3 -> "三十"
      4 -> "四十"
      5 -> "五十"
      6 -> "六十"
      7 -> "七十"
      8 -> "八十"
      9 -> "九十"
      else -> ""
    }
    val b = when (number % 10) {
      1 -> "一"
      2 -> "二"
      3 -> "三"
      4 -> "四"
      5 -> "五"
      6 -> "六"
      7 -> "七"
      8 -> "八"
      9 -> "九"
      else -> ""
    }
    return "第${a}${b}周"
  }
}