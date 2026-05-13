package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand colors ────────────────────────────────────────────────────────────
private val GreenDark = Color(0xFF2D5A1B)   // impact card background
private val GreenMedium = Color(0xFF386B21)   // location icon bg, badge bg
private val GreenLight = Color(0xFFD4E8C2)   // eco-tip card background
private val GreenLinkText = Color(0xFF3A6E24)   // "View on map" text
private val NeutralSurface = Color(0xFFF5F5F0)   // nearest-station card bg
private val NeutralBg = Color(0xFFF8F8F4)   // page background
private val DividerOnGreen = Color(0xFF4A7A30)   // divider inside the green card
private val TextOnGreen = Color(0xFFFFFFFF)
private val TextOnGreenMuted = Color(0xFFB8D4A0)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
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
            text = "Hi, Anon",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C18),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        ImpactCard()

        EcoTipCard()

        NearestStationCard()
    }
}

@Composable
private fun ImpactCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GreenDark)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column {
            // Label
            Text(
                text = "Your Impact",
                fontSize = 14.sp,
                color = TextOnGreenMuted,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold)) {
                            append("24.5")
                        }
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)) {
                            append("kg CO₂ saved")
                        }
                    },
                    color = TextOnGreen,
                    lineHeight = 52.sp
                )
                Text(
                    text = "↗",
                    fontSize = 48.sp,
                    color = GreenDark.copy(alpha = 0.0f).let { Color(0x554A7A30) },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(color = DividerOnGreen, thickness = 1.dp)

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "RECYCLED",
                        fontSize = 11.sp,
                        color = TextOnGreenMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "128 items",
                        fontSize = 18.sp,
                        color = TextOnGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vertical pipe divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .width(1.dp)
                        .height(36.dp)
                        .background(DividerOnGreen)
                )

                // Rank
                Column {
                    Text(
                        text = "RANK",
                        fontSize = 11.sp,
                        color = TextOnGreenMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Top 15%",
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
private fun EcoTipCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GreenLight)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // White pill icon
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
                text = "Rinse your plastics before throwing them in the blue bin to prevent contaminating the batch.",
                fontSize = 14.sp,
                color = Color(0xFF3A3D35),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun NearestStationCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NeutralSurface)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Green rounded-square location icon
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
                text = "Central Recycling Hub is 1.2km away and accepts all your items.",
                fontSize = 14.sp,
                color = Color(0xFF3A3D35),
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
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