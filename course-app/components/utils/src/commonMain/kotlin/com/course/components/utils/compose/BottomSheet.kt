package com.course.components.utils.compose

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/28 12:58
 */

/**
 * 从 0 -> 1
 */
@Composable
@OptIn(ExperimentalMaterialApi::class)
fun rememberBottomSheetFraction(state: BottomSheetState): State<Float> {
  return remember {
    derivedStateOf(structuralEqualityPolicy()) {
      if (state.progress == 1F && state.currentValue == state.targetValue) {
        when (state.currentValue) {
          BottomSheetValue.Collapsed -> 0F
          BottomSheetValue.Expanded -> 1F
        }
      } else when (state.currentValue) {
        BottomSheetValue.Collapsed -> state.progress
        BottomSheetValue.Expanded -> 1F - state.progress
      }
    }
  }
}
