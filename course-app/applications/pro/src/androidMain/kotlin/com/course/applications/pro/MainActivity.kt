package com.course.applications.pro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.course.components.base.BaseComposeActivity
import com.course.components.view.drag.DraggableColumn

class MainActivity : BaseComposeActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ProApp.Content()
//      val list = remember {
//        (0..19).map { it }.toMutableStateList()
//      }
//      Box {
//        DraggableColumn(
//          items = list,
//          modifier = Modifier,
//          verticalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//          val elevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
//          Card(elevation = elevation, modifier = Modifier) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//              Icon(
//                imageVector = Icons.Default.DragHandle,
//                contentDescription = null,
//                modifier = Modifier.padding(vertical = 16.dp)
//              )
//              Text(
//                text = item.toString(),
//                modifier = Modifier
//                  .fillMaxWidth()
//                  .padding(20.dp)
//              )
//            }
//          }
//        }
//      }
    }
  }
}

val color = Color(0xFFF44336)