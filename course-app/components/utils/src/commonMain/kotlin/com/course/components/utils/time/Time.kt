package com.course.components.utils.time

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import com.course.shared.time.MinuteTime
import kotlinx.datetime.DayOfWeek

/**
 * .
 *
 * @author 985892345
 * 2024/4/29 22:12
 */
val MinuteTime.Companion.VectorConverter: TwoWayConverter<MinuteTime, AnimationVector1D>
  get() = MinuteTimeVectorConverter

private val MinuteTimeVectorConverter = TwoWayConverter<MinuteTime, AnimationVector1D>(
  { AnimationVector1D((it.hour * 60 + it.minute).toFloat()) },
  { vector1D ->
    vector1D.value.toInt().let {
      MinuteTime(it / 60, it % 60)
    }
  }
)

fun DayOfWeek.toChinese(prefix: String = "周"): String {
  return prefix + when (this) {
    java.time.DayOfWeek.MONDAY -> "一"
    java.time.DayOfWeek.TUESDAY -> "二"
    java.time.DayOfWeek.WEDNESDAY -> "三"
    java.time.DayOfWeek.THURSDAY -> "四"
    java.time.DayOfWeek.FRIDAY -> "五"
    java.time.DayOfWeek.SATURDAY -> "六"
    java.time.DayOfWeek.SUNDAY -> "日"
  }
}