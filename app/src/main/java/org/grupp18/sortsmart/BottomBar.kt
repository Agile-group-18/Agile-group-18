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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        CustomBottomBar()
    }
}

@Composable
fun CustomBottomBar(modifier: Modifier = Modifier) {
    // We use a Box as the main container so the big center button can "float"
    // slightly above the main background row.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp), // Total height including the floating button
        contentAlignment = Alignment.BottomCenter
    ) {

        // ---------------- THE MAIN BACKGROUND ----------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // The height of the actual beige bar
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFFE8E8DE)), // Beige color from your design
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- Left Button (Map) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* TODO: Navigate to Map */ },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Map",
                    tint = Color(0xFF42473D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Map", fontSize = 12.sp, color = Color(0xFF42473D), fontWeight = FontWeight.Medium)
            }

            // --- Invisible Spacer ---
            // Makes room for the big green button so it doesn't overlap the text
            Spacer(modifier = Modifier.width(80.dp))

            // --- Right Button (Scores) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* TODO: Navigate to Scores */ },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Scores",
                    tint = Color(0xFF42473D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Scores", fontSize = 12.sp, color = Color(0xFF42473D), fontWeight = FontWeight.Medium)
            }
        }

        // ---------------- THE FLOATING CENTER BUTTON ----------------
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter) // Pushes it to the very top of the 96dp Box
                .size(68.dp)
                .shadow(elevation = 15.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF386B21)) // Dark green from your design
                .clickable { /* TODO: Search Action */ },
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