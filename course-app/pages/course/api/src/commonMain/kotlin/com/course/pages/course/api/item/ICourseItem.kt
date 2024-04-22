package com.course.pages.course.api.item

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate
import com.course.shared.time.toMinuteTimeDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 使用 [CardContent] [TopBottomText] 统一样式
 *
 * @author 985892345
 * @date 2024/1/25 13:03
 */
@Stable
interface ICourseItem : Comparable<ICourseItem> {

  /**
   * 开始时间
   */
  val startTime: MinuteTimeDate

  /**
   * 长度，单位分钟
   */
  val minuteDuration: Int

  /**
   * 数字越大，优先级越高，优先显示在上面
   */
  val rank: Int

  /**
   * item 唯一的 key 值，可用于定位 item 在当前周内是否发生移动
   */
  val itemKey: String

  /**
   * @param data 当前显示在那天的日期上
   * @param scrollState 滚动状态
   * @param itemClickShow 点击 item 的回调
   */
  @Composable
  fun Content(
    data: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
    itemClickShow: CourseItemClickShow,
  )

  /**
   * 返回 1 显示在上面
   */
  override fun compareTo(other: ICourseItem): Int {
    if (this === other) return 0 // 如果是同一个对象直接返回 0
    if (this == other && hashCode() == other.hashCode()) return 0
    val s1 = startTime
    val e1 = s1.plusMinutes(minuteDuration)
    val s2 = other.startTime
    val e2 = s2.plusMinutes(other.minuteDuration)
    return if (e1 < s1) -1 else if (e2 < s1) 1
    // 存在重叠的时候
    else compareDiff(other.rank - rank) {
      if (s1 >= s2 && e1 <= e2 || s1 <= s2 && e1 >= e2) { // 包含关系
        compareDiff((s1.minutesUntil(e1)) - (s2.minutesUntil(e2))) {
          hashCode() - other.hashCode() // 我们假设 hashcode 不会冲突
        }
      } else { // 交叉关系
        // 以当前时间来计算谁在谁上面
        val nowTime = Clock.System.now()
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .toMinuteTimeDate()
        if (s1 < s2 && nowTime < s2) 1
        else if (s1 < s2 && nowTime >= s2) -1
        else if (s1 > s2 && nowTime < s1) -1
        else if (s1 > s2 && nowTime >= s1) 1
        else hashCode() - other.hashCode() // 我们假设 hashcode 不会冲突
      }
    }
  }

  override fun hashCode(): Int
  override fun equals(other: Any?): Boolean

  companion object {
    inline fun compareDiff(diff: Int, block: () -> Int): Int {
      return if (diff != 0) diff else block.invoke()
    }
  }
}

/**
 * 添加统一样式的圆角和边距
 */
@Composable
inline fun ICourseItem.CardContent(
  backgroundColor: Color,
  crossinline content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.padding(1.6.dp),
    shape = RoundedCornerShape(8.dp),
    elevation = 0.5.dp,
    backgroundColor = backgroundColor
  ) {
    content.invoke()
  }
}

/**
 * 添加统一样式的顶部和底部文字
 */
@Composable
fun ICourseItem.TopBottomText(
  top: String,
  topColor: Color,
  bottom: String,
  bottomColor: Color,
) {
  Box(
    modifier = Modifier.fillMaxSize()
      .padding(horizontal = 7.dp, vertical = 7.dp)
  ) {
    Text(
      text = top,
      textAlign = TextAlign.Center,
      color = topColor,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
      fontSize = 11.sp,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = bottom,
      textAlign = TextAlign.Center,
      color = bottomColor,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      fontSize = 11.sp,
      modifier = Modifier.fillMaxWidth()
        .align(Alignment.BottomCenter)
    )
  }
}