package com.course.components.view.code

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.course.components.font.JBFontFamily
import kotlin.math.roundToInt

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 19:21
 */

@Composable
fun CodeCompose(
  text: MutableState<String>,
  editable: Boolean = true,
  modifier: Modifier = Modifier,
  hint: String = "",
  fontFamily: FontFamily = JBFontFamily(),
  style: MutableState<TextStyle> = remember {
    mutableStateOf(
      TextStyle(
        fontSize = 14.sp,
        fontFamily = fontFamily,
      )
    )
  },
) {
  var xOffset by mutableFloatStateOf(0F)
  var yOffset by mutableFloatStateOf(0F)
  var innerWidth by mutableIntStateOf(0)
  var outerWidth by mutableIntStateOf(0)
  var innerHeight by mutableIntStateOf(0)
  var outerHeight by mutableIntStateOf(0)
  Layout(
    modifier = modifier.pointerInput(Unit) {
      detectTransformGestures { _: Offset, pan: Offset, zoom: Float, _: Float ->
        xOffset = (xOffset + pan.x).coerceIn((outerWidth - innerWidth).toFloat(), 0F)
        yOffset = (yOffset + pan.y).coerceIn((outerHeight - innerHeight).toFloat(), 0F)
        style.value = style.value.copy(fontSize = style.value.fontSize * zoom)
      }
    }.graphicsLayer {
      translationY = yOffset
    },
    content = {
      val paddingTop = 8.dp
      val textLinesBottomState = mutableStateOf(emptyList<Float>())
      val hintLinesBottomState = mutableStateOf(emptyList<Float>())
      LineNumberCompose(
        paddingTop = paddingTop,
        style = style,
        linesBottom = remember {
          derivedStateOf {
            if (textLinesBottomState.value.size >= hintLinesBottomState.value.size) {
              textLinesBottomState.value
            } else {
              List(hintLinesBottomState.value.size) {
                if (it < textLinesBottomState.value.size) {
                  textLinesBottomState.value[it]
                } else {
                  hintLinesBottomState.value[it]
                }
              }
            }
          }
        },
      )
      TextCompose(
        modifier = Modifier.graphicsLayer {
          translationX = xOffset
        },
        text = text,
        editable = editable,
        hint = hint,
        paddingTop = paddingTop,
        style = style,
        textLinesBottom = textLinesBottomState,
        hintLinesBottom = hintLinesBottomState,
      )
    },
    measurePolicy = remember {
      { measurables: List<Measurable>, constraints: Constraints ->
        val textPlaceable = measurables[1].measure(Constraints())
        val linePlaceable = measurables[0].measure(
          Constraints(
            minHeight = textPlaceable.height + 20,
            maxHeight = textPlaceable.height + 20,
          )
        )
        val width =
          if (constraints.hasBoundedWidth) constraints.maxWidth else linePlaceable.width + textPlaceable.width + 50
        val height = linePlaceable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        innerWidth = maxOf(width, linePlaceable.width + textPlaceable.width + 50)
        outerWidth = width
        innerHeight = maxOf(height, linePlaceable.height)
        outerHeight = height
        layout(width, height) {
          textPlaceable.placeRelative(linePlaceable.width, 0)
          linePlaceable.placeRelative(0, 0)
        }
      }
    }
  )
}

@Composable
private fun LineNumberCompose(
  paddingTop: Dp,
  style: MutableState<TextStyle>,
  linesBottom: State<List<Float>>,
) {
  val count by remember { derivedStateOf { linesBottom.value.size } }
  Layout(
    modifier = Modifier.background(Color(0xFFE6E6E6))
      .padding(top = paddingTop, start = 8.dp, bottom = 4.dp, end = 6.dp),
    content = {
      val color = { Color(0xFF595959) }
      repeat(count) {
        BasicText(
          text = "${it + 1}",
          color = color,
          style = style.value,
        )
      }
    },
    measurePolicy = remember {
      { measurables, constraints ->
        var maxWidth = 0
        val placeables = measurables.fastMapIndexed { index, measurable ->
          measurable.measure(Constraints()).also {
            maxWidth = maxOf(maxWidth, it.width)
          }
        }
        layout(maxWidth, constraints.maxHeight) {
          var top = 0F
          placeables.fastForEachIndexed { index, placeable ->
            // 在删除行时先触发的 layout，此时 linesBottom 已经被提前修改，但 placeables 数量却还未修改
            if (index >= count) return@fastForEachIndexed
            placeable.placeRelative(
              x = maxWidth - placeable.width,
              y = (top + (linesBottom.value[index] - top - placeable.height) / 2).roundToInt()
            )
            top = linesBottom.value[index]
          }
        }
      }
    }
  )
}

@Composable
private fun TextCompose(
  modifier: Modifier,
  text: MutableState<String>,
  editable: Boolean,
  hint: String,
  paddingTop: Dp,
  style: MutableState<TextStyle>,
  textLinesBottom: MutableState<List<Float>>,
  hintLinesBottom: MutableState<List<Float>>,
) {
  Box(
    modifier = modifier.background(Color.Transparent)
      .padding(top = paddingTop, start = 8.dp)
  ) {
    if (editable) {
      val interactionSource = remember { MutableInteractionSource() }
      BasicTextField(
        value = text.value,
        onValueChange = { text.value = it },
        interactionSource = interactionSource,
        textStyle = style.value,
        decorationBox = { compose ->
          BasicText(
            modifier = Modifier.layout { measurable, constraints ->
              val placeable = measurable.measure(constraints)
              layout(
                if (text.value.isEmpty()) placeable.width else 0, // 有输入文本时 hint 宽度设置为 0
                placeable.height
              ) {
                placeable.placeRelative(0, 0)
              }
            },
            text = hint,
            color = { if (text.value.isEmpty()) Color.Gray else Color.Transparent },
            style = style.value,
            onTextLayout = { result ->
              hintLinesBottom.value = List(result.lineCount) { result.getLineBottom(it) }
            }
          )
          compose.invoke()
        },
        onTextLayout = { result ->
          textLinesBottom.value = List(result.lineCount) { result.getLineBottom(it) }
        }
      )
    } else {
      BasicText(
        text = text.value.ifEmpty { hint },
        color = { if (text.value.isEmpty()) Color.Gray else style.value.color },
        style = style.value,
        onTextLayout = { result ->
          if (text.value.isNotEmpty()) {
            textLinesBottom.value = List(result.lineCount) { result.getLineBottom(it) }
          } else {
            hintLinesBottom.value = List(result.lineCount) { result.getLineBottom(it) }
          }
        }
      )
    }
  }
}