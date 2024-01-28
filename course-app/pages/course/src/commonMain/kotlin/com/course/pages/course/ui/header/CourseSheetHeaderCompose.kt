package com.course.pages.course.ui.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetState
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.time.Today
import com.course.pages.course.ui.CourseCombine
import com.course.pages.course.ui.LocalCourseColor
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.utils.FractionBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlin.time.Duration.Companion.minutes

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 13:54
 */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseCombine.CourseSheetHeaderCompose(
  modifier: Modifier = Modifier,
  bottomSheetState: BottomSheetState,
  content: @Composable ColumnScope.(CourseSheetHeaderCombine) -> Unit = {
    it.SheetHeaderTopCompose()
    it.SheetHeaderContentCompose()
  }
) {
  Column(modifier = Modifier.padding(top = 4.dp).then(modifier)) {
    content(
      CourseSheetHeaderCombine(
        courseCombine = this@CourseSheetHeaderCompose,
        bottomSheetState = bottomSheetState,
      )
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Stable
data class CourseSheetHeaderCombine(
  val courseCombine: CourseCombine,
  val bottomSheetState: BottomSheetState,
) {
  val week get() = courseCombine.semesterVpData.terms.lastOrNull()
  val pager get() = week?.weeks?.get(courseCombine.nowWeek!!)

  @OptIn(ExperimentalCoroutinesApi::class)
  val itemFlow = snapshotFlow {
    Today
    pager?.items?.toList()
  }.filterNotNull().transformLatest {
    emit(it)
    while (true) delay(1.minutes)
  }.map { list ->
    list.asSequence().filter {
      it.dayOfWeek == Today.dayOfWeek ||
          it.dayOfWeek == DayOfWeek(Today.dayOfWeek.ordinal + 1)
    }.minOrNull()
  }.distinctUntilChanged()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseSheetHeaderCombine.SheetHeaderTopCompose(
  modifier: Modifier = Modifier,
  onClick: CoroutineScope.() -> Unit = {
    if (bottomSheetState.isCollapsed) {
      launch { bottomSheetState.expand() }
    }
  },
) {
  Card(
    modifier = Modifier.fillMaxWidth().height(16.dp).then(modifier),
    shape = RoundedCornerShape(topStartPercent = 100, topEndPercent = 100),
    backgroundColor = LocalCourseColor.current.background,
    elevation = 8.dp,
  ) {
    Box(
      modifier = Modifier,
      contentAlignment = Alignment.TopCenter
    ) {
      val coroutineScope = rememberCoroutineScope()
      Spacer(
        modifier = Modifier
          .clickable(
            onClick = { coroutineScope.onClick() },
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
          )
          .padding(top = 10.dp)
          .width(38.dp)
          .height(6.dp)
          .background(LocalCourseColor.current.sheetTip, RoundedCornerShape(6.dp))
      )
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseSheetHeaderCombine.SheetHeaderContentCompose(
  modifier: Modifier = Modifier,
  courseTop: @Composable BoxScope.() -> Unit = { courseCombine.CourseTopCompose() },
  content: @Composable BoxScope.() -> Unit = { SheetHeaderContentWithNoLessonCompose() },
) {
  bottomSheetState.FractionBox(
    modifier = Modifier.fillMaxWidth()
      .height(IntrinsicSize.Min)
      .background(LocalCourseColor.current.background)
      .then(modifier)
  ) { fraction ->
    Box(
      modifier = Modifier.fillMaxWidth()
        .wrapContentHeight()
        .alpha(maxOf(0F, fraction * 2 - 1F))
    ) {
      courseTop()
    }
    Box(
      modifier = Modifier.fillMaxSize()
        .alpha(maxOf(0F, (1 - fraction) * 2 - 1F))
    ) {
      content()
    }
  }
}

@Composable
fun CourseSheetHeaderCombine.SheetHeaderContentWithNoLessonCompose(
  item: ICourseItemBean? = itemFlow.collectAsState(null).value,
  noLesson: @Composable () -> Unit = {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "今天和明天都没课咯～",
        color = LocalAppColors.current.tvLv4,
        fontSize = 13.sp,
      )
    }
  },
  content: @Composable () -> Unit = {
    Text(
      text = "$item"
    )
  }
) {
  if (item == null) {
    noLesson()
  } else {
    content()
  }
}
