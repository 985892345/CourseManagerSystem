package com.course.applications.local

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.course.components.base.BaseComposeActivity

class MainActivity : BaseComposeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LocalApp.Content()
    }
  }
}


val color = Color(0xFFF5F5F5)