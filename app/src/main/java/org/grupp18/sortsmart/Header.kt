package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Theming & Colors
private val TitleTextColor          = Color(0xFF1A1C17)
private val NotificationBadgeColor  = Color(0xFFFA2B35)
private val PlaceholderColor        = Color.LightGray
private val ActiveColor             = Color(0xFF386B21)

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header(
        currentDestination = AppDestinations.HOME,
        onNavigate = {},
        isLoggedIn = false,
        onLoginClick = {}
    )
}

/**
 * The main top app bar (Header) for the application.
 *
 * @param isLoggedIn    Whether the user is logged in — controls avatar vs login button.
 * @param onLoginClick  Called when the login button is pressed — opens the login dialog.
 */
@Composable
fun Header(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit,
    isLoggedIn: Boolean = false,
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderLogoAndTitle()
        HeaderActions(
            isLoggedIn = isLoggedIn,
            onNavigate = onNavigate,
            onLoginClick = onLoginClick
        )
    }
}

@Composable
private fun HeaderLogoAndTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
private fun HeaderActions(
    isLoggedIn: Boolean,
    onNavigate: (AppDestinations) -> Unit,
    onLoginClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotificationBell(hasUnread = true)
        Spacer(modifier = Modifier.width(12.dp))

        if (!isLoggedIn) {
            Button(
                onClick = {
                    onNavigate(AppDestinations.PROFILE)
                    onLoginClick()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Log In", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        ProfileAvatar(
            imageUrl = "https://i.pravatar.cc/300",
            onClick = { onNavigate(AppDestinations.PROFILE) }
        )
    }
}

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
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(NotificationBadgeColor)
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    onClick: () -> Unit
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Profile Avatar",
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}