package com.course.components.view.drag

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.course.components.utils.compose.derivedStateOfStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

/**
 * 支持长按拖动改变顺序的 Column，但不支持大量数据，未使用 LazyLayout
 *
 * 官方的 LazyColumn 在长按拖动上存在较多的问题，所以这里使用 Layout 简单实现
 *
 * https://issuetracker.google.com/issues/181282427#comment25
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/integration-tests/foundation-demos/src/main/java/androidx/compose/foundation/demos/LazyColumnDragAndDropDemo.kt
 *
 *
 * @author 985892345
 * 2024/4/8 14:52
 */
@Composable
fun <T> DraggableColumn(
  items: SnapshotStateList<T>,
  modifier: Modifier = Modifier,
  scrollState: ScrollState = rememberScrollState(),
  contentPadding: PaddingValues = PaddingValues(0.dp),
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable DragItemState<T>.() -> Unit
) {
  val dragItemStateMap = remember(items) {
    hashMapOf<T, DragItemStateImpl<T>>()
  }
  val currentEventState = remember { mutableStateOf<PointerEvent?>(null) }
  val itemsList = items.toList() // 使用 toList 避免并发修改
  Layout(
    modifier = modifier.fillMaxHeight()
      .pointerInput(Unit) {
        awaitPointerEventScope {
          while (true) {
            currentEventState.value = awaitPointerEvent(PointerEventPass.Initial)
          }
        }
      }.verticalScroll(scrollState),
    content = {
      val itemCoroutineScope = rememberCoroutineScope()
      itemsList.fastForEachIndexed { index, item ->
        key(item) {
          dragItemStateMap.getOrPut(item) {
            DragItemStateImpl(
              scope = itemCoroutineScope,
              item = item,
              currentEventState = currentEventState,
            )
          }.also {
            it.update(
              index = index,
              items = items,
              dragItemStateMap = dragItemStateMap,
              scrollState = scrollState,
            )
            DraggableItem(it, content)
          }
        }
      }
    },
    measurePolicy = remember {
      DraggableColumnMeasurePolicy(
        items,
        dragItemStateMap,
        contentPadding,
        verticalArrangement,
        horizontalAlignment,
      )
    }.also {
      it.items = itemsList
      it.dragItemStateMap = dragItemStateMap
      it.contentPadding = contentPadding
      it.verticalArrangement = verticalArrangement
      it.horizontalAlignment = horizontalAlignment
    }
  )
}

@Composable
private fun <T> DraggableItem(
  itemState: DragItemStateImpl<T>,
  content: @Composable DragItemState<T>.() -> Unit,
) {
  Box(modifier = Modifier.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    itemState.size = placeable.height
    layout(placeable.width, placeable.height) {
      itemState.onLayout()
      placeable.placeWithLayer(
        x = itemState.dragOffsetX,
        y = itemState.dragOffsetY,
        zIndex = if (itemState.isDragging) 1F else 0F
      )
    }
  }) {
    content(itemState)
  }
}


private class DraggableColumnMeasurePolicy<T>(
  items: List<T>,
  dragItemStateMap: Map<T, DragItemStateImpl<T>>,
  contentPadding: PaddingValues,
  verticalArrangement: Arrangement.Vertical,
  horizontalAlignment: Alignment.Horizontal,
) : MeasurePolicy {

  var items: List<T> by mutableStateOf(items)
  var dragItemStateMap: Map<T, DragItemStateImpl<T>> by mutableStateOf(dragItemStateMap)
  var contentPadding: PaddingValues by mutableStateOf(contentPadding)
  var verticalArrangement: Arrangement.Vertical by mutableStateOf(verticalArrangement)
  var horizontalAlignment: Alignment.Horizontal by mutableStateOf(horizontalAlignment)

  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints
  ): MeasureResult {
    val paddingLeft = contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
    val paddingRight = contentPadding.calculateRightPadding(layoutDirection).roundToPx()
    val paddingTop = contentPadding.calculateTopPadding().roundToPx()
    val paddingBottom = contentPadding.calculateBottomPadding().roundToPx()

    val width = constraints.maxWidth
    val paddingWidth = (width - paddingLeft - paddingRight).coerceAtLeast(0)
    val outerHeight = constraints.minHeight
    val outerPaddingHeight = (outerHeight - paddingTop - paddingBottom).coerceAtLeast(0)

    val childConstraints = Constraints(
      maxWidth = paddingWidth,
      maxHeight = outerPaddingHeight,
    )

    var innerPaddingHeight = 0
    val spacingPx = verticalArrangement.spacing.roundToPx()
    val placeables = measurables.fastMapIndexed { _, measurable ->
      measurable.measure(childConstraints).also { placeable ->
        innerPaddingHeight += placeable.height + spacingPx
      }
    }
    innerPaddingHeight -= spacingPx
    val innerHeight = maxOf(outerHeight, innerPaddingHeight + paddingTop + paddingBottom)

    return layout(width, innerHeight) {
      val sizes = IntArray(placeables.size) { placeables[it].height }
      val outPositions = getOutPosition(verticalArrangement, innerPaddingHeight, sizes)
      placeables.fastForEachIndexed { index, placeable ->
        val x = horizontalAlignment.align(placeable.width, paddingWidth, layoutDirection) +
            paddingLeft
        val y = outPositions[index] + paddingTop
        val itemState = dragItemStateMap.getValue(items[index])
        itemState.setOffsetWhenLayout(y)
        placeable.placeWithLayer(
          x = x,
          y = y,
        )
      }
    }
  }
}

private fun Density.getOutPosition(
  verticalArrangement: Arrangement.Vertical,
  totalSize: Int,
  sizes: IntArray,
): IntArray {
  val outPositions = IntArray(sizes.size)
  with(verticalArrangement) {
    arrange(totalSize, sizes, outPositions)
  }
  return outPositions
}

@Stable
interface DragItemState<T> {
  val item: T
  val index: Int
  val isDragging: Boolean
  val dragState: DragState

  val offset: Int // 距离顶部的偏移量
  val size: Int // item 大小

  fun drag(event: DragEvent)

  // 在子组件上使用
  @Composable
  fun Modifier.draggableItem(isLongPress: Boolean): Modifier
}

sealed interface DragEvent {
  @JvmInline
  value class Dragging(val pointerId: PointerId) : DragEvent
  data object End : DragEvent
}

sealed interface DragState {
  data object Dragging : DragState
  data object Animate : DragState
  data object Idle : DragState
}

@Stable
private class DragItemStateImpl<T>(
  val scope: CoroutineScope,
  override val item: T,
  val currentEventState: State<PointerEvent?>,
) : DragItemState<T> {

  private val dragHelper = DragHelper(
    scope = scope,
    itemState = this,
    currentEventState = currentEventState,
  )

  override var index: Int by mutableIntStateOf(-1)

  override val isDragging: Boolean by derivedStateOfStructure { dragState == DragState.Dragging }
  override val dragState: DragState
    get() = dragHelper.dragState

  override var offset: Int by mutableIntStateOf(0)
  override var size: Int by mutableIntStateOf(0)

  var dragOffsetX: Int by mutableIntStateOf(0)
  var dragOffsetY: Int by mutableIntStateOf(0)

  private var offsetChangedAnimateJob: Job? = null
  private var offsetChangedAnimateVelocity = 0

  // 是否触发了交换
  private var isSwapTriggered = false

  fun update(
    index: Int,
    items: SnapshotStateList<T>,
    dragItemStateMap: Map<T, DragItemStateImpl<T>>,
    scrollState: ScrollState,
  ) {
    this.index = index
    dragHelper.update(
      items = items,
      dragItemStateMap = dragItemStateMap,
      scrollState = scrollState,
    )
  }

  override fun drag(event: DragEvent) {
    when (event) {
      is DragEvent.Dragging -> dragHelper.onDragging(event.pointerId)
      is DragEvent.End -> dragHelper.onDragEnd()
    }
  }

  @Composable
  override fun Modifier.draggableItem(
    isLongPress: Boolean
  ): Modifier = this then pointerInput(isLongPress) {
    var oldPointerId: PointerId? = null
    if (isLongPress) {
      detectDragGesturesAfterLongPress2(
        onDragStart = {
          oldPointerId = it.id
          drag(DragEvent.Dragging(it.id))
        },
        onDrag = { pointer, _ ->
          if (pointer.id != oldPointerId) {
            oldPointerId = pointer.id
            drag(DragEvent.Dragging(pointer.id))
          }
        },
        onDragEnd = { drag(DragEvent.End) },
        onDragCancel = { drag(DragEvent.End) }
      )
    } else {
      detectDragGestures(
        onDrag = { pointer, _ ->
          if (pointer.id != oldPointerId) {
            oldPointerId = pointer.id
            drag(DragEvent.Dragging(pointer.id))
          }
        },
        onDragEnd = { drag(DragEvent.End) },
        onDragCancel = { drag(DragEvent.End) }
      )
    }
  }

  private val forceLayoutState = mutableIntStateOf(0)

  fun onLayout() {
    dragHelper.onLayout()
    forceLayoutState.value // 观察 State 变量值，用于强制触发布局
    if (isSwapTriggered) {
      // 之前在 triggerSwap 函数中强制触发了一次布局，
      // 但存在调用 triggerSwap 后的下一帧只有子 layout 重新布局，
      // 父 layout 未重新布局 (即使 items 发生了改变，应该跟 composition 有关)
      // 所以这里一直强制触发布局直到 setOffsetWhenLayout 中 isSwapTriggered 被设置成 false
      forceLayoutState.intValue++
    }
  }

  fun triggerSwap() {
    isSwapTriggered = true
    forceLayoutState.intValue++
  }

  /**
   * 父 layout 中设置了位置后调用该方法设置的 [dragOffsetY] 需要在下一帧中才会有效，
   * 会导致效果看起来就是闪了一下
   *
   * 为了解决这个问题，需要在触发 swap 时强制调用 [forceLayoutState] 触发重布局
   *
   * 该函数在父 layout 布局时调用
   */
  fun setOffsetWhenLayout(newOffset: Int) {
    if (!isSwapTriggered) {
      offset = newOffset
      return
    }
    isSwapTriggered = false
    val oldOffset = Snapshot.withoutReadObservation { offset }
    offset = newOffset
    if (Snapshot.withoutReadObservation { dragState != DragState.Idle }) return
    if (oldOffset != newOffset) {
      dragOffsetY = oldOffset - newOffset
      offsetChangedAnimateJob?.cancel()
      offsetChangedAnimateJob = scope.launch {
        animate(
          typeConverter = Int.VectorConverter,
          initialValue = dragOffsetY,
          targetValue = 0,
          initialVelocity = offsetChangedAnimateVelocity,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { value, velocity ->
          dragOffsetY = value
          offsetChangedAnimateVelocity = velocity
        }
        offsetChangedAnimateJob = null
        offsetChangedAnimateVelocity = 0
      }
    }
  }
}

private class DragHelper<T>(
  val scope: CoroutineScope,
  val itemState: DragItemStateImpl<T>,
  val currentEventState: State<PointerEvent?>,
) {

  lateinit var items: SnapshotStateList<T>
  lateinit var dragItemStateMap: Map<T, DragItemStateImpl<T>>
  lateinit var scrollState: ScrollState

  var dragState: DragState by mutableStateOf(DragState.Idle)
    private set

  private var dragEndAnimateJob: Job? = null

  private var draggedInitialScrollValue = 0
  private var draggedInitialPosition = Offset.Zero
  private var draggedInitialItemOffset = 0
  private var draggedPosition = Offset.Zero
  private var draggedPointerId: PointerId? = null

  private var lastDraggedIndex: Int = -1

  private val scrollChannel = Channel<Int>()
  private var scrollUpEnable = false
  private var scrollDownEnable = false

  private var listenerScrollJob: Job? = null

  fun update(
    items: SnapshotStateList<T>,
    dragItemStateMap: Map<T, DragItemStateImpl<T>>,
    scrollState: ScrollState,
  ) {
    Snapshot.withoutReadObservation {
      if (!this::items.isInitialized || this.items != items) {
        check(dragState != DragState.Dragging) { "不允许在拖动期间修改 items" }
        this.items = items
      }
      if (!this::dragItemStateMap.isInitialized || this.dragItemStateMap != dragItemStateMap) {
        check(dragState != DragState.Dragging) { "不允许在拖动期间修改 items" }
        this.dragItemStateMap = dragItemStateMap
      }
      if (!this::scrollState.isInitialized || this.scrollState != scrollState) {
        this.scrollState = scrollState
      }
    }
  }

  fun onDragging(pointerId: PointerId) {
    tryInitDrag(pointerId)
    draggedPointerId = pointerId
  }

  /**
   * 重新布局时调用
   *
   * compose 跟 View 体系的触摸事件及动画的处理基本一致，只不过 View 一般是放在 onDraw 时设置位置，
   * compose 可以放在 layout 时设置位置，当然也可以放在 draw 时
   *
   * 这里我放在 layout 时设置位置是为了保证 itemState.offset 与 itemState.dragOffsetY 的同步
   */
  fun onLayout() {
    if (dragState == DragState.Dragging) {
      // 外部的 layout 会跟随 currentEventState 的改变而自动改变
      draggedPosition = currentEventState.value!!.changes.first { it.id == draggedPointerId }.position
      Snapshot.withoutReadObservation {
        resetItemStateOffset()
        trySwapItem()
        tryScroll()
      }
    }
  }

  fun onDragEnd() {
    listenerScrollJob?.cancel()
    listenerScrollJob = null
    dragEndAnimateJob = scope.launch {
      dragState = DragState.Animate
      animate(
        typeConverter = IntOffset.VectorConverter,
        initialValue = IntOffset(itemState.dragOffsetX, itemState.dragOffsetY),
        targetValue = IntOffset.Zero,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      ) { value, _ ->
        itemState.dragOffsetX = value.x
        itemState.dragOffsetY = value.y
      }
      dragState = DragState.Idle
    }
  }

  private fun tryInitDrag(pointerId: PointerId) {
    if (dragState == DragState.Animate) {
      dragEndAnimateJob?.cancel()
    }
    if (dragState != DragState.Dragging) {
      draggedInitialPosition =
        currentEventState.value!!.changes.first { it.id == pointerId }.position
      draggedInitialItemOffset = itemState.offset
      draggedInitialScrollValue = scrollState.value
      lastDraggedIndex = itemState.index

      val startEndOffset = calculateStartEndOffset()
      // 距离顶部的距离
      val startDistance = startEndOffset.x - scrollState.value
      // 距离底部的距离
      val endDistance = scrollState.viewportSize + scrollState.value - startEndOffset.y
      scrollDownEnable = startDistance > 10
      scrollUpEnable = endDistance < scrollState.viewportSize - 10

      // 监听滚轴的移动
      listenerScrollJob = scope.launch {
        snapshotFlow { scrollState.value }.collect {
          resetItemStateOffset()
          onDragging(draggedPointerId!!)
        }
      }

      dragState = DragState.Dragging
    }
  }

  private fun resetItemStateOffset() {
    val offset = calculateOffset()
    itemState.dragOffsetX = offset.x.roundToInt()
    itemState.dragOffsetY = offset.y.roundToInt()
  }

  private fun trySwapItem() {
    val startEndOffset = calculateStartEndOffset()
    val middleOffset = startEndOffset.x + (startEndOffset.y - startEndOffset.x) / 2F
    val targetItem = firstTargetItemState(middleOffset)
    if (targetItem != null && targetItem.index != lastDraggedIndex && targetItem !== itemState) {
      items.add(targetItem.index, items.removeAt(lastDraggedIndex))
      targetItem.triggerSwap() // target 触发交换，需要强制触发重布局
      lastDraggedIndex = targetItem.index
    }
  }

  private fun tryScroll() {
    val startEndOffset = calculateStartEndOffset()
    // 距离顶部的距离
    val startDistance = startEndOffset.x - scrollState.value
    // 距离底部的距离
    val endDistance = scrollState.viewportSize + scrollState.value - startEndOffset.y
    if (!scrollUpEnable && endDistance > 10) {
      // 不能向上滚动时需要先上移才允许向上滚动
      scrollUpEnable = true
    }
    if (!scrollDownEnable && startDistance > 10) {
      // 不能向下滚动时需要先下移才允许向下滚动
      scrollDownEnable = true
    }
    if (scrollDownEnable && startDistance < 0) {
      // 滑到顶部并且允许滚动
      scrollChannel.trySend(startDistance)
    } else if (scrollUpEnable && endDistance < 0) {
      // 滑到底部并且允许滚动
      scrollChannel.trySend(-endDistance)
    } else {
      scrollChannel.trySend(0)
    }
  }

  private fun calculateOffset(): Offset {
    return if (dragState == DragState.Dragging) {
      Offset(
        x = draggedPosition.x - draggedInitialPosition.x,
        y = (draggedPosition.y + scrollState.value) -
            (draggedInitialPosition.y + draggedInitialScrollValue) -
            (itemState.offset - draggedInitialItemOffset),
      )
    } else Offset.Zero
  }

  private fun calculateStartEndOffset(): IntOffset {
    val startOffset = itemState.offset + itemState.dragOffsetY
    return IntOffset(
      x = startOffset,
      y = startOffset + itemState.size
    )
  }

  private fun firstTargetItemState(offset: Float): DragItemStateImpl<T>? {
    items.fastForEach {
      val item = dragItemStateMap.getValue(it)
      if (offset.roundToInt() in item.offset..item.offset + item.size) {
        return item
      }
    }
    return null
  }

  init {
    // 自动滚动实现
    scope.launch {
      var scrollDiff: Float
      var scrollJob: Job? = null
      while (true) {
        val diff = scrollChannel.receive()
        if (diff == 0) {
          scrollJob?.cancel()
          scrollJob = null
        } else {
          scrollDiff = diff.toFloat()
          if (scrollJob == null) {
            scrollJob = launch {
              scrollState.scroll {
                animate(
                  0F,
                  0F,
                  animationSpec = infiniteRepeatable(tween(1000))
                ) { _, _ ->
                  scrollBy(scrollDiff)
                }
              }
            }
          }
        }
      }
    }
  }
}


// 跟官方代码一致，只不过 onDragStart 改成回调 PointerInputChange 而不是 Offset
private suspend fun PointerInputScope.detectDragGesturesAfterLongPress2(
  onDragStart: (PointerInputChange) -> Unit = { },
  onDragEnd: () -> Unit = { },
  onDragCancel: () -> Unit = { },
  onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
  awaitEachGesture {
    try {
      val down = awaitFirstDown(requireUnconsumed = false)
      val drag = awaitLongPressOrCancellation(down.id)
      if (drag != null) {
        onDragStart.invoke(drag)
        if (
          drag(drag.id) {
            onDrag(it, it.positionChange())
            it.consume()
          }
        ) {
          // consume up if we quit drag gracefully with the up
          currentEvent.changes.fastForEach {
            if (it.changedToUp()) it.consume()
          }
          onDragEnd()
        } else {
          onDragCancel()
        }
      }
    } catch (c: CancellationException) {
      onDragCancel()
      throw c
    }
  }
}


