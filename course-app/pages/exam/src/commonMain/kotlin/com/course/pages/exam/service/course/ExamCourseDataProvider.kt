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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.CourseItemClickShow
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.exam.model.ExamRepository
import com.course.pages.exam.ui.ExamScreen
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
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
@ImplProvider(clazz = IMainCourseDataProvider::class, name = "ExamCourseDataProvider")
class ExamMainCourseDataProvider : IMainCourseDataProvider {
  override fun createCourseDataProviders(account: AccountBean?): List<CourseDataProvider> {
    return when (account?.type) {
      AccountType.Student -> listOf(ExamCourseDataProvider(account))
      AccountType.Teacher -> emptyList()
      null -> emptyList()
    }
  }
}

class ExamCourseDataProvider(
  val account: AccountBean
) : CourseDataProvider() {

  private var oldItems: List<ICourseItem> = emptyList()

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    if (oldItems.isEmpty()) {
      coroutineScope.launch {
        ExamRepository.getExamBean(account.num)
          .flowOn(Dispatchers.IO)
          .collect { termBean ->
            oldItems = termBean.flatMap { term ->
              term.exams.map { ExamItem(account, term, it) }
            }
            addAll(oldItems)
          }
      }
    }
  }

  private data class ExamItem(
    val account: AccountBean,
    val term: ExamTermBean,
    val bean: ExamBean
  ) : ICourseItem {
    override val startTime: MinuteTimeDate
      get() = bean.startTime
    override val minuteDuration: Int
      get() = bean.minuteDuration
    override val rank: Int
      get() = 999
    override val itemKey: String
      get() = "exam-${bean.courseNum}"

    @Composable
    override fun Content(
      data: Date,
      timeline: CourseTimeline,
      scrollState: ScrollState,
      itemClickShow: CourseItemClickShow
    ) {
      CardContent(Color(0xFFE4D9F5)) {
        Box(modifier = Modifier.clickable {
          clickItem(itemClickShow)
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
    private fun clickItem(itemClickShow: CourseItemClickShow) {
      itemClickShow.showItemDetail(this) {
        Box(
          modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
          Column {
            Row {
              val navigator = LocalNavigator.current
              Text(
                modifier = Modifier.align(Alignment.CenterVertically).clickable {
                  itemClickShow.cancelShow()
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