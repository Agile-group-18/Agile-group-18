package org.grupp18.sortsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.data.model.ItemDetail
import org.grupp18.sortsmart.ui.map.MapScreen
import org.grupp18.sortsmart.ui.navigation.Basket
import org.grupp18.sortsmart.ui.navigation.Home
import org.grupp18.sortsmart.ui.navigation.Map
import org.grupp18.sortsmart.ui.navigation.Profile
import org.grupp18.sortsmart.ui.navigation.Scores
import org.grupp18.sortsmart.ui.screen.CustomBottomBar
import org.grupp18.sortsmart.ui.screen.Header
import org.grupp18.sortsmart.ui.screen.HomeScreen
import org.grupp18.sortsmart.ui.screen.ProfileScreen
import org.grupp18.sortsmart.ui.screen.ScoresScreen
import org.grupp18.sortsmart.ui.screen.SearchScreen
import org.grupp18.sortsmart.ui.screen.WasteBasketScreen
import org.grupp18.sortsmart.ui.theme.SortSmartBg
import org.grupp18.sortsmart.ui.theme.SortSmartGreen
import org.grupp18.sortsmart.ui.theme.SortSmartTheme
import org.grupp18.sortsmart.viewmodel.AuthViewModel
import org.grupp18.sortsmart.viewmodel.HomeViewModel
import org.grupp18.sortsmart.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    private var _pendingResetToken = mutableStateOf<String?>(null)
    private var _pendingVerifyToken = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        handleDeepLink(intent)
        setContent {
            SortSmartTheme {
                SortSmartApp(
                    initialResetToken = _pendingResetToken.value,
                    initialVerifyToken = _pendingVerifyToken.value
                )
            }
        }
    }


    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: android.content.Intent?) {
        val data = intent?.data ?: return
        val token = data.getQueryParameter("token")
        when (data.path) {
            "/verify-email" -> {
                _pendingVerifyToken.value = token; _pendingResetToken.value = null
            }

            "/reset-password" -> {
                _pendingResetToken.value = token; _pendingVerifyToken.value = null
            }
        }
    }
}

@Composable
fun SortSmartApp(initialResetToken: String? = null, initialVerifyToken: String? = null) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = currentBackStackEntry?.destination

    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isRestoringSession by authViewModel.isRestoringSession.collectAsStateWithLifecycle()

    // Block all UI until the DB session check completes - prevents logged-out flash
    if (isRestoringSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SortSmartBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SortSmartGreen)
        }
        return
    }

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
                onSearchClick = { showSearchScreen = true }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            if (showSearchScreen) {
                SearchScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onClose = { showSearchScreen = false },
                    onAddToBasket = { item ->
                        if (!wasteBasket.contains(item)) {
                            wasteBasket.add(item)

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${item.name} added to basket"
                                )
                            }

                        }
                    }
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = if (initialResetToken != null || initialVerifyToken != null) Profile else Home,
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
                        val context = androidx.compose.ui.platform.LocalContext.current
                        HomeScreen(
                            isLoggedIn = isLoggedIn,
                            homeViewModel = viewModel(factory = HomeViewModel.Factory(context)),
                            profileViewModel = profileViewModel,
                        )
                    }
                    composable<Map> { MapScreen() }
                    composable<Basket> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val mapViewModel: org.grupp18.sortsmart.viewmodel.MapViewModel = viewModel(
                            factory = org.grupp18.sortsmart.viewmodel.MapViewModelFactory(context)
                        )

                        val isCalculating by mapViewModel.isCalculatingRoute.collectAsStateWithLifecycle()

                        WasteBasketScreen(
                            items = wasteBasket,
                            isCalculatingRoute = isCalculating,
                            onDiscard = { item ->
                                wasteBasket.remove(item)
                            },
                            onShowRouteFewestStops = {
                                showSearchScreen = false
                                mapViewModel.calculateAndShowRoute(
                                    context,
                                    wasteBasket,
                                    org.grupp18.sortsmart.RouteOptimizer.OptimizationStrategy.FEWEST_STOPS
                                )
                            },
                            onShowRouteShortest = {
                                showSearchScreen = false
                                mapViewModel.calculateAndShowRoute(
                                    context,
                                    wasteBasket,
                                    org.grupp18.sortsmart.RouteOptimizer.OptimizationStrategy.SHORTEST_DISTANCE
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                    composable<Scores> { ScoresScreen() }
                    composable<Profile> {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            profileViewModel = profileViewModel,
                            resetToken = initialResetToken,
                            verifyToken = initialVerifyToken,
                            triggerLogin = triggerLoginFromHeader,
                            onLoginTriggered = { triggerLoginFromHeader = false }
                        )
                    }
                }
            }
        }
    }
}

private fun navigateToTopLevel(navController: NavHostController, route: Any) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}