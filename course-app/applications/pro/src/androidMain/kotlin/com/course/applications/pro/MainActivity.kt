package com.course.applications.pro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.course.components.base.BaseComposeActivity

class MainActivity : BaseComposeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ProScreenCompose()
    }
  }
}

val color = Color(0x02365789)