package com.course.pages.course

import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.course.components.base.IComposePresenter
import com.course.pages.course.vp.rememberCoursePagerState

/**
 * .
 *
 * @author 985892345
 * 2024/1/14 21:14
 */
class BottomSheetCourseCompose : IComposePresenter {

  @OptIn(ExperimentalMaterialApi::class)
  @Composable
  override fun Content() {
    BottomSheetScaffold(
      sheetPeekHeight = 70.dp,
      sheetContent = {
        SheetContent()
      },
    ) {
    }
  }
}

@Composable
private fun SheetContent() {
  val courseVpCompose = rememberCoursePagerState()
  courseVpCompose.Content()
}
