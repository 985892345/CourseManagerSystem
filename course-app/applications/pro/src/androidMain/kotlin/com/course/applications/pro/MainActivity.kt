package com.course.applications.pro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.course.components.base.BaseComposeActivity
import com.course.components.base.page.MainPageCompose

class MainActivity : BaseComposeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MainPageCompose(remember { ProMainScreen() })
    }
  }
}

val color = Color(0xFFF44336)