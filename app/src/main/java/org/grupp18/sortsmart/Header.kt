package org.grupp18.sortsmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
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
import androidx.navigation.NavDestination
import coil.compose.AsyncImage

// Theming & Colors
private val TitleTextColor         = Color(0xFF1A1C17)
private val NotificationBadgeColor = Color(0xFFFA2B35)
private val PlaceholderColor       = Color.LightGray
private val ActiveColor            = Color(0xFF386B21)

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header(
        currentDestination = null,
        onLogoClick = {},
        onProfileClick = {},
        isLoggedIn = false,
        onLoginClick = {}
    )
}

/**
 * The main top app bar (Header) for the application.
 *
 * @param currentDestination Used if you later want to highlight current route.
 * @param onLogoClick       Called when the logo/text is clicked (typically navigates Home).
 * @param onProfileClick    Called when avatar/login navigates to Profile.
 * @param isLoggedIn        Whether the user is logged in — controls avatar vs login hint.
 * @param onLoginClick      Called when the login action should open login dialog.
 */
@Composable
fun Header(
    currentDestination: NavDestination?,
    onLogoClick: () -> Unit,
    onProfileClick: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
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
        HeaderLogoAndTitle(onLogoClick = onLogoClick)
        HeaderActions(
            isLoggedIn = isLoggedIn,
            onProfileClick = onProfileClick,
            onLoginClick = onLoginClick
        )
    }
}

@Composable
private fun HeaderLogoAndTitle(onLogoClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onLogoClick)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFA8D672)),
            contentAlignment = Alignment.Center
        ) {
            Text("🌿", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Sort Smart",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TitleTextColor
        )
    }
}

@Composable
private fun HeaderActions(
    isLoggedIn: Boolean,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotificationBell(hasUnread = true)
        Spacer(modifier = Modifier.width(12.dp))

        if (!isLoggedIn) {
            Button(
                onClick = {
                    onProfileClick()
                    onLoginClick()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveColor,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Login,
                    contentDescription = "Log in",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Log In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        ProfileAvatar(
            imageUrl = "https://i.pravatar.cc/300",
            onClick = onProfileClick
        )
    }
}

@Composable
private fun NotificationBell(hasUnread: Boolean) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier.size(22.dp),
            tint = TitleTextColor
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp)
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
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentScale = ContentScale.Crop,
        placeholder = null
    )
}