package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Theming & Colors ---
// Extracted magic colors into constants for easier maintenance and readability.
private val ActiveColor = Color(0xFF386B21)    // Green highlight for selected states
private val InactiveColor = Color(0xFF42473D)  // Dark gray for unselected states
private val BackgroundColor = Color(0xFFE8E8DE) // Light beige for the bottom bar base

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        // Preview defaults to the Home tab
        CustomBottomBar(currentDestination = AppDestinations.HOME, onNavigate = {})
    }
}

/**
 * A custom bottom navigation bar featuring a floating center action button.
 *
 * @param currentDestination The currently active tab, used to highlight the correct icon.
 * @param onNavigate Callback function triggered when a navigation item is clicked.
 * @param modifier Optional modifier for adjusting the layout from the parent.
 */
@Composable
fun CustomBottomBar(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp), // Total height including the overflow of the floating button
        contentAlignment = Alignment.BottomCenter
    ) {

        // ---------------- THE MAIN BACKGROUND ROW ----------------
        // This row serves as the curved base of the bottom navigation bar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(BackgroundColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- Left Item: Map ---
            BottomBarItem(
                title = "Map",
                icon = Icons.Default.Place,
                isSelected = currentDestination == AppDestinations.MAP,
                onClick = { onNavigate(AppDestinations.MAP) },
                modifier = Modifier.weight(1f)
            )

            // --- Invisible Spacer ---
            // Creates the required empty space in the middle so the floating button isn't covering anything.
            Spacer(modifier = Modifier.width(80.dp))

            // --- Right Item: Scores ---
            BottomBarItem(
                title = "Scores",
                icon = Icons.Default.List,
                isSelected = currentDestination == AppDestinations.SCORES,
                onClick = { onNavigate(AppDestinations.SCORES) },
                modifier = Modifier.weight(1f)
            )
        }

        // ---------------- THE FLOATING CENTER BUTTON (Home/Search) ----------------
        // Placed at TopCenter of the Box so it slightly overlaps the main row below it.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(68.dp)
                .shadow(elevation = 15.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(ActiveColor)
                .clickable { onNavigate(AppDestinations.HOME) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search / Home",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

/**
 * A reusable composable for standard navigation items in the bottom bar.
 * This prevents duplicating the Column, Icon, and Text logic for every tab.
 */
@Composable
private fun BottomBarItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine the color based on whether this tab is currently active.
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
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}