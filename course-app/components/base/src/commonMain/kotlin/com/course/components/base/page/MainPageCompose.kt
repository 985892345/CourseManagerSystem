package com.course.components.base.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.course.components.base.theme.AppTheme
import com.course.components.base.ui.toast.ToastCompose
import com.course.components.utils.compose.dialog.DialogCompose
import com.course.components.utils.navigator.BaseScreen
import com.course.components.utils.navigator.MainNavigator

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 19:13
 */

@Composable
fun MainPageCompose(screen: BaseScreen) {
  AppTheme {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
      MainNavigator(screen)
    }
    DialogCompose()
    ToastCompose()
  }
}
