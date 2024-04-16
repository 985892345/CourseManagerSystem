import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.launchApplication
import com.course.applications.pro.ProApp
import com.course.components.utils.coroutine.runApp
import com.course.components.view.drag.DraggableColumn

fun main() = runApp {
  ProApp.initApp()
  launchApplication {
    Window(
      onCloseRequest = ::exitApplication,
      title = "课表管理系统",
      state = WindowState(width = 390.dp, height = 800.dp),
//      resizable = false,
    ) {
      ProApp.Content()
//      val list = remember {
//        (0..19).map { it }.toMutableStateList()
//      }
//      DraggableColumn(
//        items = list,
//        modifier = Modifier,
//        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//      ) {
//        val elevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
//        Card(elevation = elevation, modifier = Modifier.draggableItem(true).height(60.dp)) {
//          Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//              imageVector = Icons.Default.DragHandle,
//              contentDescription = null,
//              modifier = Modifier.padding(vertical = 16.dp)
//            )
//            Text(
//              text = item.toString(),
//              modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//            )
//          }
//        }
//      }
    }
  }
}



