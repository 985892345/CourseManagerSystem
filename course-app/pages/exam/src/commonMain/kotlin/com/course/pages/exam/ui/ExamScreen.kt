package com.course.pages.exam.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.clickableCardIndicator
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.serializable.ObjectSerializable
import com.course.pages.exam.model.ExamRepository
import com.course.pages.exam.ui.item.ExamListItem
import com.course.pages.exam.ui.item.ExamTermListHeader
import com.course.pages.exam.ui.item.IExamListItem
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/4/17 15:02
 */
@Serializable
@ObjectSerializable
class ExamScreen(
  val stuNum: String,
  val hasBack: Boolean,
) : BaseScreen() {

  @Composable
  override fun Content() {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
      ToolbarCompose()
      ListCompose()
    }
  }

  @Composable
  private fun ToolbarCompose() {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = "我的考试",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.tvLv2
      )
      if (hasBack) {
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
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = null,
          )
        }
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
    val examListItems by loadExamListItem()
    LazyColumn {
      examListItems.fastForEach {
        when (it) {
          is ExamListItem -> item(it.key) {
            with(it) { Content() }
          }
          is ExamTermListHeader -> stickyHeader(it.key, contentType = "stickyHeader") {
            with(it) { Content() }
          }
        }
      }
    }
  }

  @Composable
  private fun loadExamListItem(): State<List<IExamListItem>> {
    val examTermBeans = remember { mutableStateOf<List<IExamListItem>>(emptyList()) }
    LaunchedEffect(Unit) {
      runCatching {
        ExamRepository.refreshExamBean(stuNum)
      }.onSuccess {
        examTermBeans.value = IExamListItem.transform(it)
      }.tryThrowCancellationException()
    }
    return examTermBeans
  }
}