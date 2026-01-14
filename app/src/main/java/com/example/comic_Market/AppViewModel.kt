package com.example.comic_Market

import androidx.annotation.RestrictTo
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.coerceIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import java.net.URL

val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("AppViewModel not provided")
}
class AppViewModel : ViewModel() {
    private val _currentRoute = MutableStateFlow("map")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()
    fun updateCurrentRoute(newRoute: String) {
        _currentRoute.value = newRoute
    }
    val activeEdit = mutableStateOf(false)
    fun editBar(boolean: Boolean){
        activeEdit.value = boolean
    }
    val boxSize = mutableStateOf(IntSize.Zero)
    val contentSize = mutableStateOf(Size(0f, 0f))
    private val _maxSize: State<Size> = derivedStateOf {
        Size(boxSize.value.width / zoomScale.value - contentSize.value.width, boxSize.value.height / zoomScale.value - contentSize.value.height)
    }
    private val _tableOffset = Animatable(Offset(0f,0f), Offset.VectorConverter)
    val tableOffset: State<Offset>
        get() = _tableOffset.asState()
    fun snapTableOffset(to: Offset) {
        viewModelScope.launch{
            _tableOffset.snapTo(clamp(to,_maxSize.value))
        }
    }
    suspend fun animateTableOffsetDecay(velocity: Offset) {
        _tableOffset.animateDecay(velocity, animationSpec = exponentialDecay(0.5f))
    }
    fun calcOffset(): IntOffset {
        val offX = minmax(
            _tableOffset.value.x,
            _maxSize.value.width
        )
        val offY = minmax(
            _tableOffset.value.y,
            _maxSize.value.height
        )
        return IntOffset(offX.toInt(), offY.toInt())
    }
    val topBarHeight = mutableStateOf(0.dp)
    fun moveTopBarBy(dy: Dp) {
        topBarHeight.value = (topBarHeight.value + dy).coerceIn(-50.dp, 0.dp)
    }

    val zoomScale = mutableFloatStateOf(1f)
    fun changeScale(newScale: Float) {
        zoomScale.value = min(5f,max(0.5f,newScale))
    }

    private val _circles = MutableStateFlow(generateDummyCircles())

    val circles = _circles.asStateFlow()
    val selectedCell = mutableStateOf<CellPos?>(null)
    fun cellText(): String {
        val cell = selectedCell.value ?: return ""
        val circle = circleAt(cell.row) ?: return ""
        val column = columnAt(cell.col) ?: return ""
        return circle.getValue(column)
    }
    private fun circleAt(row: Int): Circle? =
        _circles.value.getOrNull(row)

    private fun columnAt(col: Int): Column? =
        columns.getOrNull(col)

    fun updateSelectedCellText(newText: String) {
        val cell = selectedCell.value ?: return
        val column = columns.getOrNull(cell.col) ?: return

        _circles.update { list ->
            list.mapIndexed { index, circle ->
                if (index == cell.row) {
                    circle.withValue(column, newText)
                } else {
                    circle
                }
            }
        }
    }

    fun Circle.getValue(column: Column): String =
        when (column) {
            Column.CHECK   -> check.toString()
            Column.USER    -> user ?: ""
            Column.WEEK    -> week?.name ?: ""
            Column.HALL    -> hall?.name ?: ""
            Column.SPACE   -> space ?: ""
            Column.NAME    -> name ?: ""
            Column.WRITER  -> writer ?: ""
            Column.TWITTER -> twitter?.toString() ?: ""
            Column.MEMO    -> memo ?: ""
            Column.PRICE   -> price?.toString() ?: ""
            Column.ASSIGN  -> assign ?: ""
        }
    fun Circle.withValue(column: Column, newValue: String): Circle =
        when (column) {
            Column.CHECK   -> copy(check = newValue.toBoolean())
            Column.USER    -> copy(user = newValue)
            Column.WEEK    -> copy(week = Week.valueOf(newValue))
            Column.HALL    -> copy(hall = Hall.valueOf(newValue))
            Column.SPACE   -> copy(space = newValue)
            Column.NAME    -> copy(name = newValue)
            Column.WRITER  -> copy(writer = newValue)
            Column.TWITTER -> copy(twitter = runCatching { URL(newValue) }.getOrNull())
            Column.MEMO    -> copy(memo = newValue)
            Column.PRICE   -> copy(price = newValue.toIntOrNull())
            Column.ASSIGN  -> copy(assign = newValue)
        }
}

data class CellPos(val row: Int, val col: Int)