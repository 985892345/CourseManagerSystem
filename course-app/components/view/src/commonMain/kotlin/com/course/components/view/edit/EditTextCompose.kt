package com.course.components.view.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * .
 *
 * @author 985892345
 * 2024/3/23 17:11
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditTextCompose(
  text: MutableState<String>,
  keyboardType: KeyboardType = KeyboardType.Text,
  hint: String = "",
  modifier: Modifier = Modifier,
  isShowIndicatorLine: Boolean = true,
  textStyle: TextStyle = remember { TextStyle() },
  onValueChange: (String) -> Unit = {
    text.value = it
  },
) {
  val interactionSource = remember { MutableInteractionSource() }
  BasicTextField(
    modifier = modifier.background(Color.Transparent)
      .indicatorLine(
        enabled = true,
        isError = false,
        interactionSource = interactionSource,
        colors = TextFieldDefaults.textFieldColors(),
        focusedIndicatorLineThickness = if (isShowIndicatorLine) TextFieldDefaults.FocusedBorderThickness else 0.dp,
        unfocusedIndicatorLineThickness = if (isShowIndicatorLine) TextFieldDefaults.UnfocusedBorderThickness else 0.dp,
      ),
    value = text.value,
    onValueChange = onValueChange,
    keyboardOptions = KeyboardOptions(
      keyboardType = keyboardType,
    ),
    interactionSource = interactionSource,
    textStyle = textStyle,
    decorationBox = {
      if (text.value.isEmpty()) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = hint,
          color = Color.Gray,
          fontSize = textStyle.fontSize,
          fontFamily = textStyle.fontFamily,
          letterSpacing = textStyle.letterSpacing,
          textAlign = textStyle.textAlign,
          lineHeight = textStyle.lineHeight,
        )
      }
      it.invoke()
    }
  )
}