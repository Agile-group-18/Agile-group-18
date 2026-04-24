package org.grupp18.sortsmart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
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

// --- THEME & CONSTANTS ---
private val PrimaryGreen = Color(0xFF386B21)
private val DarkText = Color(0xFF1A1C17)
private val LightText = Color(0xFF42473D)
private val BadgeRed = Color(0xFFFA2B35)
private val FloatingMenuBackground = Color(0xFFFFDBB6)

// --- DATA MODELS ---

/**
 * Represents a recycling station's core data on the map.
 */
data class RecyclingStation(
    val name: String,
    val location: LatLng,
    val externalId: String,
    val municipalityCode: String,
    val acceptedCategories: List<String>
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
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val categories = listOf("All", "Plastic", "Paper", "Glass", "Metal", "Hazardous")
    var selectedCategory by remember { mutableStateOf("All") }

    // UI States
    var selectedStation by remember { mutableStateOf<RecyclingStation?>(null) }
    var allStations by remember { mutableStateOf<List<RecyclingStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 1. Fetch all stations from the Avfall Sverige API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getAllStations()
            allStations = response.avsList.mapNotNull { apiStation ->
                val latDouble = apiStation.lat?.toDoubleOrNull()
                val lngDouble = apiStation.longitude?.toDoubleOrNull()

                if (latDouble != null && lngDouble != null) {
                    RecyclingStation(
                        name = apiStation.name ?: "Unknown station",
                        location = LatLng(latDouble, lngDouble),
                        externalId = apiStation.externalAvsId ?: "",
                        municipalityCode = apiStation.municipalityCode ?: "",
                        acceptedCategories = listOf("Plastic", "Paper", "Glass", "Metal", "Hazardous") // Mocked for now
                    )
                } else null
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage}"
            isLoading = false
        }
    }

    // 2. Filter stations based on the selected category
    val filteredStations = remember(selectedCategory, allStations) {
        if (selectedCategory == "All") allStations
        else allStations.filter { it.acceptedCategories.contains(selectedCategory) }
    }

    // 3. Camera & GPS Configuration
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

    // 4. Lazy Loading (Only render markers visible on the screen for performance)
    var visibleStations by remember { mutableStateOf<List<RecyclingStation>>(emptyList()) }

    LaunchedEffect(cameraPositionState.isMoving, filteredStations) {
        if (!cameraPositionState.isMoving) {
            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
            if (bounds != null) {
                visibleStations = filteredStations.filter { bounds.contains(it.location) }
            } else {
                // Fallback: render up to 100 stations if bounds are unavailable
                visibleStations = filteredStations.take(100)
            }
        }
    }

    // 5. Build the Map and UI Overlay
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            val customIcon = remember(context) {
                bitmapDescriptorFromVector(context, R.drawable.ic_recycle_bin)
            }

            visibleStations.forEach { station ->
                Marker(
                    state = MarkerState(position = station.location),
                    title = station.name,
                    icon = customIcon,
                    onClick = {
                        selectedStation = station
                        true // Consume the click so the default info window doesn't pop up
                    }
                )
            }
        }

        // Top Category Filter Menu
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
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
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else DarkText
                    )
                }
            }
        }

        // Loading and Error States
        if (isLoading) {
            Box(Modifier.align(Alignment.Center).background(Color.White, CircleShape).padding(16.dp)) {
                Text("Loading stations from Avfall Sverige...")
            }
        }
        if (errorMessage != null) {
            Box(Modifier.align(Alignment.Center).background(Color.White, CircleShape).padding(16.dp)) {
                Text(errorMessage!!, color = Color.Red)
            }
        }

        // Floating Action Button (Map Layers)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(FloatingMenuBackground)
                .clickable { /* TODO: Implement Layer selection */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Layers, "Map Layers", tint = Color.Black, modifier = Modifier.size(24.dp))

            // Notification Badge
            Box(
                Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .offset((-4).dp, 4.dp)
                    .clip(CircleShape)
                    .background(BadgeRed)
            )
        }
    }

    // 6. Bottom Sheet Menu (Displays details for the selected station)
    if (selectedStation != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedStation = null },
            containerColor = Color.White
        ) {
            StationDetailView(station = selectedStation!!)
        }
    }
}

// --- SCRAPING & DETAIL VIEW (SOPOR.NU) ---

/**
 * Bottom sheet content that scrapes and displays exact waste fractions from sopor.nu.
 */
@Composable
fun StationDetailView(station: RecyclingStation) {
    var isLoading by remember { mutableStateOf(true) }
    var fractions by remember { mutableStateOf<List<FractionItem>>(emptyList()) }

    // A smart image loader configured to decode SVG files
    val context = LocalContext.current
    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    LaunchedEffect(station) {
        isLoading = true
        try {
            // Guard clause if essential IDs are missing
            if (station.externalId.isEmpty() || station.municipalityCode.isEmpty()) {
                fractions = listOf(FractionItem("Information missing", ""))
                isLoading = false
                return@LaunchedEffect
            }

            val response = SoporRetrofitClient.apiService.getStationDetails(
                externalId = station.externalId,
                municipalityCode = station.municipalityCode
            )

            val rawHtml = response.string()
            val document = Jsoup.parse(rawHtml)
            val parsedFractions = mutableListOf<FractionItem>()

            val liElements = document.select("li")
            for (li in liElements) {
                val images = li.select("img")
                if (images.isEmpty()) continue

                // Inject split markers directly into the HTML to map images to text
                for ((i, img) in images.withIndex()) {
                    img.after(" _SPLIT_${i}_ ")
                }

                val fullText = li.text()

                // Extract the specific text for each waste fraction
                for ((i, img) in images.withIndex()) {
                    val marker = "_SPLIT_${i}_"
                    val nextMarker = "_SPLIT_${i + 1}_"

                    val startIndex = fullText.indexOf(marker)
                    if (startIndex != -1) {
                        val start = startIndex + marker.length
                        var end = fullText.indexOf(nextMarker)
                        if (end == -1) end = fullText.length

                        // Clean up the extracted text block
                        // Note: Keeping Swedish keywords as they match exact website output
                        val fractionText = fullText.substring(start, end)
                            .replace("Felanmäl", "")
                            .replace(Regex("_SPLIT_[0-9]+_"), "") // Cleans up leftover markers
                            .trim()

                        if (fractionText.isNotEmpty() && fractionText.length < 60 && !fractionText.contains("Id:") && !fractionText.contains("Återvinningsstation")) {
                            var url = img.attr("src")
                            if (url.startsWith("/")) url = "https://www.sopor.nu$url"

                            // Ensure no duplicates are added
                            if (parsedFractions.none { it.name == fractionText }) {
                                parsedFractions.add(FractionItem(fractionText, url))
                            }
                        }
                    }
                }
            }

            fractions = if (parsedFractions.isEmpty()) {
                listOf(FractionItem("No waste categories found", ""))
            } else {
                parsedFractions
            }

        } catch (e: Exception) {
            fractions = listOf(FractionItem("Could not load data", ""))
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(station.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryGreen, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Fetching exact availability from Sopor.nu...", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            Text("Accepts the following waste:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = LightText)
            Spacer(modifier = Modifier.height(12.dp))

            fractions.forEach { fraction ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    if (fraction.iconUrl.isNotEmpty()) {
                        // Uses our custom svgImageLoader, falling back to a checkmark on error
                        SubcomposeAsyncImage(
                            model = fraction.iconUrl,
                            imageLoader = svgImageLoader,
                            contentDescription = fraction.name,
                            modifier = Modifier.size(28.dp),
                            loading = { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) },
                            error = { Icon(Icons.Default.CheckCircle, "Check", tint = PrimaryGreen, modifier = Modifier.size(24.dp)) }
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, "Check", tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = fraction.name,
                        fontSize = 15.sp,
                        color = DarkText
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// --- UTILITIES ---

/**
 * Converts a vector drawable resource into a Google Maps BitmapDescriptor.
 */
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}