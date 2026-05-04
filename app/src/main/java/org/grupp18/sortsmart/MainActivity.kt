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
import org.grupp18.sortsmart.frontend.loggin.ProfileScreen
import org.grupp18.sortsmart.ui.map.MapScreen
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

/**
 * The main entry point for the Sort Smart application.
 */
class MainActivity : ComponentActivity() {

    companion object {
        // Stored here so it never triggers recomposition
        var pendingResetToken: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables the app content to draw behind the system bars (status bar & navigation bar)
        enableEdgeToEdge()

        // Extract reset token if the app was opened via a password reset deep link
        pendingResetToken = intent?.data?.getQueryParameter("token")

        setContent {
            SortSmartTheme {
                SortSmartApp()
            }
        }
    }

    // Handle deep link when app is already running
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        pendingResetToken = intent.data?.getQueryParameter("token")
        recreate()
    }
}

/**
 * Defines the primary navigation tabs available in the application.
 */
enum class AppDestinations {
    MAP,
    HOME,
    SCORES,
    PROFILE
}

/**
 * The root composable for the application.
 * Manages top-level state, navigation routing, and the main Scaffold structure.
 */
@Composable
fun SortSmartApp() {
    // Start on PROFILE tab if app was opened via reset link, otherwise HOME
    var currentDestination by rememberSaveable {
        mutableStateOf(
            if (MainActivity.pendingResetToken != null) AppDestinations.PROFILE
            else AppDestinations.HOME
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header(
                currentDestination = currentDestination,
                onNavigate = { newDestination ->
                    currentDestination = newDestination
                }
            )
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
                AppDestinations.SCORES -> {
                    ScoresScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                AppDestinations.PROFILE -> {
                    ProfileScreen(modifier = Modifier.fillMaxSize().padding(innerPadding), resetToken = MainActivity.pendingResetToken)
                }
            }
        }
    }
}

// ---------------- PLACEHOLDER SCREENS ----------------

/**
 * A temporary placeholder for the Home screen content.
 * @param name The name to display in the greeting.
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
 * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun ScoresScreen(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "Scores View goes here!", fontSize = 24.sp)
    }
}