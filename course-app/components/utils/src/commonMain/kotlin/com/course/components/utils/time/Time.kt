package com.course.components.utils.time

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import com.course.shared.time.MinuteTime

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
