package com.course.pages.course.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.course.components.utils.time.Date
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/25 13:03
 */
@Stable
interface ICourseItemBean : Comparable<ICourseItemBean> {
  val date: Date
  val startTime: LocalTime
  val endTime: LocalTime

  /**
   * 数字越大，优先级越高，优先显示在上面
   */
  val rank: Int

  /**
   * 周内 item 唯一的 key 值，可用于定位 item 是否发生移动，不要与时间有关联
   */
  val weeklyKey: Any

  @Composable
  fun Content()

  /**
   * 返回 1 显示在上面
   */
  override fun compareTo(other: ICourseItemBean): Int {
    if (this === other) return 0 // 如果是同一个对象直接返回 0
    if (this == other && hashCode() == other.hashCode()) return 0
    return compareDiff(date.daysUntil(other.date)) {
      val s1 = startTime
      val e1 = endTime
      val s2 = endTime
      val e2 = other.endTime
      if (e1 < s1) -1 else if (e2 < s1) 1
      // 存在重叠的时候
      else compareDiff(other.rank - rank) {
        if (s1 >= s2 && e1 <= e2 || s1 <= s2 && e1 >= e2) { // 包含关系
          compareDiff((e1.toSecondOfDay() - s1.toSecondOfDay()) - (e2.toSecondOfDay() - s2.toSecondOfDay())) {
            hashCode() - other.hashCode() // 我们假设 hashcode 不会冲突
          }
        } else { // 交叉关系
          val nowTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
          // 长度相同，但起始位置不同，以当前时间来计算谁在谁上面
          compareDiff(nowTime.toSecondOfDay() - maxOf(s1, s2).toSecondOfDay()) {
            hashCode() - other.hashCode() // 我们假设 hashcode 不会冲突
          }
        }
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