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

// --- Theming & Colors ---
// Extracted colors for consistency and easier updates
private val TitleTextColor = Color(0xFF1A1C17)
private val NotificationBadgeColor = Color(0xFFFA2B35)
private val PlaceholderColor = Color.LightGray
private val AvatarPlaceholderColor = Color.Gray

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header()
}

/**
 * The main top app bar (Header) for the application.
 * Contains the branding on the left and user actions on the right.
 *
 * @param modifier Optional modifier for adjusting the layout from the parent.
 */
@Composable
fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween, // Pushes left and right content to the edges
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left Side ---
        HeaderLogoAndTitle()

        // --- Right Side ---
        HeaderActions()
    }
}

/**
 * Composable for the left side of the header containing the logo and app name.
 */
@Composable
private fun HeaderLogoAndTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Placeholder Logo
        // TODO: Replace with an actual Image composable when the logo is ready
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "App Logo Placeholder",
                tint = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Sort Smart",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor
        )
    }
}

/**
 * Composable for the right side of the header containing user-specific actions.
 */
@Composable
private fun HeaderActions() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotificationBell(hasUnread = true)

        Spacer(modifier = Modifier.width(12.dp))

        ProfileAvatar()
    }
}

/**
 * A notification icon that conditionally displays a red unread badge.
 * * @param hasUnread Boolean dictating whether the red dot should be visible.
 */
@Composable
private fun NotificationBell(hasUnread: Boolean) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier.size(24.dp)
        )

        // The Red Unread Dot
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd) // Anchors to the top-right of the parent Box
                    .offset(x = (-4).dp, y = 4.dp) // Fine-tunes the exact position to sit on the bell
                    .clip(CircleShape)
                    .background(NotificationBadgeColor)
            )
        }
    }
}

/**
 * A placeholder for the user's profile picture.
 */
@Composable
private fun ProfileAvatar() {
    // TODO: Replace this Box with an AsyncImage (like Coil) or Image composable later
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AvatarPlaceholderColor)
    )
}