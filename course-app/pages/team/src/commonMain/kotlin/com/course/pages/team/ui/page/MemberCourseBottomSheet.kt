package com.course.pages.team.ui.page

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.course.components.utils.compose.clickableNoIndicator
import com.course.components.utils.compose.showBottomSheetWindow
import com.course.components.utils.provider.Provider
import com.course.pages.course.api.ICourseService
import com.course.source.app.account.AccountType
import com.course.source.app.team.TeamMember

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 14:10
 */
class MemberCourseBottomSheet(
  val member: TeamMember
) {

  private val courseService = Provider.impl(ICourseService::class)

  private val courseDetail = when (member.type) {
    AccountType.Student -> courseService.stuCourseDetail(member.num)
    AccountType.Teacher -> courseService.stuCourseDetail(member.num)
  }

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
            courseService.Content(courseDetail)
          }
        }
      }
    }
  }
}