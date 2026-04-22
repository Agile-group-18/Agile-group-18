package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ActiveColor   = Color(0xFF386B21)
private val InactiveColor = Color(0xFF42473D)
private val BarBackground = Color(0xFFE8E8DE)

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        CustomBottomBar(
            isMapSelected = true,
            isScoresSelected = false,
            onMapClick = {},
            onScoresClick = {},
            onSearchClick = {}
        )
    }
}

@Composable
fun CustomBottomBar(
    isMapSelected: Boolean,
    isScoresSelected: Boolean,
    onMapClick: () -> Unit,
    onScoresClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── Background bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BarBackground),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Map tab
            NavBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "Map",
                        tint = if (isMapSelected) ActiveColor else InactiveColor,
                        modifier = Modifier.size(30.dp)
                    )
                },
                label = "Map",
                isSelected = isMapSelected,
                onClick = onMapClick,
                modifier = Modifier.weight(1f)
            )

            // Space for the floating center button
            Spacer(modifier = Modifier.width(72.dp))

            // Scores tab
            NavBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = "Scores",
                        tint = if (isScoresSelected) ActiveColor else InactiveColor,
                        modifier = Modifier.size(30.dp)
                    )
                },
                label = "Scores",
                isSelected = isScoresSelected,
                onClick = onScoresClick,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Floating center search/scan button ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y=0.dp)
                .size(62.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(ActiveColor)
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) ActiveColor else InactiveColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}