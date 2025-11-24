package com.example.comic_Market

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TableScreen() {
    val scrollState = rememberScrollState()
    var lastScroll by remember { mutableStateOf(0) }
    val threshold = 100
    var headerVisible by remember { mutableStateOf(true) }
    var footerVisible by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        TableContent(scrollState)
        TableHeader(isVisible = headerVisible)
        TableFooter(isVisible = footerVisible)

        LaunchedEffect(scrollState.value) {
            val delta = scrollState.value - lastScroll

            if (delta > threshold) { // 下方向スクロールで消す
                headerVisible = false
                footerVisible = false
                lastScroll = scrollState.value
            } else if (delta < -threshold) { // 上方向スクロールで表示
                headerVisible = true
                footerVisible = true
                lastScroll = scrollState.value
            }
        }
    }
}

//fun createEmptyTable(rowCount: Int, colCount: Int): List<MutableList<String>> {
//    return List(rowCount) {
//        mutableStateListOf(*Array(colCount) { "" })
//    }
//}

@Composable
fun TableContent(scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .background(Color.White)
    ) {
        repeat(50) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(if (index % 2 == 0) Color(0xFFE0E0E0) else Color(0xFFF5F5F5)), // 交互に色を付けて見やすく
                contentAlignment = Alignment.Center
            ) {
                Text("Row $index | Scroll: ${scrollState.value}")
            }
        }
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

//val rowCount = 300
//val colCount = 12


