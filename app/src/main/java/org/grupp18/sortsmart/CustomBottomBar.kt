package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
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

// Color tokens used in the bottom bar
private val ActiveColor = Color(0xFF386B21)
private val InactiveColor = Color(0xFF42473D)
private val BackgroundColor = Color(0xFFE8E8DE)

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        // Use boolean-based preview to match existing call sites in MainActivity
        CompactBottomBarPreview()
    }
}

@Composable
private fun CompactBottomBarPreview() {
    CustomBottomBar(
        isMapSelected = true,
        isScoresSelected = false,
        onMapClick = {},
        onScoresClick = {},
        onSearchClick = {}
    )
}

/**
 * Custom bottom bar compatible with existing calls from `MainActivity`.
 *
 * Keep the boolean selection API to avoid changing navigation call sites.
 */
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
            .height(96.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background curved bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BackgroundColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Map item
            BottomBarItem(
                title = "Map",
                icon = Icons.Filled.Place,
                isSelected = isMapSelected,
                onClick = onMapClick,
                modifier = Modifier.weight(1f)
            )

            // Spacer for floating action
            Spacer(modifier = Modifier.width(80.dp))

            // Scores item
            BottomBarItem(
                title = "Scores",
                icon = Icons.AutoMirrored.Filled.List,
                isSelected = isScoresSelected,
                onClick = onScoresClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Floating center action (Home / Search)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(68.dp)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(ActiveColor)
                .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) ActiveColor else InactiveColor

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}