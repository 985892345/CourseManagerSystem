package com.course.pages.schedule.api.item.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.course.pages.schedule.api.item.BottomSheetScheduleItem

/**
 * .
 *
 * @author 985892345
 * 2024/5/10 16:44
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditColorCompose(
  modifier: Modifier,
  item: BottomSheetScheduleItem,
) {
  Box(modifier = modifier.padding(top = 16.dp)) {
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ScheduleColorDataList.fastForEachIndexed { i, data ->
        Box(
          modifier = Modifier.size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(data.backgroundColor).clickable {
              item.textColor = data.textColor
              item.backgroundColor = data.backgroundColor
            },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = ('A' + i).toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = data.textColor,
          )
        }
      }
    }
  }
}

val ScheduleColorDataList = listOf(
  ScheduleColorData(Color(0xFF546E7A), Color(0xFFCFD8DC)),
  ScheduleColorData(Color(0xFF6D4C41), Color(0xFFD7CCC8)),
  ScheduleColorData(Color(0xFF039BE5), Color(0xFFB3E5FC)),
  ScheduleColorData(Color(0xFF43A047), Color(0xFFC8E6C9)),
  ScheduleColorData(Color(0xFF7CB342), Color(0xFFDCEDC8)),
  ScheduleColorData(Color(0xFFAFB42B), Color(0xFFF0F4C3)),
  ScheduleColorData(Color(0xFFFFA000), Color(0xFFFFECB3)),
)

class ScheduleColorData(
  val textColor: Color,
  val backgroundColor: Color,
)