package com.example.comic_Market

import androidx.annotation.RestrictTo
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("AppViewModel not provided")
}
class AppViewModel : ViewModel() {
    private val _currentRoute = MutableStateFlow("map")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()
    fun updateCurrentRoute(newRoute: String) {
        _currentRoute.value = newRoute
    }

    private val _tableOffset = Animatable(Offset(0f,0f), Offset.VectorConverter)
    val tableOffset: State<Offset>
        get() = _tableOffset.asState()
    fun snapTableOffset(to: Offset) {
        viewModelScope.launch{
            _tableOffset.snapTo(to)
        }
    }
    suspend fun animateTableOffsetDecay(velocity: Offset) {
        _tableOffset.animateDecay(velocity, animationSpec = exponentialDecay(0.25f))
    }


    val topBarHeight = mutableStateOf(0.dp)
    fun moveTopBarBy(dy: Dp) {
        topBarHeight.value = (topBarHeight.value + dy).coerceIn(-50.dp, 0.dp)
    }

    val zoomScale = mutableFloatStateOf(1f)
    fun changeScale(newScale: Float) {
        zoomScale.value = min(10f,max(0.5f,newScale))
    }

}