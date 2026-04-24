package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.outlined.Notifications

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header()
}

@Composable
fun Header(
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(64.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Logo + Title ────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onLogoClick))  {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFA8D672)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 18.sp)  // TODO: swap with your actual leaf SVG/icon
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sort Smart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C17)
            )
        }

        // ── Bell + Avatar ───────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Bell with red dot
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF1A1C17)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFA2B35))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Profile avatar placeholder
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF888888))
            )
        }
    }
}