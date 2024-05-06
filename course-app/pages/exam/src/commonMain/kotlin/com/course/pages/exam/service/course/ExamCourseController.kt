package com.course.pages.exam.service.course

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.showBottomSheetDialog
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.exam.model.ExamRepository
import com.course.pages.exam.ui.ExamScreen
import com.course.shared.time.Date
import com.course.source.app.account.AccountBean
import com.course.source.app.account.AccountType
import com.course.source.app.exam.ExamBean
import com.course.source.app.exam.ExamTermBean
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 12:26
 */
@ImplProvider(clazz = IMainCourseDataProvider::class, name = "ExamMainCourseDataProvider")
class ExamMainCourseDataProvider : IMainCourseDataProvider {
  override fun createCourseDataProviders(account: AccountBean?): List<CourseController> {
    return when (account?.type) {
      AccountType.Student -> listOf(ExamCourseController(account))
      AccountType.Teacher -> emptyList()
      null -> emptyList()
    }
  }
}

class ExamCourseController(
  val account: AccountBean
) : CourseController() {

  private var oldTimeline: CourseTimeline? = null
  private var oldItems: List<ExamItemData> = emptyList()
  private val itemMapState: MutableState<Map<Date, List<ExamItemData>>> =
    mutableStateOf(emptyMap())

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    if (oldItems.isEmpty()) {
      coroutineScope.launch {
        ExamRepository.getExamBean(account.num)
          .flowOn(Dispatchers.IO)
          .collect { termBean ->
            resetData(
              termBean.flatMap { term ->
                term.exams.map { ExamItemData(account, term, it) }
              }
            )
          }
      }
    }
  }

  private fun resetData(data: List<ExamItemData>) {
    oldItems = data
    val timeline = oldTimeline
    if (timeline != null) {
      itemMapState.value =
        data.groupBy { timeline.getItemWhichDate(it.bean.startTime).weekBeginDate }
    }
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    if (oldTimeline != timeline) {
      oldTimeline = timeline
      resetData(oldItems)
    }
    itemMapState.value[weekBeginDate]?.fastForEach {
      with(it) { ExamContent(weekBeginDate, timeline) }
    }
  }

  private data class ExamItemData(
    val account: AccountBean,
    val term: ExamTermBean,
    val bean: ExamBean
  ) {
    @Composable
    fun ICourseItemGroup.ExamContent(
      weekBeginDate: Date,
      timeline: CourseTimeline,
    ) {
      CardContent(
        backgroundColor = Color(0xFFE4D9F5),
        modifier = Modifier.zIndex(3F) // 考试显示在课程上面
          .singleDayItem(
            weekBeginDate = weekBeginDate,
            timeline = timeline,
            startTimeDate = bean.startTime,
            minuteDuration = bean.minuteDuration,
          ),
      ) {
        Box(modifier = Modifier.clickable {
          clickItem()
        }) {
          TopBottomText(
            top = bean.course,
            topColor = Color(0xFF904EF5),
            bottom = bean.classroom,
            bottomColor = Color(0xFF904EF5),
          )
        }
      }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun clickItem() {
      showBottomSheetDialog { dismiss ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        ) {
          Column(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
          ) {
            Row {
              val navigator = LocalNavigator.current
              Text(
                modifier = Modifier.align(Alignment.CenterVertically).clickable {
                  dismiss.invoke()
                  navigator?.push(ExamScreen(account.num, true))
                },
                text = "考试",
                fontSize = 22.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
              Spacer(
                modifier = Modifier.padding(horizontal = 6.dp)
                  .width(1.dp)
                  .height(16.dp)
                  .align(Alignment.CenterVertically)
                  .background(Color.Black)
              )
              Text(
                modifier = Modifier.align(Alignment.CenterVertically).basicMarquee(),
                text = bean.course,
                fontSize = 22.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
            }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
              Text(
                modifier = Modifier.align(Alignment.TopStart),
                text = "考试座位",
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.TopEnd),
                text = bean.seat,
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
            }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
              Text(
                modifier = Modifier.align(Alignment.TopStart),
                text = "考试时间",
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.TopEnd),
                text = "${bean.startTime.time}-${bean.startTime.time.plusMinutes(bean.minuteDuration)}",
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
            }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
              Text(
                modifier = Modifier.align(Alignment.TopStart),
                text = "考试类型",
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.TopEnd),
                text = bean.type,
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}