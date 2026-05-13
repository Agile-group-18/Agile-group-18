package org.grupp18.sortsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.grupp18.sortsmart.ui.model.navigation.Home
import org.grupp18.sortsmart.ui.model.navigation.Map
import org.grupp18.sortsmart.ui.model.navigation.Scores
import org.grupp18.sortsmart.ui.screen.HomeScreen
import org.grupp18.sortsmart.ui.screen.MapScreen
import org.grupp18.sortsmart.ui.screen.ScoresScreen
import org.grupp18.sortsmart.ui.screen.WastebasketSheet
import androidx.compose.ui.unit.sp
import org.grupp18.sortsmart.ui.map.MapScreen
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

/**
 * The main entry point for the SortSmart application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Removed NavigationSuiteScaffold because you have a custom BottomBar now!
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0), // this is automaticaly handled via windowInsetsPadding()
        topBar = { Header(
            onLogoClick = {
                navController.navigate(Home) {
                    popUpTo<Home> { inclusive = false }
                    launchSingleTop = true
                }
            }
        ) },
        bottomBar = {
            CustomBottomBar(
                // Tell the bar which tab is active right now.
                // hierarchy.any handles nested graphs gracefully
                isMapSelected    = currentDestination?.hierarchy?.any { it.hasRoute(Map::class)    } == true,
                isScoresSelected = currentDestination?.hierarchy?.any { it.hasRoute(Scores::class) } == true,
                onMapClick    = { navController.navigateToTopLevel(navController, Map)    },
                onScoresClick = { navController.navigateToTopLevel(navController, Scores) },
                onSearchClick = { /* TODO: open search / scan screen */ }
            )
        }
    ) { innerPadding ->
        Box(    modifier = Modifier.padding(
            top = innerPadding.calculateTopPadding()
        )) {
            NavHost(
                navController = navController,
                startDestination = Home,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = EaseOutQuart),
                        initialOffsetX = { it }
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(300, easing = EaseOutQuart),
                        targetOffsetX = { -it / 3 }
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(300, easing = EaseOutQuart),
                        initialOffsetX = { -it / 3 }
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(300, easing = EaseOutQuart),
                        targetOffsetX = { it }
                    )
                }
            ) {
                composable<Home> {
                    HomeScreen(
                        onWastebasketClick = { showWastebasket = true }
                    )
                }
                composable<Map>    { MapScreen()    }
                composable<Scores> { ScoresScreen() }
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
 * Standard top-level tab navigation:
 * - popUpTo the graph's start so the back stack doesn't grow on every tab tap
 * - saveState/restoreState preserves scroll position and form state per tab
 * - launchSingleTop prevents duplicate copies of the same destination
 */
private fun NavHostController.navigateToTopLevel(navController: NavHostController, route: Any) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}