package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.grupp18.sortsmart.viewmodel.AuthViewModel
import org.grupp18.sortsmart.viewmodel.ProfileViewModel
import org.grupp18.sortsmart.viewmodel.state.ProfileState

// Theming & Colors
private val ActiveColor = Color(0xFF386B21)
private val BackgroundColor = Color(0xFFE8E8DE)
private val InactiveColor = Color(0xFF42473D)
private val FieldBgColor = Color(0xFFF4F4EE)
private val ErrorColor = Color(0xFFB00020)
private val DangerColor = Color(0xFFB00020)

/**
 * Profile screen.
 * - Not logged in → shows "Log In" button (bottom-left) that opens [LoginDialog].
 * - Logged in     → shows profile info with edit and logout options.
 *
 * @param resetToken  If non-null, the login dialog opens automatically on the reset password screen.
 *                    This comes from a password reset deep link.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    resetToken: String? = null,
    verifyToken: String? = null,
    triggerLogin: Boolean = false,
    onLoginTriggered: () -> Unit = {}
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()

    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var showVerifyDialog by remember { mutableStateOf(verifyToken != null) }
    // Open the dialog automatically if a reset token was passed in from a deep link
    var showLoginDialog by remember { mutableStateOf(resetToken != null) }

    // Open login dialog when triggered from header button
    LaunchedEffect(triggerLogin) {
        if (triggerLogin) {
            showLoginDialog = true
            onLoginTriggered()
        }
    }

    // Load profile whenever the user logs in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) profileViewModel.loadProfile()
    }

    Box(modifier = modifier.background(BackgroundColor)) {
        verificationMessage?.let { msg ->
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("Verified")) ActiveColor.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    color = if (msg.contains("Verified")) ActiveColor else ErrorColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        when {
            // Not logged in
            !isLoggedIn -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = InactiveColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "You're not logged in",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = InactiveColor
                    )
                    Text(
                        text = "Log in to view and manage your profile",
                        fontSize = 13.sp,
                        color = InactiveColor.copy(alpha = 0.7f)
                    )
                }

                // Log In button
                Button(
                    onClick = { showLoginDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Log In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            // Loading
            profileState is ProfileState.Loading -> {
                CircularProgressIndicator(
                    color = ActiveColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Error
            profileState is ProfileState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (profileState as ProfileState.Error).message,
                        color = ErrorColor,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { profileViewModel.loadProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveColor)
                    ) { Text("Retry", color = Color.White) }
                }
            }

            // Loaded
            profileState is ProfileState.Loaded -> {
                val profile = (profileState as ProfileState.Loaded).profile
                LoggedInProfile(
                    username = profile.username,
                    email = profile.email,
                    joinedDate = profile.createdAt,
                    onSave = { newUsername, newEmail ->
                        profileViewModel.updateProfile(newUsername, newEmail)
                    },
                    onLogout = { authViewModel.logout() },
                    onDeleteAccount = {
                        profileViewModel.deleteProfile { authViewModel.logout() }
                    }
                )
            }
        }
    }

    if (showVerifyDialog && verifyToken != null) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            containerColor = BackgroundColor,
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ActiveColor) },
            title = { Text("Verify Your Account", fontWeight = FontWeight.Bold) },
            text = { Text("Click below to verify your email address.") },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            authViewModel.verifyEmail(verifyToken) { statusCode ->
                                showVerifyDialog = false
                                if (statusCode == 200 || statusCode == 400) {
                                    verificationMessage = "Email Verified!"
                                    profileViewModel.loadProfile()
                                } else {
                                    verificationMessage = "Verification Failed (Error: $statusCode)"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveColor)
                    ) {
                        Text("Verify Now")
                    }
                }
            }
        )
    }
    // Auth dialog — pass reset token so it opens on the correct screen
    if (showLoginDialog) {
        LoginDialog(
            onDismiss = { showLoginDialog = false },
            onAuthSuccess = { showLoginDialog = false },
            authViewModel = authViewModel,
            resetToken = resetToken
        )
    }
}

// Logged-in profile view
@Composable
private fun LoggedInProfile(
    username: String,
    email: String,
    joinedDate: String,
    onSave: (String, String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var editUsername by remember(username) { mutableStateOf(username) }
    var editEmail by remember(email) { mutableStateOf(email) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        // Avatar circle
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(ActiveColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.take(1).uppercase(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = username,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = InactiveColor
        )
        Text(
            text = "Member since ${joinedDate.take(10)}",
            fontSize = 12.sp,
            color = InactiveColor.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(28.dp))

        // Edit fields
        OutlinedTextField(
            value = editUsername,
            onValueChange = { editUsername = it },
            label = { Text("Username", color = InactiveColor, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = ActiveColor) },
            enabled = isEditing,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = InactiveColor,
                unfocusedTextColor = InactiveColor,
                disabledTextColor = InactiveColor,
                focusedBorderColor = ActiveColor,
                unfocusedBorderColor = InactiveColor,
                disabledBorderColor = InactiveColor.copy(alpha = 0.4f),
                focusedContainerColor = FieldBgColor,
                unfocusedContainerColor = FieldBgColor,
                disabledContainerColor = FieldBgColor,
                cursorColor = ActiveColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = editEmail,
            onValueChange = { editEmail = it },
            label = { Text("Email", color = InactiveColor, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = ActiveColor) },
            enabled = isEditing,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = InactiveColor,
                unfocusedTextColor = InactiveColor,
                disabledTextColor = InactiveColor,
                focusedBorderColor = ActiveColor,
                unfocusedBorderColor = InactiveColor,
                disabledBorderColor = InactiveColor.copy(alpha = 0.4f),
                focusedContainerColor = FieldBgColor,
                unfocusedContainerColor = FieldBgColor,
                disabledContainerColor = FieldBgColor,
                cursorColor = ActiveColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Action buttons
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { editUsername = username; editEmail = email; isEditing = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InactiveColor)
                ) { Text("Cancel") }

                Button(
                    onClick = { onSave(editUsername, editEmail); isEditing = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveColor,
                        contentColor = Color.White
                    )
                ) { Text("Save", fontWeight = FontWeight.SemiBold) }
            }
        } else {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveColor,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Log out
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = InactiveColor)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log Out", fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.weight(1f))

        // Delete account
        TextButton(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.textButtonColors(contentColor = DangerColor)
        ) {
            Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Delete Account", fontSize = 13.sp)
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = BackgroundColor,
            title = {
                Text("Delete Account?", color = DangerColor, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete your account and all data. This cannot be undone.",
                    color = InactiveColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDeleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = InactiveColor)
                }
            }
        )
    }
}