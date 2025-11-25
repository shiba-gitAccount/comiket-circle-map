package com.example.comic_Market

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import kotlin.math.roundToInt

@Composable
fun TableScreenContainer() {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    var lastScroll by remember { mutableStateOf(0) }
    val threshold = 100
    var NavBarsVisible = remember { Visible(true) }
    LaunchedEffect(verticalScroll.value) {
        val delta = verticalScroll.value - lastScroll
        NavBarsVisible.update(delta)
        lastScroll = verticalScroll.value
    }
    TableScreenContent(verticalScroll = verticalScroll, horizontalScroll = horizontalScroll, NavBarsVisible = NavBarsVisible.value)
}

//fun createEmptyTable(rowCount: Int, colCount: Int): List<MutableList<String>> {
//    return List(rowCount) {
//        mutableStateListOf(*Array(colCount) { "" })
//    }
//}

@Composable
fun TableScreenContent(verticalScroll: ScrollState, horizontalScroll: ScrollState, NavBarsVisible: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        TableContent()
        TableHeader(isVisible = NavBarsVisible)
        TableFooter(isVisible = NavBarsVisible)
    }
}
@Composable
fun TableContent() {
    val rowCount: Int = 300
    val colCount: Int = 12
    val cellWidth: Dp = 90.dp
    val cellHeight: Dp = 50.dp
    // Canvas 全体のサイズ
    val contentWidth = cellWidth * colCount
    val contentHeight = cellHeight * rowCount
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        val startOffsetX = offsetX.value
                        val startOffsetY = offsetY.value

                        // ドラッグ処理（オフセット更新）
                        val result = handleDrag(down, startOffsetX, startOffsetY, offsetX, offsetY, scope)

                        // 慣性スクロール開始
                        startInertialScroll(offsetX, offsetY, result.velocityX, result.velocityY, scope)
                    }
                }
            }
    ) {
        DrawTableCanvas(
            rowCount = rowCount,
            colCount = colCount,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            offsetX = offsetX.value,
            offsetY = offsetY.value
        )
    }
}



@Composable
fun TableHeader(isVisible:  Boolean) {
    val targetOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-56).dp
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .offset(y = targetOffset)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("ヘッダー")
    }
}

@Composable
fun BoxScope.TableFooter(isVisible:  Boolean) {
    val targetOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 56.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .align(Alignment.BottomCenter)
            .offset(y = targetOffset)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("フッター")
    }
}

class Visible(initial: Boolean = true) {
    var value by mutableStateOf(initial)
        private set
    fun update(delta: Int) {
        when {
            delta > 20 -> value = false
            delta < -20 -> value = true
        }
    }
}

@Composable
fun DrawTableCanvas(
    rowCount: Int,
    colCount: Int,
    cellWidth: Dp,
    cellHeight: Dp,
    contentWidth: Dp,
    contentHeight: Dp,
    offsetX: Float = 0f,
    offsetY: Float = 0f
) {
    Canvas(modifier = Modifier.size(contentWidth,contentHeight).offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }) {
        val cellWidthPx = cellWidth.toPx()
        val cellHeightPx = cellHeight.toPx()

        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {

                val left = col * cellWidthPx
                val top = row * cellHeightPx

                // 背景
                drawRect(
                    color = if (row % 2 == 0) Color(0xFFE0E0E0) else Color(0xFFF5F5F5),
                    topLeft = Offset(left, top),
                    size = Size(cellWidthPx, cellHeightPx)
                )

                // 枠線
                drawRect(
                    color = Color.Gray,
                    topLeft = Offset(left, top),
                    size = Size(cellWidthPx, cellHeightPx),
                    style = Stroke(1f)
                )

                // --- ★ テキスト描画追加 ---
                drawContext.canvas.nativeCanvas.apply {
                    val text = "$row, $col"

                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 32f        // 固定のpx値（必要なら調整）
                        isAntiAlias = true
                    }

                    // 中心に置きたい場合はこういう調整も可能
                    val textX = left + 10f
                    val textY = top + cellHeightPx / 2f + 10f

                    drawText(text, textX, textY, paint)
                }
            }
        }
    }
}

data class DragResult(
    val velocityX: Float,
    val velocityY: Float
)

suspend fun AwaitPointerEventScope.handleDrag(
    down: PointerInputChange,
    startOffsetX: Float,
    startOffsetY: Float,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope
): DragResult {

    var lastPos = down.position
    var lastTime = System.currentTimeMillis()
    var vx = 0f
    var vy = 0f

    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.first()
        if (!change.pressed) break

        val nowPos = change.position
        val nowTime = System.currentTimeMillis()
        val dx = nowPos.x - down.position.x
        val dy = nowPos.y - down.position.y
        val frameDx = nowPos.x - lastPos.x
        val frameDy = nowPos.y - lastPos.y
        val dt = nowTime - lastTime

        scope.launch {
            offsetX.snapTo(startOffsetX + dx)
            offsetY.snapTo(startOffsetY + dy)
        }

        vx = if (dt > 0) (frameDx / dt) * 1000f else 0f   // px/s
        vy = if (dt > 0) (frameDy / dt) * 1000f else 0f

        lastPos = nowPos
        lastTime = nowTime
        change.consume()
    }

    // 仮の速度（まだ簡易）
    return DragResult(
        velocityX = (vx) ,
        velocityY = (vy)
    )
}


fun startInertialScroll(
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    velocityX: Float,
    velocityY: Float,
    scope: CoroutineScope
) {
    val decay = exponentialDecay<Float>()

    scope.launch {
        offsetX.animateDecay(velocityX, decay)
    }
    scope.launch {
        offsetY.animateDecay(velocityY, decay)
    }
}


