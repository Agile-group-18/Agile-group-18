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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

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

@PreviewScreenSizes
@Composable
fun SortSmartApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // memorize this as the state overalys everything!
    var showWastebasket by remember { mutableStateOf(false) }

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

        if (showWastebasket) {
            WastebasketSheet(
                onDismiss        = { showWastebasket = false },
                onAddMoreItems   = { showWastebasket = false /* TODO: navigate to scan */ },
                onStartRecycling = { showWastebasket = false /* TODO: navigate to map  */ }
            )
        }
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