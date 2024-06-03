package com.course.pages.team.ui.course.base

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.reflexScrollableForMouse
import com.course.components.utils.compose.showBottomSheetWindow
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.source.app.course.CourseBean
import com.course.source.app.course.LessonBean
import com.course.source.app.course.LessonPeriod
import com.course.source.app.course.LessonPeriodDate

/**
 * .
 *
 * @author 985892345
 * 2024/5/9 17:13
 */
class MemberCourseItemGroup(
  val excludeCourseNum: Set<String>,
) : ICourseItemGroup {

  fun resetData(data: List<Pair<MemberCourseItemData.Member, CourseBean>>) {
    val map = mutableMapOf<Date, WeekData>()
    val members = mutableListOf<MemberCourseItemData.Member>()
    data.fastForEach { (member, bean) ->
      members.add(member)
      bean.lessons.fastForEach { lesson ->
        lesson.period.fastForEach { period ->
          period.dateList.fastForEach { periodDate ->
            val weekBeginDate = periodDate.date.weekBeginDate
            map.getOrPut(weekBeginDate) {
              WeekData(weekBeginDate)
            }.lesson.add(LessonData(member, bean, lesson, period, periodDate))
          }
        }
      }
    }
    membersState.value = members
    dataMapState.value = map
  }

  private val membersState = mutableStateOf(emptyList<MemberCourseItemData.Member>())
  private val dataMapState = mutableStateOf(emptyMap<Date, WeekData>())

  class LessonData(
    val member: MemberCourseItemData.Member,
    val course: CourseBean,
    val lesson: LessonBean,
    val period: LessonPeriod,
    val periodDate: LessonPeriodDate,
  ) {
    val week: Int
      get() = course.beginDate.daysUntil(periodDate.date) / 7 + 1
  }

  private inner class WeekData(
    val weekBeginDate: Date,
  ) {

    val lesson = mutableListOf<LessonData>()

    private var data: List<MemberCourseItemData>? = null

    fun get(): List<MemberCourseItemData> {
      return data ?: collectData()
    }

    private fun collectData(): List<MemberCourseItemData> {
      return lesson.groupBy { it.periodDate.date.dayOfWeek }
        .map { collectDayData(weekBeginDate.plusDays(it.key.ordinal), it.value) }
        .flatten()
        .also { data = it }
    }

    private fun collectDayData(
      date: Date,
      data: List<LessonData>,
    ): List<MemberCourseItemData> {
      val positionArray = Array<MutableList<LessonData>>(12) { mutableListOf() }
      data.fastForEach {
        if (it.lesson.courseNum !in excludeCourseNum) {
          repeat(it.period.length) { position ->
            positionArray[position + it.period.beginLesson - 1].add(it)
          }
        }
      }
      val result = mutableListOf<MemberCourseItemData>()
      repeat(3) { i ->
        var start = i * 4
        repeat(4) { j ->
          if (positionArray[i * 4 + j].isEmpty()) {
            if (start != i * 4 + j) {
              result.add(
                MemberCourseItemData(
                  date = date,
                  beginLesson = start + 1,
                  node = List(i * 4 + j - start) { k ->
                    val list = positionArray[start + k]
                    MemberCourseItemData.Node(
                      member = list.map { it.member }.toSet()
                    )
                  },
                )
              )
            }
            start = i * 4 + j + 1
          }
        }
        if (start != (i + 1) * 4) {
          result.add(
            MemberCourseItemData(
              date = date,
              beginLesson = start + 1,
              node = List((i + 1) * 4 - start) { k ->
                val list = positionArray[start + k]
                MemberCourseItemData.Node(
                  member = list.map { it.member }.toSet()
                )
              },
            )
          )
        }
      }
      return result
    }
  }

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    dataMapState.value[weekBeginDate]?.get()?.fastForEach { data ->
      CardContent(
        backgroundColor = when {
          data.startTime.time < MinuteTime(12, 0) -> Color(0xFFF9E7D8)
          data.startTime.time < MinuteTime(18, 0) -> Color(0xFFF9E3E4)
          else -> Color(0xFFDDE3F8)
        },
        modifier = Modifier.singleDayItem(
          weekBeginDate = weekBeginDate,
          timeline = timeline,
          startTimeDate = data.startTime,
          minuteDuration = data.minuteDuration,
        )
      ) {
        Column {
          data.node.fastForEach { node ->
            Box(
              modifier = Modifier.fillMaxWidth()
                .weight(1F)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  clickItem(node)
                },
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = node.member.size.toString(),
                fontSize = 11.sp,
                color = when {
                  data.startTime.time < MinuteTime(12, 0) -> Color(0xFFFF8015)
                  data.startTime.time < MinuteTime(18, 0) -> Color(0xFFFF6262)
                  else -> Color(0xFF4066EA)
                },
              )
            }
          }
        }
      }
    }
  }

  @OptIn(ExperimentalLayoutApi::class)
  private fun clickItem(node: MemberCourseItemData.Node) {
    showBottomSheetWindow {
      Card(
        modifier = Modifier.fillMaxWidth().bottomSheetDraggable(),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
      ) {
        Column(
          modifier = Modifier.navigationBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .reflexScrollableForMouse()
            .verticalScroll(rememberScrollState())
        ) {
          Column {
            Text(
              text = "忙碌：${node.member.size}人",
              fontSize = 14.sp,
            )
            FlowRow(
              modifier = Modifier.padding(top = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              node.member.forEach {
                MemberCompose(it)
              }
            }
            Text(
              modifier = Modifier.padding(top = 16.dp),
              text = "空闲：${
                if (node.member.size == membersState.value.size) "无"
                else "${membersState.value.size - node.member.size}人"
              }",
              fontSize = 14.sp,
            )
            if (node.member.size != membersState.value.size) {
              FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                membersState.value.forEach {
                  if (it !in node.member) {
                    MemberCompose(it)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun MemberCompose(member: MemberCourseItemData.Member) {
    Card(
      modifier = Modifier,
      elevation = 1.dp,
    ) {
      Column(modifier = Modifier.padding(4.dp)) {
        Text(
          text = member.name,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = LocalAppColors.current.tvLv2,
        )
        Text(
          text = member.num,
          fontSize = 12.sp,
          color = Color(0xFF666666),
        )
      }
    }
  }
}