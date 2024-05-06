package com.course.components.utils.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.pointer.PointerInputChange

/**
 * .
 *
 * @author 985892345
 * 2024/2/29 13:06
 */

internal actual fun reflexScrollableCanDrag(node: Modifier.Node) {
  val childField = Modifier.Node::class.java.getDeclaredField("child")
  childField.isAccessible = true
  var child = childField.get(node) as Modifier.Node
  while (child !is KeyInputModifierNode) {
    child = childField.get(child) as Modifier.Node
  }
  val draggableGesturesNodeField = child::class.java.getDeclaredField("draggableGesturesNode")
  draggableGesturesNodeField.isAccessible = true
  val draggableGesturesNode = draggableGesturesNodeField.get(child) as Modifier.Node
  val canDragField = draggableGesturesNode::class.java.superclass.getDeclaredField("_canDrag")
  canDragField.isAccessible = true
  canDragField.set(draggableGesturesNode, object : Function1<PointerInputChange, Boolean> {
    override fun invoke(input: PointerInputChange): Boolean = true
  })
}
