package org.grupp18.sortsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.grupp18.sortsmart.ui.map.MapScreen
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

/**
 * The main entry point for the Sort Smart application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables the app content to draw behind the system bars (status bar & navigation bar)
        enableEdgeToEdge()

        setContent {
            SortSmartTheme {
                SortSmartApp()
            }
        }
    }
}

/**
 * Defines the primary navigation tabs available in the application.
 */
enum class AppDestinations {
    MAP,
    HOME,
    BASKET,
    SEARCH
}

/**
 * The root composable for the application.
 * Manages top-level state, navigation routing, and the main Scaffold structure.
 */
@Composable
fun SortSmartApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val wasteBasket = remember {
        mutableStateListOf<ItemDetail>()
    }
    // Removed NavigationSuiteScaffold because you have a custom BottomBar now!
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header()
        },
        bottomBar = {
            CustomBottomBar(
                currentDestination = currentDestination,
                onNavigate = { newDestination ->
                    currentDestination = newDestination
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentDestination) {
                AppDestinations.HOME -> {
                    Greeting(
                        name = "Android",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                AppDestinations.MAP -> {
                    MapScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AppDestinations.BASKET -> {
                    WasteBasketScreen(
                        items = wasteBasket,
                        onDiscard = { item ->
                            wasteBasket.remove(item)
                        },
                        onShowRoute = {
                            currentDestination = AppDestinations.MAP
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                AppDestinations.SEARCH -> {
                    SearchScreen(
                        onClose = {
                            currentDestination = AppDestinations.HOME
                        },
                        onAddToBasket = { item ->
                            if (!wasteBasket.contains(item)) {
                                wasteBasket.add(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ---------------- PLACEHOLDER SCREENS ----------------


/**
 * A temporary placeholder for the Home screen content.
 * * @param name The name to display in the greeting.
 * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "Hello $name! This is the Home Screen.", fontSize = 20.sp)
    }
}