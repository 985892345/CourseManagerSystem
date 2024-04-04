package com.course.components.view.code

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import com.course.components.utils.platform.DesktopPlatform
import java.awt.event.MouseWheelEvent
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 因为官方没有暴露鼠标滚动，所以这里直接把代码 CV 过来了
 *
 * @author 985892345
 * 2024/4/2 21:35
 */

// TODO(demin): Chrome on Windows/Linux uses different scroll strategy
//  (always the same scroll offset, bounds-independent).
//  Figure out why and decide if we can use this strategy instead of the current one.
internal val LocalScrollConfig = compositionLocalOf<ScrollConfig> {
  when (DesktopPlatform.Current) {
    DesktopPlatform.Linux -> LinuxGnomeConfig
    DesktopPlatform.Windows -> WindowsWinUIConfig
    DesktopPlatform.MacOS -> MacOSCocoaConfig
    DesktopPlatform.Unknown -> WindowsWinUIConfig
  }
}

internal actual fun CompositionLocalConsumerModifierNode.platformScrollConfig(): ScrollConfig =
  currentValueOf(LocalScrollConfig)

internal abstract class DesktopScrollConfig : ScrollConfig {
  override var isSmoothScrollingEnabled = System.getProperty("compose.scrolling.smooth.enabled") != "false"
    internal set

  override fun isPreciseWheelScroll(event: PointerEvent): Boolean = event.isPreciseWheelRotation
}

// TODO(demin): is this formula actually correct? some experimental values don't fit
//  the formula
internal object LinuxGnomeConfig : DesktopScrollConfig() {
  // the formula was determined experimentally based on Ubuntu Nautilus behaviour
  override fun Density.calculateMouseWheelScroll(event: PointerEvent, bounds: IntSize): Offset {
    return if (event.shouldScrollByPage) {
      calculateOffsetByPage(event, bounds)
    } else {
      Offset(
        x = event.totalScrollDelta.x * sqrt(bounds.width.toFloat()),
        y = event.totalScrollDelta.y * sqrt(bounds.height.toFloat())
      )
    } * -event.scrollAmount
  }
}

internal object WindowsWinUIConfig : DesktopScrollConfig() {
  // the formula was determined experimentally based on Windows Start behaviour
  override fun Density.calculateMouseWheelScroll(event: PointerEvent, bounds: IntSize): Offset {
    return if (event.shouldScrollByPage) {
      calculateOffsetByPage(event, bounds)
    } else {
      Offset(
        x = event.totalScrollDelta.x * (bounds.width / 20f),
        y = event.totalScrollDelta.y * (bounds.height / 20f)
      )
    } * -event.scrollAmount
  }
}

internal object MacOSCocoaConfig : DesktopScrollConfig() {
  // the formula was determined experimentally based on MacOS Finder behaviour
  // MacOS driver will send events with accelerating delta
  override fun Density.calculateMouseWheelScroll(event: PointerEvent, bounds: IntSize): Offset {
    event.awtEventOrNull!!
    return if (event.shouldScrollByPage) {
      calculateOffsetByPage(event, bounds)
    } else {
      event.totalScrollDelta * 10.dp.toPx()
    } * -event.scrollAmount
  }
}

// TODO(demin): Chrome/Firefox on Windows scroll differently: value * 0.90f * bounds
// the formula was determined experimentally based on Windows Start behaviour
private fun calculateOffsetByPage(event: PointerEvent, bounds: IntSize): Offset {
  return Offset(
    x = event.totalScrollDelta.x * bounds.width,
    y = event.totalScrollDelta.y * bounds.height
  )
}

private val PointerEvent.scrollAmount
  get() = (awtEventOrNull as? MouseWheelEvent)?.scrollAmount?.toFloat() ?: 1f

private val PointerEvent.shouldScrollByPage
  get() = (awtEventOrNull as? MouseWheelEvent)?.scrollType == MouseWheelEvent.WHEEL_BLOCK_SCROLL

private val PointerEvent.totalScrollDelta
  get() = this.changes.fastFold(Offset.Zero) { acc, c -> acc + c.scrollDelta }

private val PointerEvent.isPreciseWheelRotation
  get() = (awtEventOrNull as? MouseWheelEvent)?.isPreciseWheelRotation ?: false

private val MouseWheelEvent.isPreciseWheelRotation
  get() = abs(preciseWheelRotation - wheelRotation.toDouble()) > 0.001
