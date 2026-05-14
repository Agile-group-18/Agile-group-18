package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import coil.compose.AsyncImage
import org.grupp18.sortsmart.ui.theme.SortSmartGreen
import org.grupp18.sortsmart.ui.theme.SortSmartInk
import org.grupp18.sortsmart.viewmodel.ProfileViewModel
import org.grupp18.sortsmart.viewmodel.state.ProfileState

private val TitleTextColor = SortSmartInk
private val NotificationBadgeColor = Color(0xFFFA2B35)
private val ActiveColor = SortSmartGreen

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header(
        currentDestination = null,
        onLogoClick = {},
        onProfileClick = {},
        isLoggedIn = true,
        onLoginClick = {}
    )
}

@Composable
fun Header(
    currentDestination: NavDestination?,
    onLogoClick: () -> Unit,
    onProfileClick: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) profileViewModel.loadProfile()
    }
    val displayName = (profileState as? ProfileState.Loaded)?.profile
        ?.let { it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.username }

    val avatarUrl = (profileState as? ProfileState.Loaded)?.profile?.avatarUrl

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
            username = displayName,
            avatarUrl = avatarUrl,
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
    username: String?,
    avatarUrl: String?,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotificationBell(hasUnread = true)
        Spacer(modifier = Modifier.width(12.dp))

        if (isLoggedIn) {
            ProfileAvatar(
                username = username,
                avatarUrl = avatarUrl,
                onClick = onProfileClick
            )
        } else {
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = "Log in",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Log In", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NotificationBell(hasUnread: Boolean) {
    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
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
    username: String?,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    val letter = username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(ActiveColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}