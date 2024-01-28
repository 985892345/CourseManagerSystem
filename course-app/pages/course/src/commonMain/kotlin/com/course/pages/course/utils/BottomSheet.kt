package com.course.pages.course.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/28 12:58
 */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetState.FractionBox(
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  propagateMinConstraints: Boolean = false,
  content: @Composable BoxScope.(fraction: Float) -> Unit,
) {
  val fraction = if (progress == 1F && currentValue == targetValue) {
    when (currentValue) {
      BottomSheetValue.Collapsed -> 0F
      BottomSheetValue.Expanded -> 1F
    }
  } else when (currentValue) {
    BottomSheetValue.Collapsed -> progress
    BottomSheetValue.Expanded -> 1F - progress
  }
  Box(
    modifier = Modifier.then(modifier),
    contentAlignment = contentAlignment,
    propagateMinConstraints = propagateMinConstraints,
  ) {
    content(fraction)
  }
}