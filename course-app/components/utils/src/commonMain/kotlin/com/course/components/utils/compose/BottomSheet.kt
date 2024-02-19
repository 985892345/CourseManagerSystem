package com.course.components.utils.compose

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/28 12:58
 */

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetState.fraction: Float
  @Composable
  get() = if (progress == 1F && currentValue == targetValue) {
    when (currentValue) {
      BottomSheetValue.Collapsed -> 0F
      BottomSheetValue.Expanded -> 1F
    }
  } else when (currentValue) {
    BottomSheetValue.Collapsed -> progress
    BottomSheetValue.Expanded -> 1F - progress
  }