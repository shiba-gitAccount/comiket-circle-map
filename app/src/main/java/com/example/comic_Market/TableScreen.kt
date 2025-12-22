package com.example.comic_Market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

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
fun TopBar(topOffset: Int) {
    TopAppBar(
        title = { Text("Top Bar") },
        modifier = Modifier.height(50.dp).offset{ IntOffset(x = 0, y = topOffset) }
    )
}
fun calcTopOffset(heightPx: Float, appViewModel: AppViewModel): Int{
    return max(
        heightPx,
        min(0f,appViewModel.tableOffset.value.y * appViewModel.zoomScale.floatValue)
    ).toInt()
}

@Composable
fun TableContent(innerPadding: PaddingValues, topOffset: Int, appViewModel: AppViewModel) {
    val cfg = TableConfig()
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.padding(innerPadding)){
        val offset = appViewModel.calcOffset()
        DrawColTag(offset, topOffset, appViewModel)
        DrawRowNum(offset, appViewModel)
        Box(Modifier
            .background(Color.White)
            .fillMaxSize()
            .offset({IntOffset(x = (40.dp.toPx() * appViewModel.zoomScale.floatValue).toInt(),y = (25.dp.toPx() * appViewModel.zoomScale.floatValue).toInt())})
            .zIndex(0f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val pointers = awaitPointerEvent().changes.filter { it.pressed }
                        if (pointers.isEmpty()) continue
                        scrollAndZoom(pointers, appViewModel = appViewModel, scope = scope)
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
    Canvas(modifier = Modifier.zIndex(2f).offset{ IntOffset(x = 0, y = topOffset) }){ drawCell(left = 0f, top = 0f, width = 40.dp.toPx() * appViewModel.zoomScale.floatValue, height = 25.dp.toPx() * appViewModel.zoomScale.floatValue, text = "", backColor = Color.Gray, borderColor = Color.Black) }
    Canvas(modifier = Modifier.zIndex(1f).border(width = 2.dp,Color.Cyan).graphicsLayer(scaleX = appViewModel.zoomScale.floatValue, scaleY = appViewModel.zoomScale.floatValue, transformOrigin = TransformOrigin(0f, 0f)).offset({ IntOffset( x = offset.x + 40.dp.toPx().toInt(), y = (topOffset / appViewModel.zoomScale.floatValue).toInt() )})){
        val colWidthsPx = startDp.map { it.toPx() }
        tags.forEachIndexed { col,text ->
            drawCell(left = colWidthsPx[col], top = 0f, width = colWidthsPx[col+1] - colWidthsPx[col], height = 25.dp.toPx(), text = text, backColor = Color(0.80f, 0.93f, 0.97f, 1f), borderColor = Color.Gray)
        }
    }
}

@Composable
fun DrawRowNum(offset: IntOffset, appViewModel: AppViewModel) {
    Canvas(modifier = Modifier.border(width = 2.dp,Color.Cyan).graphicsLayer(scaleX = appViewModel.zoomScale.floatValue, scaleY = appViewModel.zoomScale.floatValue, transformOrigin = TransformOrigin(0f, 0f)).zIndex(1f).offset({ IntOffset( x = 0, y = offset.y + 25.dp.toPx().toInt()) })){
        val cellHeight = 25.dp.toPx()
        for(row in 0 until 300){
            drawCell(left = 0f, top = row * cellHeight, width = 40.dp.toPx(), height = 25.dp.toPx(), text = (row+1).toString(), Color(0.80f, 0.93f, 0.97f, 1f), borderColor = Color.Gray)
        }
    }
}



data class TableConfig(
    val rows: Int = 300,
    val cellHeight: Dp = 25.dp,
    val contentHeight: Dp = cellHeight * rows + 25.dp
)




@Composable
fun DrawTableCanvas(cfg: TableConfig, offset: IntOffset, appViewModel: AppViewModel = LocalAppViewModel.current) {
    val zoom = appViewModel.zoomScale.floatValue
    with(LocalDensity.current) { appViewModel.contentSize.value = Size((startDp[11]+40.dp).toPx(),cfg.contentHeight.toPx()) }
    Canvas(
        modifier = Modifier
            .graphicsLayer(
                scaleX = zoom,
                scaleY = zoom,
                transformOrigin = TransformOrigin(0f, 0f)
            )
            .onSizeChanged { appViewModel.boxSize.value = it }
            .offset { offset }
            .size(startDp[11], cfg.contentHeight)
    ) {
        drawTable(appViewModel)
    }
}





private fun DrawScope.drawTable(
    appViewModel: AppViewModel
) {
    val circles = appViewModel.circles.value
    val selected = appViewModel.selectedCell.value
    val colLeftPx = startDp.map { it.toPx() }
    val rowHeightPx = 25.dp.toPx()

    circles.forEachIndexed { rowIndex, circle ->
        drawCircleRow(circle, rowIndex, colLeftPx, rowHeightPx, selected)
    }
}

private fun DrawScope.drawCircleRow(
    circle: Circle,
    row: Int,
    colLeft: List<Float>,
    rowHeight: Float,
    selected: CellPos? = null
) {
    val top = row * rowHeight

    val values = circleToDisplayList(circle)

    values.forEachIndexed { col, text ->
        val isSelected = (row == selected?.row && col == selected.col)
        val (backColor,borderColor) = if (isSelected) Pair(Color.Cyan, Color.Cyan) else Pair(Color.White, Color.Gray)
        drawCell( colLeft[col], top, colLeft[col + 1] - colLeft[col], rowHeight, text, backColor, borderColor)
    }
}

private fun DrawScope.drawCell(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    text: String?,
    backColor: Color,
    borderColor: Color
){
    drawCellBackground(left, top, width, height, backColor)
    drawCellBorder(left, top, width, height, borderColor)
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

data class TableCell(
    val key: CircleField,     // どの項目か
    val display: String?,     // 画面に表示する文字
    val value: Any?           // 元の型（String, Int, URL, enum etc）
)

enum class CircleField { CHECK, USER, WEEK, HALL, SPACE, NAME, WRITER, TWITTER, MEMO, PRICE, ASSIGN, }

private fun DrawScope.drawCellBackground(
    left: Float,
    top: Float,
    w: Float,
    h: Float,
    backColor: Color
) {
    drawRect(
        color = backColor,
        topLeft = Offset(left, top),
        size = Size(w, h)
    )
}

private fun DrawScope.drawCellBorder(
    left: Float,
    top: Float,
    w: Float,
    h: Float,
    borderColor: Color
) {
    drawRect(
        color = borderColor,
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


suspend fun AwaitPointerEventScope.scrollAndZoom(initialPointers: List<PointerInputChange>, appViewModel: AppViewModel, scope: CoroutineScope) {
    val initialScale = appViewModel.zoomScale.floatValue; val initialPos = centerPosition(initialPointers)/ initialScale;
    var lastPos = initialPos; var lastTime = System.currentTimeMillis()
    var vx = 0f; var vy = 0f;
    var initialDistance: Float? = null; var prevPointerCount = 0;


    while (true) {
        val pointers = awaitPointerEvent().changes.filter { it.pressed }
        if (pointers.isEmpty()) break
        val nowPos = centerPosition(pointers = pointers)/ appViewModel.zoomScale.floatValue
        if ((nowPos - initialPos).getDistance() < 4) continue
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

        appViewModel.moveTopBarBy(frameDy.toDp() * appViewModel.zoomScale.floatValue)
        appViewModel.snapTableOffset(currentOffset)


        vx = computeVelocity(frameDx, dt)
        vy = computeVelocity(frameDy, dt)
        prevPointerCount = pointers.size
        lastPos = nowPos
        lastTime = nowTime
    }
    if ((lastPos - initialPos).getDistance() < 4){
        val tapPos = initialPos - appViewModel.calcOffset()
        appViewModel.selectedCell.value = CellPos((tapPos.y.toDp() / 25.dp).toInt(),tapCol(tapPos.x.toDp(), startDp))
        appViewModel.editBar(true)
    }else{
        scope.launch { appViewModel.animateTableOffsetDecay(Offset(vx, vy)) }
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



private val startDp = listOf(
    0.dp, 25.dp, 85.dp, 115.dp, 145.dp, 195.dp, 295.dp, 395.dp, 470.dp, 770.dp, 830.dp, 890.dp
)
fun tapCol(dpValue: Dp, startDp: List<Dp>): Int {
    val idx = startDp.binarySearch(dpValue)
    return when {
        idx >= 0 -> idx
        else -> -idx - 2
    }
}
operator fun Offset.minus(other: IntOffset): Offset {
    return Offset(this.x - other.x, this.y - other.y)
}




