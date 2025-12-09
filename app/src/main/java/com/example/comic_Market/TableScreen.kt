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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import java.net.URL
import kotlin.Boolean
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
fun TableScreenContent(appViewModel: AppViewModel = LocalAppViewModel.current) {
    val topOffset = with(LocalDensity.current) { calcTopOffset(appViewModel = appViewModel, heightPx = appViewModel.topBarHeight.value.toPx()) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(topOffset) }
    ) { innerPadding ->
        TableContent(innerPadding,topOffset,appViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(topOffset: Int, appViewModel: AppViewModel = LocalAppViewModel.current) {
    TopAppBar(
        title = { Text("My Screen") },
        modifier = Modifier.height(50.dp).offset{ IntOffset(x = 0, y = topOffset) }
    )
}
fun calcTopOffset(heightPx: Float, appViewModel: AppViewModel): Int{
    return max(
        heightPx,
        min(0f,appViewModel.tableOffset.value.y * appViewModel.zoomScale.value)
    ).toInt()
}

@Composable
fun TableContent(innerPadding: PaddingValues, topOffset: Int, appViewModel: AppViewModel) {
    val cfg = TableConfig()
    val scope = rememberCoroutineScope()
    var maxSize = remember { mutableStateOf(Size.Zero) }
    val conWidthPx = with(LocalDensity.current) { cfg.contentWidth.toPx() }
    val conHeightPx = with(LocalDensity.current) { cfg.contentHeight.toPx() }
    Box(modifier = Modifier.padding(innerPadding)){
        val offset = calcOffset(appViewModel, maxSize.value)
        DrawColTag(offset, topOffset, appViewModel)
        DrawRowNum(offset, topOffset, appViewModel)
        Box(Modifier
            .background(Color.White)
            .fillMaxSize()
            .offset({IntOffset(x = (40.dp.toPx() * appViewModel.zoomScale.value).toInt(),y = (25.dp.toPx() * appViewModel.zoomScale.value).toInt())})
            .onSizeChanged { maxSize.value = calcMaxSize(it, appViewModel, conWidthPx, conHeightPx) }
            .zIndex(0f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val pointers = awaitPointerEvent().changes.filter { it.pressed }
                        if (pointers.isEmpty()) continue
                        scrollAndZoom(pointers, maxSize = maxSize.value, appViewModel = appViewModel, scope = scope)
                    }
                }
            }
        ) {
            DrawTableCanvas(cfg, offset)
        }
    }
}

@Composable
fun DrawColTag(offset: IntOffset, topOffset: Int, appViewModel: AppViewModel) {
    val tags = listOf("CB", "ユーザー", "曜日", "ホール", "スペース", "サークル名", "作者名", "Twitter", "備考", "値段", "担当")
    Canvas(modifier = Modifier.zIndex(2f).offset{ IntOffset(x = 0, y = topOffset) }){ drawCell(left = 0f, top = 0f, width = 40.dp.toPx() * appViewModel.zoomScale.value, height = 25.dp.toPx() * appViewModel.zoomScale.value, text = "") }
    Canvas(modifier = Modifier.zIndex(1f).graphicsLayer(scaleX = appViewModel.zoomScale.value, scaleY = appViewModel.zoomScale.value, transformOrigin = TransformOrigin(0f, 0f)).offset({ IntOffset( x = offset.x + 40.dp.toPx().toInt(), y = (topOffset / appViewModel.zoomScale.value).toInt() )})){
        val colWidthsPx = columnWidthsDp.map { it.dp.toPx() }
        val cumulative = colWidthsPx.runningFold(0f) { acc, v -> acc + v }
        tags.forEachIndexed { col,text ->
            drawCell(left = cumulative[col], top = 0f, width = colWidthsPx[col], height = 25.dp.toPx(), text = text)
        }
    }
}

@Composable
fun DrawRowNum(offset: IntOffset, topOffset: Int, appViewModel: AppViewModel) {
    Canvas(modifier = Modifier.graphicsLayer(scaleX = appViewModel.zoomScale.value, scaleY = appViewModel.zoomScale.value, transformOrigin = TransformOrigin(0f, 0f)).zIndex(1f).offset({ IntOffset( x = 0, y = offset.y + 25.dp.toPx().toInt()) })){
        val cellHeight = 25.dp.toPx()
        for(row in 0 until 300){
            drawCell(left = 0f, top = row * cellHeight, width = 40.dp.toPx(), height = 25.dp.toPx(), text = (row+1).toString())
        }
    }
}



data class TableConfig(
    val rows: Int = 300,
    val cellHeight: Dp = 25.dp,
    val contentWidth: Dp = (columnWidthsDp.reduce { acc, i -> acc + i } +40).dp,
    val contentHeight: Dp = cellHeight * rows + 25.dp
)




@Composable
fun DrawTableCanvas(cfg: TableConfig, offset: IntOffset, appViewModel: AppViewModel = LocalAppViewModel.current) {
    val zoom = appViewModel.zoomScale.value
    Canvas(
        modifier = Modifier
            .graphicsLayer(
                scaleX = zoom,
                scaleY = zoom,
                transformOrigin = TransformOrigin(0f, 0f)
            )
            .offset { offset }
            .size(cfg.contentWidth, cfg.contentHeight)
    ) {
        drawTable(appViewModel.circles.value)
    }
}

private fun calcOffset(app: AppViewModel, maxSize: Size): IntOffset {
    val offX = minmax(
        app.tableOffset.value.x,
        maxSize.width
    )
    val offY = minmax(
        app.tableOffset.value.y,
        maxSize.height
    )
    return IntOffset(offX.toInt(), offY.toInt())
}

private fun calcMaxSize(boxSize: IntSize, appViewModel: AppViewModel, width: Float, height: Float): Size{
    return Size(boxSize.width / appViewModel.zoomScale.value - width, boxSize.height / appViewModel.zoomScale.value - height)
}

private fun DrawScope.drawTable(
    circles: List<Circle>
) {
    val colWidthsPx = columnWidthsDp.map { it.dp.toPx() }
    val rowHeightPx = 25.dp.toPx()

    circles.forEachIndexed { rowIndex, circle ->
        drawCircleRow(circle, rowIndex, colWidthsPx, rowHeightPx)
    }
}

private fun DrawScope.drawCircleRow(
    circle: Circle,
    row: Int,
    colWidths: List<Float>,
    rowHeight: Float
) {
    var left = 0f
    val top = row * rowHeight

    val values = circleToDisplayList(circle)

    values.forEachIndexed { col, text ->
        val w = colWidths[col]

        drawCell( left, top, w, rowHeight, text)

        left += w
    }
}

private fun DrawScope.drawCell(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    text: String?,
){
    drawCellBackground(left, top, width, height)
    drawCellBorder(left, top, width, height)
    drawCellText(text, left, top, height)
}

private fun circleToDisplayList(circle: Circle): List<String?> = listOf(
    if (circle.check) "✔" else "",
    circle.user,
    circle.week?.kanji,
    circle.hall?.kanji,
    circle.space,
    circle.name,
    circle.writer,
    circle.twitter.toString(),
    circle.memo,
    circle.price.toString(),
    circle.assign
)

private fun DrawScope.drawCellBackground(
    left: Float,
    top: Float,
    w: Float,
    h: Float
) {
    drawRect(
        color = Color(0xFFE0E0E0),
        topLeft = Offset(left, top),
        size = Size(w, h)
    )
}

private fun DrawScope.drawCellBorder(
    left: Float,
    top: Float,
    w: Float,
    h: Float
) {
    drawRect(
        color = Color.Gray,
        topLeft = Offset(left, top),
        size = Size(w, h),
        style = Stroke(1.5f)
    )
}

private fun DrawScope.drawCellText(
    text: String?,
    left: Float,
    top: Float,
    cellHeight: Float
) {
    drawContext.canvas.nativeCanvas.apply {
        val textSizePx = with(LocalDensity) { 14.sp.toPx() }
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = textSizePx
            isAntiAlias = true
        }
        val textY = top + cellHeight * 0.8f
        drawText(text?: "", left +4f, textY, paint)
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

private val columnWidthsDp = listOf(
    25, 60, 30, 30, 50, 100, 100, 72, 300, 60, 60
)





