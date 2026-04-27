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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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
    SCORES
}

/**
 * The root composable for the application.
 * Manages top-level state, navigation routing, and the main Scaffold structure.
 */
@Composable
fun SortSmartApp() {
    // Track the currently active tab.
    // rememberSaveable ensures the state survives configuration changes (like screen rotations).
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header() // The custom top bar we built previously
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

        // The main content area. We apply innerPadding to ensure content
        // isn't hidden behind the custom Header or BottomBar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Route to the correct screen based on the selected destination
            when (currentDestination) {
                AppDestinations.HOME -> {
                    // The default Android greeting placeholder
                    Greeting(name = "Android")
                }
                AppDestinations.MAP -> {
                    // Assumes MapScreen is built in a separate file
                    MapScreen(modifier = Modifier.fillMaxSize())
                }
                AppDestinations.SCORES -> {
                    // Placeholder for your future Scores screen
                    ScoresScreen()
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

/**
 * A temporary placeholder for the Scores screen content.
 * * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun ScoresScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "Scores View goes here!", fontSize = 24.sp)
    }
}