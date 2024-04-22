package com.course.applications.pro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.course.components.base.BaseComposeActivity

class MainActivity : BaseComposeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ProApp.Content()
    }
  }
}


val color = Color(0xFFE4D9F5)