package org.grupp18.sortsmart.frontend.loggin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// --- Theming & Colors ---
private val ActiveColor     = Color(0xFF386B21)
private val BackgroundColor = Color(0xFFE8E8DE)
private val InactiveColor   = Color(0xFF42473D)
private val ScrimColor      = Color(0x99000000)
private val ErrorColor      = Color(0xFFB00020)
private val FieldBgColor    = Color(0xFFF4F4EE)
private val LinkColor       = Color(0xFF1A6FB5)
private val SuccessColor    = Color(0xFF386B21)

// Which "page" the dialog is currently showing
private enum class DialogMode { LOGIN, REGISTER, FORGOT_PASSWORD, VERIFY_EMAIL }

// Backend validation rules
private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]+$")

/**
 * Full-screen auth dialog.
 * Defaults to Log In. User can switch to Create Account or Forgot Password.
 *
 * @param authViewModel  Shared [AuthViewModel] — pass in from parent so login
 *                       state is observed app-wide.
 * @param onDismiss      Called when the dialog is closed.
 * @param onAuthSuccess  Called after a successful login.
 */
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Only close the dialog on login success, not on registration
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
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
            onLogin = { username, password ->
                authViewModel.login(username, password)
            },
            onRegister = { username, email, password ->
                authViewModel.register(username, email, password)
            },
            onForgotPassword = { email ->
                authViewModel.forgotPassword(email)
            },
            onDismiss = {
                authViewModel.resetState()
                onDismiss()
            }
        )
    }
}

@Composable
private fun LoginDialogContent(
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(DialogMode.LOGIN) }

    // Switch to verify screen when registration completes
    LaunchedEffect(authState) {
        if (authState is AuthState.RegisteredPendingVerification) {
            mode = DialogMode.VERIFY_EMAIL
        }
    }

    // Fields
    var username        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation errors
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun clearAll() {
        username = ""; email = ""; password = ""
        usernameError = null; emailError = null; passwordError = null
    }

    fun switchTo(newMode: DialogMode) { clearAll(); mode = newMode }

    fun validate(): Boolean {
        // Username: required for login + register, 3-50 chars, only a-z A-Z 0-9 _ -
        usernameError = when {
            mode == DialogMode.FORGOT_PASSWORD    -> null
            username.isBlank()                   -> "Username is required"
            username.length < 3                  -> "Username must be at least 3 characters"
            username.length > 50                 -> "Username must be at most 50 characters"
            !USERNAME_REGEX.matches(username)    -> "Only letters, numbers, _ and - are allowed"
            else                                 -> null
        }

        // Email: required for register + forgot password
        emailError = when {
            mode == DialogMode.LOGIN -> null
            email.isBlank()         -> "Email is required"
            !email.contains("@")    -> "Enter a valid email address"
            else                    -> null
        }

        // Password: required for login + register, 8-128 chars
        passwordError = when {
            mode == DialogMode.FORGOT_PASSWORD -> null
            password.isBlank()                -> "Password is required"
            password.length < 8               -> "Password must be at least 8 characters"
            password.length > 128             -> "Password must be at most 128 characters"
            else                              -> null
        }

        return listOf(usernameError, emailError, passwordError).all { it == null }
    }

    val isLoading = authState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundColor)
                .border(width = 1.dp, color = ActiveColor, shape = RoundedCornerShape(20.dp))
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
                            DialogMode.LOGIN           -> "Log In"
                            DialogMode.REGISTER        -> "Create Account"
                            DialogMode.FORGOT_PASSWORD -> "Reset Password"
                            DialogMode.VERIFY_EMAIL    -> "Verify Email"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = InactiveColor)
                    }
                }

                Text(
                    text = when (mode) {
                        DialogMode.LOGIN           -> "Welcome back to SortSmart"
                        DialogMode.REGISTER        -> "Join SortSmart to track your impact"
                        DialogMode.FORGOT_PASSWORD -> "Enter your email and we'll send a reset link"
                        DialogMode.VERIFY_EMAIL    -> ""
                    },
                    color = InactiveColor,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )

                // Verify email screen — return early so nothing below renders
                if (mode == DialogMode.VERIFY_EMAIL) {
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = ActiveColor,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Check your inbox",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "We sent a verification link to your email address. Please verify before logging in.",
                        fontSize = 13.sp,
                        color = InactiveColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { switchTo(DialogMode.LOGIN) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveColor,
                            contentColor   = Color.White
                        )
                    ) {
                        Text("Go to Log In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    return@Column // skip all fields, submit button, and footer
                }

                // Global error banner
                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = ErrorColor,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Success banner (forgot password)
                if (authState is AuthState.Success && mode == DialogMode.FORGOT_PASSWORD) {
                    Text(
                        text = (authState as AuthState.Success).message,
                        color = SuccessColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Username (login + register)
                if (mode != DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = username,
                        onValueChange = { username = it; usernameError = null },
                        label = "Username",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = ActiveColor) },
                        error = usernameError
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Email (register + forgot password)
                if (mode == DialogMode.REGISTER || mode == DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Email",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = ActiveColor) },
                        keyboardType = KeyboardType.Email,
                        error = emailError
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Password (login + register)
                if (mode != DialogMode.FORGOT_PASSWORD) {
                    FormField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = "Password",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = ActiveColor) },
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = InactiveColor
                                )
                            }
                        },
                        error = passwordError
                    )

                    // Forgot password link (login mode only)
                    if (mode == DialogMode.LOGIN) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Forgot password?",
                                color = LinkColor,
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
                                DialogMode.LOGIN           -> onLogin(username, password)
                                DialogMode.REGISTER        -> onRegister(username, email, password)
                                DialogMode.FORGOT_PASSWORD -> onForgotPassword(email)
                                DialogMode.VERIFY_EMAIL    -> {} // never reached due to return@Column above
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveColor,
                        contentColor   = Color.White,
                        disabledContainerColor = ActiveColor.copy(alpha = 0.6f),
                        disabledContentColor   = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = when (mode) {
                                DialogMode.LOGIN           -> "Log In"
                                DialogMode.REGISTER        -> "Sign Up"
                                DialogMode.FORGOT_PASSWORD -> "Send Reset Link"
                                DialogMode.VERIFY_EMAIL    -> "" // never reached
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
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
                                pushStyle(SpanStyle(color = LinkColor, fontWeight = FontWeight.Bold))
                                append("Create one")
                                pop()
                            }
                            DialogMode.REGISTER -> {
                                append("Already have an account?  ")
                                pushStyle(SpanStyle(color = LinkColor, fontWeight = FontWeight.Bold))
                                append("Log in")
                                pop()
                            }
                            DialogMode.FORGOT_PASSWORD -> {
                                append("Remembered it?  ")
                                pushStyle(SpanStyle(color = LinkColor, fontWeight = FontWeight.Bold))
                                append("Back to Log In")
                                pop()
                            }
                            DialogMode.VERIFY_EMAIL -> {} // never reached
                        }
                    },
                    color = InactiveColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            switchTo(
                                when (mode) {
                                    DialogMode.LOGIN           -> DialogMode.REGISTER
                                    DialogMode.REGISTER        -> DialogMode.LOGIN
                                    DialogMode.FORGOT_PASSWORD -> DialogMode.LOGIN
                                    DialogMode.VERIFY_EMAIL    -> DialogMode.LOGIN // never reached
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
            label = { Text(label, color = InactiveColor, fontSize = 13.sp) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = true,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = InactiveColor,
                unfocusedTextColor      = InactiveColor,
                focusedBorderColor      = ActiveColor,
                unfocusedBorderColor    = InactiveColor,
                errorBorderColor        = ErrorColor,
                focusedContainerColor   = FieldBgColor,
                unfocusedContainerColor = FieldBgColor,
                cursorColor             = ActiveColor
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                text = error,
                color = ErrorColor,
                fontSize = 11.sp,
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
        onDismiss = {}
    )
}