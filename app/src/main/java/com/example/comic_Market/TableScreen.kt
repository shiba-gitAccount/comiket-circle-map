package com.example.comic_Market

import android.icu.text.Transliterator
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
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun TableScreenContainer(appViewModel: AppViewModel) {
    CompositionLocalProvider(
        LocalAppViewModel provides appViewModel
    ) {
        TableScreenContent()
    }
}
@Composable
fun TableScreenContent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() }
    ) { innerPadding ->
        TableContent(innerPadding)
    }
}
@Composable
fun TableContent(innerPadding: PaddingValues,appViewModel: AppViewModel = LocalAppViewModel.current) {
    val cfg = TableConfig()
    val scope = rememberCoroutineScope()
    Box(Modifier
        .background(Color.White)
        .fillMaxSize()
        .padding(innerPadding)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val pointers = awaitPointerEvent().changes.filter { it.pressed }
                    if (pointers.isEmpty()) continue
                    scrollAndZoom(pointers, maxSize = Size((this.size.width / appViewModel.zoomScale.value - cfg.contentWidth.toPx()), (this.size.height / appViewModel.zoomScale.value - cfg.contentHeight.toPx())), appViewModel = appViewModel, scope = scope)
                }
            }
        }
) {
        DrawTableCanvas(cfg)
    }
}



data class TableConfig(
    val rows: Int = 300,
    val cols: Int = 12,
    val cellWidth: Dp = 80.dp,
    val cellHeight: Dp = 25.dp,
    val contentWidth: Dp = cellWidth * cols,
    val contentHeight: Dp = cellHeight * rows
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(appViewModel: AppViewModel = LocalAppViewModel.current) {
    TopAppBar(
        title = { Text("My Screen") },
        modifier = Modifier.height(50.dp).offset(y =  appViewModel.topBarHeight.value)
    )
}


@Composable
fun DrawTableCanvas(cfg: TableConfig, appViewModel: AppViewModel = LocalAppViewModel.current) {
    var boxSize = remember{ IntSize.Zero }
    Canvas(modifier = Modifier
        .onSizeChanged { boxSize = it }
        .graphicsLayer(
            scaleX = appViewModel.zoomScale.value,
            scaleY = appViewModel.zoomScale.value,
            transformOrigin = TransformOrigin(0f,0f)
        )
        .offset { IntOffset(minmax(appViewModel.tableOffset.value.x,boxSize.width / appViewModel.zoomScale.value - cfg.contentWidth.toPx() ).toInt(),minmax(appViewModel.tableOffset.value.y,boxSize.height / appViewModel.zoomScale.value - cfg.contentHeight.toPx()).toInt()) }
        .size(cfg.contentWidth, cfg.contentHeight)

    ) {
        val cellWidthPx = cfg.cellWidth.toPx()
        val cellHeightPx = cfg.cellHeight.toPx()

        for (row in 0 until cfg.rows) {
            for (col in 0 until cfg.cols) {

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

suspend fun AwaitPointerEventScope.scrollAndZoom(initialPointers: List<PointerInputChange>, maxSize: Size, appViewModel: AppViewModel, scope: CoroutineScope) {
    val initialScale = appViewModel.zoomScale.value
    var lastPos = centerPosition(initialPointers)/ initialScale; var lastTime = System.currentTimeMillis()
    var vx = 0f; var vy = 0f;
    var initialDistance: Float? = null; var prevPointerCount = 0;


    while (true) {
        val pointers = awaitPointerEvent().changes.filter { it.pressed }
        if (pointers.isEmpty()) {
            break
        }

        val nowPos = centerPosition(pointers = pointers)/ appViewModel.zoomScale.value
        if(prevPointerCount != pointers.size) lastPos = nowPos

        if (pointers.size > 1) {
            val currDistance = (pointers[0].position - pointers[1].position).getDistance()
            if (initialDistance == null){
                initialDistance = currDistance
            } else {
                appViewModel.changeScale(initialScale * (currDistance / initialDistance))
            }
        }else initialDistance = null


        val nowTime = System.currentTimeMillis()

        val frameDx = nowPos.x - lastPos.x; val frameDy = nowPos.y - lastPos.y
        val currentOffset = Offset(appViewModel.tableOffset.value.x + frameDx,appViewModel.tableOffset.value.y + frameDy)
        val dt = nowTime - lastTime

        appViewModel.moveTopBarBy(frameDy.toDp() * appViewModel.zoomScale.value)


        appViewModel.snapTableOffset(clamp(currentOffset, maxSize))


        vx = computeVelocity(frameDx, dt)
        vy = computeVelocity(frameDy, dt)
        prevPointerCount = pointers.size
        lastPos = nowPos
        lastTime = nowTime
    }
    scope.launch {
        appViewModel.animateTableOffsetDecay(Offset(vx, vy))
    }
}

fun clamp(offset: Offset, maxSize: Size): Offset {
    return Offset(offset.x.coerceIn(maxSize.width,0f),offset.y.coerceIn(maxSize.height,0f))
}

fun computeVelocity(delta: Float, dt: Long) = if (dt > 0) delta / dt * 1000f else 0f
fun minmax(middle: Float, minimum: Float) : Float {
    return min(0f,max(middle,minimum))
}
fun centerPosition(pointers: List<PointerInputChange>) : Offset {
    return (pointers.fold(Offset.Zero) { acc, change -> acc + change.position }) / pointers.size.toFloat()
}


