package org.grupp18.sortsmart.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.grupp18.sortsmart.data.api.dto.StationDetailDto
import org.grupp18.sortsmart.viewmodel.DailyTipState
import org.grupp18.sortsmart.viewmodel.HomeViewModel
import org.grupp18.sortsmart.viewmodel.NearestStationState
import org.grupp18.sortsmart.viewmodel.ProfileViewModel
import org.grupp18.sortsmart.viewmodel.state.ProfileState
import androidx.core.net.toUri

private val GreenDark = Color(0xFF2D5A1B)
private val GreenMedium = Color(0xFF386B21)
private val GreenLight = Color(0xFFD4E8C2)
private val GreenLinkText = Color(0xFF3A6E24)
private val NeutralSurface = Color(0xFFF5F5F0)
private val NeutralBg = Color(0xFFF8F8F4)
private val DividerOnGreen = Color(0xFF4A7A30)
private val TextOnGreen = Color(0xFFFFFFFF)
private val TextOnGreenMuted = Color(0xFFB8D4A0)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    profileViewModel: ProfileViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(LocalContext.current))
) {
    val context = LocalContext.current
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val nearestStationState by homeViewModel.nearestStation.collectAsStateWithLifecycle()
    val dailyTipState by homeViewModel.dailyTip.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) homeViewModel.loadNearestStation(context)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) profileViewModel.loadProfile()
    }

    LaunchedEffect(Unit) {
        homeViewModel.loadRandomTip()
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val displayName = when (val s = profileState) {
        is ProfileState.Loaded -> s.profile.displayName?.takeIf { it.isNotBlank() }
            ?: s.profile.username

        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hi, ${displayName ?: "there"}!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C18),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        ActivityCard(profileState = profileState)

        when (val s = dailyTipState) {
            is DailyTipState.Loading -> EcoTipCardShimmer()
            is DailyTipState.Ready -> EcoTipCard(tip = s.tip)
            else -> {}
        }

        when (val s = nearestStationState) {
            is NearestStationState.Loading -> NearestStationCardShimmer()
            is NearestStationState.Ready -> s.station?.let { NearestStationCard(station = it) }
            else -> {}
        }
    }
}

@Composable
private fun ActivityCard(profileState: ProfileState) {
    val profile = (profileState as? ProfileState.Loaded)?.profile

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GreenDark)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text = "Your Activity",
                fontSize = 14.sp,
                color = TextOnGreenMuted,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reports
                Column {
                    Text(
                        text = profile?.reportCount?.toString() ?: "—",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGreen,
                        lineHeight = 52.sp
                    )
                    Text(
                        text = "REPORTS SUBMITTED",
                        fontSize = 11.sp,
                        color = TextOnGreenMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Verified badge
                if (profile?.isVerified == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x334A7A30))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✓ Verified",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnGreen
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = DividerOnGreen, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            // Member since
            val memberSince = profile?.createdAt?.let { raw ->
                runCatching {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.ENGLISH)
                    val date = sdf.parse(raw)!!
                    val out = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.ENGLISH)
                    out.format(date)
                }.getOrNull()
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "MEMBER SINCE",
                        fontSize = 11.sp,
                        color = TextOnGreenMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = memberSince ?: "—",
                        fontSize = 18.sp,
                        color = TextOnGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EcoTipCard(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GreenLight)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = GreenMedium,
                modifier = Modifier.size(26.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Daily Eco-Tip",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C17)
            )
            Text(
                text = tip,
                fontSize = 14.sp,
                color = Color(0xFF3A3D35),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun EcoTipCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GreenLight.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = GreenMedium,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@SuppressLint("UseKtx")
@Composable
private fun NearestStationCard(station: StationDetailDto) {
    val context = LocalContext.current

    val distanceText = station.distanceKm?.let {
        if (it < 1.0) "${"%.0f".format(it * 1000)} m away"
        else "${"%.1f".format(it)} km away"
    }

    val mapsUri = remember(station.latitude, station.longitude) {
        "geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}(${
            android.net.Uri.encode(
                station.name
            )
        })".toUri()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NeutralSurface)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GreenMedium),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Nearest Station",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C17)
            )
            Text(
                text = buildString {
                    append(station.name)
                    if (distanceText != null) append(" is $distanceText")
                    station.address?.let { append(" · $it") }
                        ?: append(" · ${station.municipality}")
                },
                fontSize = 14.sp,
                color = Color(0xFF3A3D35),
                lineHeight = 20.sp
            )
            station.openingHours?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Open: $it",
                    fontSize = 13.sp,
                    color = Color(0xFF5A5D55),
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        // Fall back to any app that handles geo: URIs if Maps isn't installed
                        val chooser = Intent.createChooser(
                            intent.takeIf {
                                it.resolveActivity(context.packageManager) != null
                            } ?: Intent(Intent.ACTION_VIEW, mapsUri),
                            null
                        )
                        context.startActivity(chooser)
                    }
            ) {
                Text(
                    text = "View on map",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenLinkText
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "›",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenLinkText
                )
            }
        }
    }
}

@Composable
private fun NearestStationCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NeutralSurface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = GreenMedium,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}