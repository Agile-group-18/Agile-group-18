package org.grupp18.sortsmart.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import org.grupp18.sortsmart.R
import org.grupp18.sortsmart.data.model.RecyclingStationDetail
import org.grupp18.sortsmart.data.model.WasteCategory
import org.grupp18.sortsmart.viewmodel.MapViewModel
import org.grupp18.sortsmart.viewmodel.MapViewModelFactory
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard


private val PrimaryGreen = Color(0xFF386B21)
private val DarkText = Color(0xFF1A1C17)
private val LightText = Color(0xFF42473D)
private val BadgeRed = Color(0xFFFA2B35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val viewModelFactory = remember(context) {
        MapViewModelFactory(context)
    }

    val viewModel: MapViewModel = viewModel(
        factory = viewModelFactory
    )

    val stationMarkers by viewModel.stationMarkers.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val isLoadingMarkers by viewModel.isLoadingMarkers.collectAsState()
    val isSyncingMarkers by viewModel.isSyncingMarkers.collectAsState()
    val isLoadingStationDetail by viewModel.isLoadingStationDetail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    val problemOnly by viewModel.problemOnly.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    val activeFilterCount = selectedCategoryIds.size + if (problemOnly) 1 else 0

    var isLegendExpanded by remember { mutableStateOf(false) }

    val gothenburg = LatLng(57.708870, 11.974560)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(gothenburg, 12f)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapStyleOptions = remember(context) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            R.raw.map_style
        )
    }

    val mapProperties = remember(hasLocationPermission, mapStyleOptions) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = mapStyleOptions
        )
    }

    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        compassEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false
    )

    val clusterItems = remember(stationMarkers) {
        stationMarkers.map { station ->
            StationClusterItem(
                stationId = station.id,
                stationType = station.stationType,
                problemReport = station.hasProblemReport,
                itemPosition = station.location
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            13f
                        )
                    }
                }
            } catch (_: SecurityException) {
                // Permission was revoked while request was running.
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            val clusterManagerRef = remember {
                arrayOfNulls<ClusterManager<StationClusterItem>>(1)
            }

            MapEffect(Unit) { googleMap ->
                if (clusterManagerRef[0] != null) return@MapEffect

                val screenWidth = context.resources.displayMetrics.widthPixels
                val screenHeight = context.resources.displayMetrics.heightPixels

                val clusterManager = ClusterManager<StationClusterItem>(
                    context,
                    googleMap
                )

                clusterManager.algorithm = NonHierarchicalViewBasedAlgorithm(
                    screenWidth,
                    screenHeight
                )

                clusterManager.renderer = StationClusterRenderer(
                    context = context,
                    map = googleMap,
                    clusterManager = clusterManager
                )

                clusterManager.setAnimation(true)

                clusterManager.setOnClusterItemClickListener { item ->
                    viewModel.selectStation(item.stationId)
                    true
                }

                googleMap.setOnCameraIdleListener(clusterManager)
                googleMap.setOnMarkerClickListener(clusterManager)

                clusterManagerRef[0] = clusterManager
            }

            LaunchedEffect(clusterItems) {
                val clusterManager = clusterManagerRef[0] ?: return@LaunchedEffect

                clusterManager.clearItems()
                clusterManager.addItems(clusterItems)
                clusterManager.cluster()
            }
        }

        MapFilters(
            categories = categories,
            selectedCategoryIds = selectedCategoryIds,
            problemOnly = problemOnly,
            onCategoryToggle = { viewModel.toggleCategory(it) },
            onProblemOnlyToggle = { viewModel.setProblemOnly(it) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 96.dp + 16.dp,
                    end = 16.dp
                )
        )

        MapLegend(
            isExpanded = isLegendExpanded,
            onToggle = { isLegendExpanded = !isLegendExpanded },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp)
        )

        if (isLoadingMarkers && stationMarkers.isEmpty()) {
            LoadingCard(
                text = "Loading stations...",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isSyncingMarkers && stationMarkers.isNotEmpty()) {
            LoadingCard(
                text = "Refreshing stations...",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }

        errorMessage?.let { message ->
            ErrorCard(
                text = message,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            )
        }

        if (isLoadingStationDetail) {
            LoadingCard(
                text = "Loading station details...",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    selectedStation?.let { station ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.clearSelectedStation()
            },
            containerColor = Color.White
        ) {
            StationDetailSheet(
                station = station,
                onReportSent = { reportedCategories ->
                    viewModel.reportStationCategories(
                        // TODO: replace with real auth token location
                        accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjMjRhY2JiYS1lYWFhLTQwZmYtYWY3MC1lOWYwYWUxM2FjYjIiLCJ1c2VybmFtZSI6Ik4xc3k1R01hOEdPSkZRY3VnamxvMThKcGVKeDBoWVhqTUZRMnBlLWV6QkQyV05Xc1RQIiwiZXhwIjoxNzc4Nzg2NTE4fQ.dq1p26QoGJaIh8AKvOS7MIipkQfxA5Hggg0JQVPQaA0",
                        stationId = station.id,
                        categories = reportedCategories
                    )
                }
            )
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Filtrera stationer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedFilterChip(
                        selected = problemOnly,
                        onClick = { viewModel.setProblemOnly(!problemOnly) },
                        label = { Text("Needs attention", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = Color(0xFFFA2B35).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFFD32F2F),
                            selectedLeadingIconColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(50),
                        border = null
                    )

                    categories.forEach { category ->
                        val isSelected = selectedCategoryIds.contains(category.id)
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleCategory(category.id) },
                            label = { Text(category.name, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = Color(0xFF386B21).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF386B21),
                                selectedLeadingIconColor = Color(0xFF386B21),
                                labelColor = DarkText
                            ),
                            shape = RoundedCornerShape(50),
                            border = null
                        )
                    }
                }

                if (activeFilterCount > 0) {
                    TextButton(
                        onClick = {
                            viewModel.clearCategoryFilters()
                            viewModel.setProblemOnly(false)
                        },
                        modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
                    ) {
                        Text("Rensa filter", color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegend(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Map Legend",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                LegendRow(
                    iconId = R.drawable.ic_station_functional,
                    label = "Functional"
                )

                LegendRow(
                    iconId = R.drawable.ic_station_full,
                    label = "Needs attention"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Toggle legend",
            tint = DarkText,
            modifier = Modifier
                .size(36.dp)
                .clickable { onToggle() }
        )
    }
}

@Composable
private fun LegendRow(
    iconId: Int,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = DarkText
        )
    }
}

@Composable
private fun StationDetailSheet(
    station: RecyclingStationDetail,
    onReportSent: (List<WasteCategory>) -> Unit
) {
    var isReporting by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf<WasteCategory>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = station.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        StationInfo(station = station)

        Spacer(modifier = Modifier.height(16.dp))

        if (showSuccessMessage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Report submitted.",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isReporting) {
            Text(
                text = "Which categories need attention?",
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            station.acceptedCategories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedCategories = if (selectedCategories.contains(category)) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedCategories.contains(category),
                        onCheckedChange = null
                    )

                    Text(
                        text = category.name,
                        modifier = Modifier.padding(start = 8.dp),
                        color = DarkText
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        isReporting = false
                        selectedCategories = emptySet()
                    }
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onReportSent(selectedCategories.toList())
                        isReporting = false
                        showSuccessMessage = true
                        selectedCategories = emptySet()
                    },
                    enabled = selectedCategories.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Submit Report")
                }
            }
        } else {
            if (station.fullBins.isNotEmpty()) {
                Text(
                    text = "Reported issues:",
                    color = BadgeRed,
                    fontWeight = FontWeight.Bold
                )

                station.fullBins.forEach { bin ->
                    Text(
                        text = "• $bin",
                        color = BadgeRed,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Accepts waste:",
                fontWeight = FontWeight.SemiBold,
                color = LightText
            )

            Spacer(modifier = Modifier.height(8.dp))

            station.acceptedCategories.forEach { category ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    if (category.imageUrl != null) {
                        SubcomposeAsyncImage(
                            model = category.imageUrl,
                            imageLoader = svgImageLoader,
                            contentDescription = category.name,
                            modifier = Modifier.size(28.dp),
                            error = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = category.name,
                        fontSize = 15.sp,
                        color = DarkText
                    )
                }
            }

            Button(
                onClick = {
                    isReporting = true
                    showSuccessMessage = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Report Status")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun StationInfo(
    station: RecyclingStationDetail
) {
    Column {
        station.address?.let { address ->
            Text(
                text = address,
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        if (station.municipality.isNotBlank()) {
            Text(
                text = "Municipality: ${station.municipality}",
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        if (station.stationType.isNotBlank()) {
            Text(
                text = "Type: ${station.stationType}",
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        station.openingHours?.let { openingHours ->
            Text(
                text = "Opening hours: $openingHours",
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        station.operator?.let { operator ->
            Text(
                text = "Operator: $operator",
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        station.distanceKm?.let { distance ->
            Text(
                text = "Distance: %.1f km".format(distance),
                fontSize = 14.sp,
                color = LightText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = "Reports: ${station.reportCount}",
            fontSize = 14.sp,
            color = LightText
        )
    }
}

@Composable
private fun LoadingCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = PrimaryGreen
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = DarkText,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = BadgeRed,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MapFilters(
    categories: List<WasteCategory>,
    selectedCategoryIds: Set<Int>,
    problemOnly: Boolean,
    onCategoryToggle: (Int) -> Unit,
    onProblemOnlyToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val activeFilterCount = selectedCategoryIds.size + if (problemOnly) 1 else 0

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = Color.White,
            contentColor = PrimaryGreen,
            modifier = Modifier.size(48.dp)
        ) {
            if (activeFilterCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = BadgeRed,
                            contentColor = Color.White
                        ) {
                            Text(activeFilterCount.toString())
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Filter")
                }
            } else {
                Icon(imageVector = Icons.Default.List, contentDescription = "Filter")
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.85f),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                FlowRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    ElevatedFilterChip(
                        selected = problemOnly,
                        onClick = { onProblemOnlyToggle(!problemOnly) },
                        label = { Text("Needs attention", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = Color(0xFFFA2B35).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFFD32F2F),
                            selectedLeadingIconColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(50),
                        border = null
                    )

                    categories.forEach { category ->
                        val isSelected = selectedCategoryIds.contains(category.id)
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { onCategoryToggle(category.id) },
                            label = { Text(category.name, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = Color(0xFF386B21).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF386B21),
                                selectedLeadingIconColor = Color(0xFF386B21),
                                labelColor = DarkText
                            ),
                            shape = RoundedCornerShape(50),
                            border = null
                        )
                    }
                }
            }
        }
    }
}

data class StationClusterItem(
    val stationId: String,
    val stationType: String,
    val problemReport: Boolean,
    val itemPosition: LatLng
) : ClusterItem {

    override fun getPosition(): LatLng = itemPosition

    override fun getTitle(): String = stationType

    override fun getSnippet(): String? = null

    override fun getZIndex(): Float? = null
}