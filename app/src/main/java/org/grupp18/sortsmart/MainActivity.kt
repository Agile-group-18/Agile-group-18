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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.frontend.loggin.AuthViewModel
import org.grupp18.sortsmart.frontend.loggin.ProfileScreen
import org.grupp18.sortsmart.frontend.loggin.RetrofitClient
import org.grupp18.sortsmart.ui.map.MapScreen
import org.grupp18.sortsmart.ui.navigation.Basket
import org.grupp18.sortsmart.ui.navigation.Home
import org.grupp18.sortsmart.ui.navigation.Map
import org.grupp18.sortsmart.ui.navigation.Profile
import org.grupp18.sortsmart.ui.navigation.Scores
import org.grupp18.sortsmart.ui.screen.HomeScreen
import org.grupp18.sortsmart.ui.screen.ScoresScreen
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

class MainActivity : ComponentActivity() {

    companion object {
        var pendingResetToken: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RetrofitClient.init(this)

        pendingResetToken = intent?.data?.getQueryParameter("token")

        setContent {
            SortSmartTheme {
                SortSmartApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        pendingResetToken = intent.data?.getQueryParameter("token")
        recreate()
    }
}

@Composable
fun SortSmartApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = currentBackStackEntry?.destination

    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var triggerLoginFromHeader by rememberSaveable { mutableStateOf(false) }
    var showSearchScreen by rememberSaveable { mutableStateOf(false) }

    val wasteBasket = remember {
        mutableStateListOf<ItemDetail>()
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),

        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Header(
                currentDestination = currentDestination,
                onLogoClick = {
                    showSearchScreen = false
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    showSearchScreen = false
                    navController.navigate(Profile) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isLoggedIn = isLoggedIn,
                onLoginClick = { triggerLoginFromHeader = true }
            )
        },
        bottomBar = {
            CustomBottomBar(
                isMapSelected = !showSearchScreen &&
                        currentDestination?.hierarchy?.any { it.hasRoute(Map::class) } == true,
                isBasketSelected = !showSearchScreen &&
                        currentDestination?.hierarchy?.any { it.hasRoute(Basket::class) } == true,
                isScoresSelected = !showSearchScreen &&
                        currentDestination?.hierarchy?.any { it.hasRoute(Scores::class) } == true,
                onMapClick = {
                    showSearchScreen = false
                    navigateToTopLevel(navController, Map)
                },
                onBasketClick = {
                    showSearchScreen = false
                    navigateToTopLevel(navController, Basket)
                },
                onScoresClick = {
                    showSearchScreen = false
                    navigateToTopLevel(navController, Scores)
                },
                onSearchClick = {
                    showSearchScreen = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding()
            )
        ) {
            if (showSearchScreen) {
                SearchScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onAddToBasket = { item ->
                        if (!wasteBasket.contains(item)) {
                            wasteBasket.add(item)

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${item.name} added to basket"
                                )
                            }

                        }
                    },
                    onClose = {
                        showSearchScreen = false
                    }
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = if (MainActivity.pendingResetToken != null) Profile else Home,
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
                    composable<Home> { HomeScreen() }
                    composable<Map> { MapScreen() }
                    composable<Basket> { WasteBasketScreen(
                        items = wasteBasket,
                        onDiscard = { item ->
                            wasteBasket.remove(item)
                        },
                        onShowRoute = {
                            showSearchScreen = false
                            navigateToTopLevel(navController, Map)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) }
                    composable<Scores> { ScoresScreen() }
                    composable<Profile> {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            resetToken = MainActivity.pendingResetToken,
                            triggerLogin = triggerLoginFromHeader,
                            onLoginTriggered = { triggerLoginFromHeader = false }
                        )
                    }
                }
            }
        }
    }
}

private fun navigateToTopLevel(
    navController: NavHostController,
    route: Any
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}