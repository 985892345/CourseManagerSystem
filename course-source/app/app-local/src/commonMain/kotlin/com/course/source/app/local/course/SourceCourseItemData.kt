package com.course.source.app.local.course

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import com.course.components.base.theme.LocalAppColors
import com.course.components.utils.compose.showBottomSheetDialog
import com.course.components.utils.serializable.ColorArgbSerializable
import com.course.pages.course.api.item.CardContent
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.pages.course.api.item.TopBottomText
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:18
 */
@Serializable
data class SourceCourseItemData(
  val id: String,
  val zIndex: Float,
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  @Serializable(ColorArgbSerializable::class)
  val backgroundColor: Color,
  val topText: String,
  @Serializable(ColorArgbSerializable::class)
  val topTextColor: Color,
  val bottomText: String,
  @Serializable(ColorArgbSerializable::class)
  val bottomTextColor: Color,
  val title: String? = null,
  val description: String? = null,
  val showOptions: List<Pair<String, String>>,
) {
  @Composable
  fun ICourseItemGroup.SourceContent(
    weekBeginDate: Date,
    timeline: CourseTimeline,
  ) {
    key(id) {
      CardContent(
        backgroundColor = backgroundColor,
        modifier = Modifier.zIndex(zIndex)
          .singleDayItem(
            weekBeginDate = weekBeginDate,
            timeline = timeline,
            startTimeDate = startTime,
            minuteDuration = minuteDuration,
          )
      ) {
        Box(modifier = Modifier.clickable {
          clickItem()
        }) {
          TopBottomText(
            top = topText,
            topColor = topTextColor,
            bottom = bottomText,
            bottomColor = bottomTextColor,
          )
        }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  private fun clickItem() {
    showBottomSheetDialog {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
      ) {
        Column(
          modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
          Text(
            modifier = Modifier.basicMarquee(),
            text = title ?: topText,
            fontSize = 22.sp,
            color = LocalAppColors.current.tvLv2,
            fontWeight = FontWeight.Bold,
          )
          if (description != null) {
            Text(
              modifier = Modifier.basicMarquee(),
              text = description,
              fontSize = 13.sp,
              color = LocalAppColors.current.tvLv2,
            )
          }
          showOptions.fastForEach {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
              Text(
                modifier = Modifier.align(Alignment.TopStart),
                text = it.first,
                fontSize = 15.sp,
                color = LocalAppColors.current.tvLv2,
              )
              Text(
                modifier = Modifier.align(Alignment.TopEnd),
                text = it.second,
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