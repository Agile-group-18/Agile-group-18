package org.grupp18.sortsmart.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.grupp18.sortsmart.ui.theme.SortSmartBg
import org.grupp18.sortsmart.ui.theme.SortSmartError
import org.grupp18.sortsmart.ui.theme.SortSmartField
import org.grupp18.sortsmart.ui.theme.SortSmartGreen
import org.grupp18.sortsmart.ui.theme.SortSmartLink
import org.grupp18.sortsmart.ui.theme.SortSmartScrim
import org.grupp18.sortsmart.viewmodel.AuthViewModel
import org.grupp18.sortsmart.viewmodel.state.AuthState

// Which "page" the dialog is currently showing
private enum class DialogMode {
    LOGIN, REGISTER, FORGOT_PASSWORD, RESET_PASSWORD, VERIFY_EMAIL
}

// Backend validation rules
private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]+$")

/**
 * @param resetToken  If non-null the dialog opens directly on the Reset Password screen
 *                    with the token pre-filled (came from a deep link).
 */
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    resetToken: String? = null
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Reset stale state when the dialog first opens
    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    Dialog(
        onDismissRequest = {
            authViewModel.resetState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LoginDialogContent(
            authState = authState,
            onLogin = { username, password -> authViewModel.login(username, password) },
            onRegister = { username, email, password ->
                authViewModel.register(
                    username,
                    email,
                    password
                )
            },
            onForgotPassword = { usernameOrEmail -> authViewModel.forgotPassword(usernameOrEmail) },
            onResetPassword = { token, newPassword ->
                authViewModel.resetPassword(
                    token,
                    newPassword
                )
            },
            onAuthSuccess = onAuthSuccess,
            onDismiss = {
                authViewModel.resetState()
                onDismiss()
            },
            initialResetToken = resetToken
        )
    }
}

@Composable
private fun LoginDialogContent(
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onResetPassword: (String, String) -> Unit,
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit,
    initialResetToken: String? = null
) {
    // If a reset token was passed in via deep link, open straight to reset screen
    var mode by remember {
        mutableStateOf(
            if (initialResetToken != null) DialogMode.RESET_PASSWORD else DialogMode.LOGIN
        )
    }

    // React to auth state changes — only close dialog for login success
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.RegisteredPendingVerification -> mode = DialogMode.VERIFY_EMAIL
            is AuthState.PasswordResetSuccess -> mode = DialogMode.LOGIN
            is AuthState.Success -> if (mode == DialogMode.LOGIN) onAuthSuccess()
            else -> {}
        }
    }

    // Fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    // Pre-fill token from deep link if available
    var resetToken by remember { mutableStateOf(initialResetToken ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // Validation errors
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var tokenError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }

    fun clearAll() {
        username = ""; email = ""; password = ""
        resetToken = ""; newPassword = ""
        usernameError = null; emailError = null; passwordError = null
        tokenError = null; newPasswordError = null
        newPasswordVisible = false; passwordVisible = false
    }

    fun switchTo(newMode: DialogMode) {
        clearAll(); mode = newMode
    }

    fun validate(): Boolean {
        usernameError = when {
            mode == DialogMode.FORGOT_PASSWORD ||
                    mode == DialogMode.RESET_PASSWORD -> null

            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 50 -> "Username must be at most 50 characters"
            mode == DialogMode.REGISTER &&
                    !USERNAME_REGEX.matches(username) -> "Only letters, numbers, _ and - are allowed"

            else -> null
        }
        emailError = when {
            mode == DialogMode.LOGIN ||
                    mode == DialogMode.RESET_PASSWORD -> null

            email.isBlank() -> "Email or username is required"
            else -> null
        }
        passwordError = when {
            mode == DialogMode.FORGOT_PASSWORD ||
                    mode == DialogMode.RESET_PASSWORD -> null

            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            password.length > 128 -> "Password must be at most 128 characters"
            else -> null
        }
        tokenError = when {
            mode != DialogMode.RESET_PASSWORD -> null
            resetToken.isBlank() -> "Token is required"
            else -> null
        }
        newPasswordError = when {
            mode != DialogMode.RESET_PASSWORD -> null
            newPassword.isBlank() -> "New password is required"
            newPassword.length < 8 -> "Password must be at least 8 characters"
            newPassword.length > 128 -> "Password must be at most 128 characters"
            else -> null
        }
        return listOf(usernameError, emailError, passwordError, tokenError, newPasswordError)
            .all { it == null }
    }

    val isLoading = authState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SortSmartScrim),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SortSmartBg)
                .border(1.dp, SortSmartGreen, RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (mode) {
                            DialogMode.LOGIN -> "Log In"
                            DialogMode.REGISTER -> "Create Account"
                            DialogMode.FORGOT_PASSWORD -> "Forgot Password"
                            DialogMode.RESET_PASSWORD -> "Reset Password"
                            DialogMode.VERIFY_EMAIL -> "Verify Email"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SortSmartGreen
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SortSmartGreen
                        )
                    }
                }

                Text(
                    text = when (mode) {
                        DialogMode.LOGIN -> "Welcome back to SortSmart"
                        DialogMode.REGISTER -> "Join SortSmart to track your impact"
                        DialogMode.FORGOT_PASSWORD -> "Enter your username or email to get a reset link"
                        DialogMode.RESET_PASSWORD -> "Enter your new password"
                        DialogMode.VERIFY_EMAIL -> ""
                    },
                    color = SortSmartGreen,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // Verify email screen
                if (mode == DialogMode.VERIFY_EMAIL) {
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        Icons.Default.MarkEmailUnread, null,
                        tint = SortSmartGreen, modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Check your inbox",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SortSmartGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "We sent a verification link to your email address. Please verify before logging in.",
                        fontSize = 13.sp, color = SortSmartGreen, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { switchTo(DialogMode.LOGIN) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SortSmartGreen, contentColor = Color.White
                        )
                    ) {
                        Text("Go to Log In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    return@Column
                }

                // Reset password success banner
                if (authState is AuthState.PasswordResetSuccess) {
                    Text(
                        "Password reset successfully! You can now log in.",
                        color = SortSmartGreen, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Global error banner
                if (authState is AuthState.Error) {
                    Text(
                        authState.message,
                        color = SortSmartError, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Forgot password success banner
                if (authState is AuthState.Success && mode == DialogMode.FORGOT_PASSWORD) {
                    Text(
                        authState.message,
                        color = SortSmartGreen, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Check your email and click the reset link to set a new password.",
                        fontSize = 12.sp,
                        color = SortSmartGreen.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    return@Column
                }
                // Reset password fields
                if (mode == DialogMode.RESET_PASSWORD) {
                    FormField(
                        value = newPassword,
                        onValueChange = { newPassword = it; newPasswordError = null },
                        label = "New password",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = SortSmartGreen) },
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (newPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    if (newPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = SortSmartGreen
                                )
                            }
                        },
                        error = newPasswordError
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { if (validate()) onResetPassword(resetToken, newPassword) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SortSmartGreen, contentColor = Color.White,
                            disabledContainerColor = SortSmartGreen.copy(alpha = 0.6f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White, strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                "Set New Password",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Back to  ")
                            pushStyle(
                                SpanStyle(
                                    color = SortSmartLink,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            append("Log In"); pop()
                        },
                        color = SortSmartGreen, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { switchTo(DialogMode.LOGIN) }
                            .padding(vertical = 4.dp)
                    )
                    return@Column
                }

                // Username field (login + register)
                if (mode != DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = username,
                        onValueChange = { username = it; usernameError = null },
                        label = if (mode == DialogMode.LOGIN) "Username or email" else "Username",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = SortSmartGreen) },
                        error = usernameError
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Email field (register only)
                if (mode == DialogMode.REGISTER) {
                    FormField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Email",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SortSmartGreen) },
                        keyboardType = KeyboardType.Email,
                        error = emailError
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Username or email field (forgot password)
                if (mode == DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Username or email",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SortSmartGreen) },
                        error = emailError
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Password field (login + register)
                if (mode != DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = "Password",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = SortSmartGreen) },
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = SortSmartGreen
                                )
                            }
                        },
                        error = passwordError
                    )
                    if (mode == DialogMode.LOGIN) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                "Forgot password?",
                                color = SortSmartLink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { switchTo(DialogMode.FORGOT_PASSWORD) }
                                    .padding(top = 6.dp, bottom = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Submit button
                Button(
                    onClick = {
                        if (validate()) {
                            when (mode) {
                                DialogMode.LOGIN -> onLogin(username, password)
                                DialogMode.REGISTER -> onRegister(username, email, password)
                                DialogMode.FORGOT_PASSWORD -> onForgotPassword(email)
                                else -> {}
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SortSmartGreen, contentColor = Color.White,
                        disabledContainerColor = SortSmartGreen.copy(alpha = 0.6f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White, strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = when (mode) {
                                DialogMode.LOGIN -> "Log In"
                                DialogMode.REGISTER -> "Sign Up"
                                DialogMode.FORGOT_PASSWORD -> "Send Reset Link"
                                else -> ""
                            },
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Footer link
                Text(
                    text = buildAnnotatedString {
                        when (mode) {
                            DialogMode.LOGIN -> {
                                append("Don't have an account?  ")
                                pushStyle(
                                    SpanStyle(
                                        color = SortSmartLink,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                append("Create one"); pop()
                            }

                            DialogMode.REGISTER -> {
                                append("Already have an account?  ")
                                pushStyle(
                                    SpanStyle(
                                        color = SortSmartLink,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                append("Log in"); pop()
                            }

                            DialogMode.FORGOT_PASSWORD -> {
                                append("Remembered it?  ")
                                pushStyle(
                                    SpanStyle(
                                        color = SortSmartLink,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                append("Back to Log In"); pop()
                            }

                            else -> {}
                        }
                    },
                    color = SortSmartGreen, fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            switchTo(
                                when (mode) {
                                    DialogMode.LOGIN -> DialogMode.REGISTER
                                    DialogMode.REGISTER -> DialogMode.LOGIN
                                    DialogMode.FORGOT_PASSWORD -> DialogMode.LOGIN
                                    else -> DialogMode.LOGIN
                                }
                            )
                        }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Reusable field
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = SortSmartGreen, fontSize = 13.sp) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = true,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SortSmartGreen,
                unfocusedTextColor = SortSmartGreen,
                focusedBorderColor = SortSmartGreen,
                unfocusedBorderColor = SortSmartGreen,
                errorBorderColor = SortSmartError,
                focusedContainerColor = SortSmartField,
                unfocusedContainerColor = SortSmartField,
                cursorColor = SortSmartGreen
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                error, color = SortSmartError, fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginDialogPreview() {
    LoginDialogContent(
        authState = AuthState.Idle,
        onLogin = { _, _ -> },
        onRegister = { _, _, _ -> },
        onForgotPassword = {},
        onResetPassword = { _, _ -> },
        onAuthSuccess = {},
        onDismiss = {}
    )
}