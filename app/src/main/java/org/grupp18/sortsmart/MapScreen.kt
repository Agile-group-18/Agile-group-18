package org.grupp18.sortsmart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.jsoup.Jsoup
import androidx.lifecycle.viewmodel.compose.viewModel
import org.grupp18.sortsmart.viewmodel.MapViewModel
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder

// --- THEME & CONSTANTS ---
private val PrimaryGreen = Color(0xFF386B21)
private val DarkText = Color(0xFF1A1C17)
private val LightText = Color(0xFF42473D)
private val BadgeRed = Color(0xFFFA2B35)

// --- DATA MODELS ---

data class WasteCategory(
    val name: String,
    val iconUrl: String?
)

// 2. Update your RecyclingStation to use the new WasteCategory list
data class RecyclingStation(
    val name: String,
    val location: LatLng,
    val externalId: String,
    val municipalityCode: String,
    val acceptedCategories: List<WasteCategory>,
    val fullBins: List<String> = emptyList()
)

/**
 * Represents a specific waste fraction/category accepted at a station.
 */
data class FractionItem(
    val name: String,
    val iconUrl: String
)

// --- MAIN MAP SCREEN ---

/**
 * The primary screen displaying the Google Map, filter categories, and station markers.
 */




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel() // 1. Inject the ViewModel
) {
    val context = LocalContext.current
    val categories = listOf("All", "Plastic", "Paper", "Glass", "Metal", "Hazardous")
    var selectedCategory by remember { mutableStateOf("All") }

    // UI States
    var selectedStationId by remember { mutableStateOf<String?>(null) }
    var isLegendExpanded by remember { mutableStateOf(false) }

    // 2. Observe the state from the ViewModel
    val allStations by viewModel.stations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 3. Filter stations based on the selected category
    val filteredStations = remember(selectedCategory, allStations) {
        if (selectedCategory == "All") allStations
        else allStations.filter { station ->
            station.acceptedCategories.any { category -> category.name == selectedCategory }
        }
    }

    // 4. Camera & GPS Configuration (Keep this exactly as you had it!)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val mapProperties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    val gothenburg = LatLng(57.708870, 11.974560)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(gothenburg, 12f)
    }

    // Fetch user location if permission is granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude), 13f
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Ignore gracefully if permissions are revoked mid-flight
            }
        }
    }

    // 5. Local Data Update Function
    // We now just pass this intent to the ViewModel!
    val handleReportSubmission: (String, List<String>) -> Unit = { id, reportedBins ->
        viewModel.updateStationReport(id, reportedBins)
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            filteredStations.forEach { station ->
                // Pin color changes based on fullBins content
                val iconResId = if (station.fullBins.isNotEmpty()) {
                    R.drawable.ic_station_full
                } else {
                    R.drawable.ic_station_functional
                }

                Marker(
                    state = MarkerState(position = station.location),
                    title = station.name,
                    icon = bitmapDescriptorFromVector(context, iconResId),
                    onClick = {
                        selectedStationId = station.externalId
                        true
                    }
                )
            }
        }

        // Top Category Filter
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryGreen else Color.White)
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(category, color = if (isSelected) Color.White else DarkText, fontSize = 13.sp)
                }
            }
        }

        // Collapsible Map Legend (Aligned above zoom buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 130.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (isLegendExpanded) {
                Column(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Map Legend", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    LegendRow(R.drawable.ic_station_functional, "Functional")
                    LegendRow(R.drawable.ic_station_full, "Needs Attention")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Toggle Legend",
                tint = DarkText,
                modifier = Modifier.size(36.dp).clickable { isLegendExpanded = !isLegendExpanded }
            )
        }

        if (isLoading) {
            Box(Modifier.align(Alignment.Center).background(Color.White, CircleShape).padding(16.dp)) {
                Text("Loading stations...")
            }
        }
    }

    // 5. Bottom Sheet for Details & Reporting
    val activeStation = allStations.find { it.externalId == selectedStationId }
    if (activeStation != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedStationId = null },
            containerColor = Color.White
        ) {
            StationDetailView(
                station = activeStation,
                onReportSent = { reportedBins ->
                    handleReportSubmission(activeStation.externalId, reportedBins)
                }
            )
        }
    }
}

/**
 * Helper row for the map legend.
 */
@Composable
fun LegendRow(iconId: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(id = iconId), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = DarkText)
    }
}

// --- SCRAPING & DETAIL VIEW (SOPOR.NU) ---

/**
 * Displays details and the reporting form.
 */
@Composable
fun StationDetailView(
    station: RecyclingStation,
    onReportSent: (List<String>) -> Unit
) {
    // Reporting States
    var isReporting by remember { mutableStateOf(false) }
    var selectedFullBins by remember { mutableStateOf(setOf<String>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Restore the SVG Image Loader
    val context = LocalContext.current
    val svgImageLoader = remember {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(station.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(16.dp))

        if (showSuccessMessage) {
            Box(Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(16.dp)) {
                Text("Report submitted!", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isReporting) {
            Text("Which bins are full?", fontWeight = FontWeight.SemiBold, color = DarkText)

            station.acceptedCategories.forEach { category ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        selectedFullBins = if (selectedFullBins.contains(category.name)) {
                            selectedFullBins - category.name
                        } else {
                            selectedFullBins + category.name
                        }
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedFullBins.contains(category.name), onCheckedChange = null)
                    Text(category.name, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { isReporting = false }) { Text("Cancel") }
                Button(
                    onClick = {
                        onReportSent(selectedFullBins.toList())
                        isReporting = false
                        showSuccessMessage = true
                        selectedFullBins = emptySet<String>()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("Submit Report") }
            }
        } else {
            if (station.fullBins.isNotEmpty()) {
                Text("⚠️ Reported Full:", color = BadgeRed, fontWeight = FontWeight.Bold)
                station.fullBins.forEach { Text("• $it", color = BadgeRed, modifier = Modifier.padding(start = 8.dp)) }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Accepts waste:", fontWeight = FontWeight.SemiBold, color = LightText)

            station.acceptedCategories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    // Use the image if it exists, otherwise fallback to the checkmark
                    if (category.iconUrl != null) {
                        SubcomposeAsyncImage(
                            model = category.iconUrl,
                            imageLoader = svgImageLoader,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            error = { Icon(Icons.Default.CheckCircle, null, tint = PrimaryGreen) }
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(category.name, fontSize = 15.sp, color = DarkText)
                }
            }

            Button(
                onClick = { isReporting = true; showSuccessMessage = false },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Report Status")
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

// --- UTILITIES ---

/**
 * Converts vector to BitmapDescriptor.
 */
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}