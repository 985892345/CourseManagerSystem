package com.course.applications.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.course.pages.course.ui.CourseCompose
import com.course.pages.course.ui.content.CourseContentCompose
import com.course.pages.course.ui.content.vp.CourseSemesterVpData
import com.course.pages.course.ui.header.CourseSheetHeaderCompose
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.collections.immutable.persistentListOf

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 17:45
 */
@ImplProvider
object ProMainScreen : Screen {
  @OptIn(ExperimentalMaterialApi::class)
  @Composable
  override fun Content() {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
      modifier = Modifier.fillMaxSize(),
      sheetPeekHeight = 60.dp,
      sheetBackgroundColor = Color.Transparent,
      sheetElevation = 0.dp,
      scaffoldState = bottomSheetScaffoldState,
      sheetContent = {
        CourseCompose(
          semesterVpData = CourseSemesterVpData(persistentListOf()),
        ) {
          it.CourseSheetHeaderCompose(
            bottomSheetState = bottomSheetScaffoldState.bottomSheetState,
          )
          it.CourseContentCompose()
        }
      },
    ) {
      Box(modifier = Modifier.fillMaxSize().background(Color.White))
    }
  }
}