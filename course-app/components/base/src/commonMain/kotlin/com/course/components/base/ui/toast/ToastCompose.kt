package com.course.components.base.ui.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.StateFactoryMarker
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.base.theme.LocalAppColors
import com.course.components.base.theme.LocalAppDarkTheme
import kotlinx.coroutines.delay

/**
 * .
 *
 * @author 985892345
 * @date 2023/12/22 10:14
 */

@StateFactoryMarker
fun toast(msg: CharSequence, duration: Long = 1500L) = Toast(msg, duration).show()

@StateFactoryMarker
fun toastLong(msg: CharSequence) = toast(msg, 3000L)

internal class Toast(
  val msg: CharSequence,
  val duration: Long,
) {
  fun show(): Boolean {
    if (!AppToastEnable) {
      AppToastState = this
      AppToastEnable = true
      return true
    }
    return false
  }
}

private var AppToastEnable by mutableStateOf(false)

private var AppToastState: Toast by mutableStateOf(Toast("", 0L))

@Composable
internal fun ToastCompose() {
  AnimatedVisibility(visible = AppToastEnable, enter = fadeIn(), exit = fadeOut()) {
    Column {
      Spacer(modifier = Modifier.weight(1F))
      Box(modifier = Modifier.weight(7F).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Card(
          modifier = Modifier.wrapContentSize(),
          shape = RoundedCornerShape(18.dp),
          backgroundColor = LocalAppColors.current.tvLv4
        ) {
          Box(
            modifier = Modifier.wrapContentSize()
              .padding(horizontal = 30.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = AppToastState.msg.toString(),
              color = if (LocalAppDarkTheme.current) Color.Black else Color.White,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }
  LaunchedEffect(AppToastEnable) {
    if (AppToastEnable) {
      delay(AppToastState.duration)
      AppToastEnable = false
    }
  }
}