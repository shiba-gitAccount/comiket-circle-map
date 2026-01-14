package com.example.comic_Market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomAppBarState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
        bottomBar = { BottomBar(navController = navController, appViewModel) }
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
fun BottomBar(navController: NavHostController, appVM: AppViewModel) {
    BottomAppBar(
        Modifier.height(50.dp)
    ) {
        if(appVM.activeEdit.value) {
            Row(
                modifier = Modifier.background(color = Color.White, shape = CircleShape).border(border = BorderStroke(width = 2.dp, color = Color.Yellow), shape = CircleShape),
            ){
                BasicTextField(
                    value = appVM.cellText(),
                    modifier = Modifier.fillMaxHeight().width(300.dp),
                    onValueChange = { appVM.updateSelectedCellText(it) },
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(start = 20.dp),
                            contentAlignment = Alignment.CenterStart,
                        ){
                            innerTextField()
                        }
                    }
                )
                Checkbox(
                    
                    checked = true,
                    onCheckedChange = {
                        appVM.editBar(false)
                        appVM.selectedCell.value = null
                    },
                    colors = CheckboxDefaults.colors(Color.Yellow),
                    modifier = Modifier
                        .scale(0.8f)
                        .background(color = Color.Yellow, shape = CircleShape)
                )
            }

        }
        else {
            writeNav(navController)
        }
    }
}

@Composable
fun writeNav(navController: NavHostController) {
    Row( modifier = Modifier.fillMaxWidth()) {
        BarItem("table") {
            navController.navigate("table")
        }
        BarItem("map") {
            navController.navigate("map")
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
                if (appViewModel.currentRoute.collectAsState().value == text) m.background(
                    Color.Cyan,
                    shape = CircleShape
                )
                else m
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
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

fun updateCircle(
    circles: List<Circle>,
    row: Int,
    column: Column,
    newValue: String
): List<Circle> =
    circles.mapIndexed { index, circle ->
        if (index != row) return@mapIndexed circle

        when (column) {
            Column.NAME -> circle.copy(name = newValue)
            Column.MEMO -> circle.copy(memo = newValue)
            Column.SPACE -> circle.copy(space = newValue)
            Column.PRICE -> circle.copy(price = newValue.toIntOrNull())
            else -> circle
        }
    }
