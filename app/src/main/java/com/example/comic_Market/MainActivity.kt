package com.example.comic_Market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomAppBarState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comic_Market.ui.theme.ComikeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.TextStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComikeTheme {
                ComicMarketAppContainer()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicMarketAppContainer(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            appViewModel.updateCurrentRoute(destination.route ?: "")
        }
    }
    ComicMarketAppContent(navController = navController, appViewModel)
}


@Composable
fun ComicMarketAppContent(navController: NavHostController, appViewModel: AppViewModel) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "map",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("table") { TableScreenContainer(appViewModel) }
            composable("map") { MapScreen(appViewModel,onNavigateToTable = { navController.navigate("table") }) }
        }
    }

}

@Composable
fun BottomBar(navController: NavHostController) {
    BottomAppBar(
        Modifier.height(50.dp)
    ) {
        Row( modifier = Modifier.fillMaxWidth()) {
            BarItem("table") {
                navController.navigate("table")
            }
            BarItem("map") {
                navController.navigate("map")
            }
        }
    }
}

@Composable
fun BarItem(text: String, onClick: () -> Unit) {
    val appViewModel: AppViewModel = viewModel()
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width((30 + measureTextWidthInDp(text)).dp)
            .let { m ->
                if (appViewModel.currentRoute.collectAsState().value == text) m.background(Color.Cyan, shape = CircleShape)
                else m
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center // ← ここで上下中央に配置
    ) {
        Text(
            text = text
        )
    }
}


@Composable
fun measureTextWidthInDp(text: String): Float {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        constraints = Constraints(),
    )
    return with(density) { textLayoutResult.size.width.toDp().value }
}
