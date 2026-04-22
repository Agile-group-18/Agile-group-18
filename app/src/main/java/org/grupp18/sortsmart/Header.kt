package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header()
}

@Composable
fun Header(modifier: Modifier = Modifier) {
    // The main container for the header
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 24.dp), // Natural padding on the left and right
        horizontalArrangement = Arrangement.SpaceBetween, // Pushes left and right sides apart
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ---------------- LEFT SIDE (Logo & Title) ----------------
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Placeholder Logo (Replace with your actual logo later)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Logo",
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sort Smart",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C17) // Hex color from your design
            )
        }

        // ---------------- RIGHT SIDE (Bell & Profile) ----------------
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Notification Bell with Red Dot
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                // The Bell Icon
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp)
                )

                // The Red Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd) // Sticks it to the top right
                        .offset(x = (-4).dp, y = 4.dp) // Tweaks the exact position
                        .clip(CircleShape)
                        .background(Color(0xFFFA2B35)) // Red color from your design
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Replace this Box with an Image later
            )
        }
    }
}