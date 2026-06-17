package com.rinnsan.creavity.presentation.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rinnsan.creavity.R
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// LOCAL TOKENS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private val DimBorder    = Color(0xFF2A2A2A)
private val FieldBg      = Color(0xFF0D0D0D)
private val ScanlineGray = Color(0xFF111111)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 1. LOGIN SCREEN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Khởi tạo CallbackManager cho Facebook
    val callbackManager = remember { CallbackManager.Factory.create() }

    // Launcher kết quả trả về từ Facebook
    val facebookLauncher =  rememberLauncherForActivityResult(
        contract = com.facebook.login.LoginManager.getInstance().createLogInActivityResultContract()
    ) { result ->
        // Gửi kết quả về CallbackManager
        com.facebook.login.LoginManager.getInstance().onActivityResult(result.resultCode, result.data, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                viewModel.signInWithFacebook(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("FacebookSignIn", "User cancelled sign-in")
            }

            override fun onError(error: FacebookException) {
                Log.e("FacebookSignIn", "Sign-in failed", error)
                viewModel.setError("// FACEBOOK SIGN-IN THẤT BẠI: ${error.localizedMessage?.uppercase()}")
            }
        })
    }

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    AuthScaffold {
        // ── Header ────────────────────────────────────────────────
        AuthHeader(
            label    = "ACCESS TERMINAL",
            title    = "SIGN IN",
            subtitle = "IDENTIFY YOURSELF TO PROCEED"
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Fields ────────────────────────────────────────────────
        AuthTextField(
            value       = email,
            onValueChange = { email = it },
            label       = "EMAIL ADDRESS",
            placeholder = "agent@rinnsan.io",
            keyboardType = KeyboardType.Email,
            imeAction   = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value         = password,
            onValueChange = { password = it },
            label         = "ACCESS CODE",
            placeholder   = "••••••••",
            keyboardType  = KeyboardType.Password,
            imeAction     = ImeAction.Done,
            onImeAction   = { focusManager.clearFocus() },
            isPassword    = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password link
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text       = "FORGOT ACCESS CODE?",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 10.sp,
                color      = CyberAcid.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp,
                modifier   = Modifier.clickable {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Error message ─────────────────────────────────────────
        if (authState is AuthState.Error) {
            AuthErrorBanner(message = (authState as AuthState.Error).message)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Primary CTA ───────────────────────────────────────────
        AuthPrimaryButton(
            label     = "INITIATE SESSION",
            isLoading = authState is AuthState.Loading,
            onClick   = { viewModel.login(email, password) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Divider ───────────────────────────────────────────────
        AuthDivider(label = "OR CONTINUE WITH")

        Spacer(modifier = Modifier.height(24.dp))

        // ── Social buttons ────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialAuthButton(
                label    = "GOOGLE",
                modifier = Modifier.weight(1f),
                iconRes  = R.drawable.ic_google,
                onClick  = {
                    coroutineScope.launch {
                        launchGoogleSignIn(context, viewModel)
                    }
                }
            )
            SocialAuthButton(
                label    = "FACEBOOK",
                modifier = Modifier.weight(1f),
                iconRes  = R.drawable.ic_facebook,
                onClick  = {
                    facebookLauncher.launch(listOf("email", "public_profile"))
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Register link ─────────────────────────────────────────
        AuthSwitchRow(
            question = "NO PROFILE YET?",
            action   = "CREATE IDENTITY",
            onClick  = { navController.navigate(Routes.REGISTER) }
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 2. REGISTER SCREEN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Khởi tạo CallbackManager cho Facebook
    val callbackManager = remember { CallbackManager.Factory.create() }

    // Launcher kết quả trả về từ Facebook
    val facebookLauncher = rememberLauncherForActivityResult(
        contract = com.facebook.login.LoginManager.getInstance().createLogInActivityResultContract()
    ) { result ->
        com.facebook.login.LoginManager.getInstance().onActivityResult(result.resultCode, result.data, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                viewModel.signInWithFacebook(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("FacebookSignIn", "User cancelled sign-in")
            }

            override fun onError(error: FacebookException) {
                Log.e("FacebookSignIn", "Sign-in failed", error)
                viewModel.setError("// FACEBOOK SIGN-IN THẤT BẠI: ${error.localizedMessage?.uppercase()}")
            }
        })
    }

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    AuthScaffold(
        showBack = true,
        onBack   = { navController.popBackStack() }
    ) {
        AuthHeader(
            label    = "IDENTITY FORGE",
            title    = "CREATE\nPROFILE",
            subtitle = "INITIALIZE YOUR PRESENCE IN THE SYSTEM"
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthTextField(
            value         = email,
            onValueChange = { email = it },
            label         = "EMAIL ADDRESS",
            placeholder   = "agent@rinnsan.io",
            keyboardType  = KeyboardType.Email,
            imeAction     = ImeAction.Next,
            onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value            = password,
            onValueChange    = { password = it },
            label            = "ACCESS CODE",
            placeholder      = "Min. 6 characters",
            keyboardType     = KeyboardType.Password,
            imeAction        = ImeAction.Next,
            onImeAction      = { focusManager.moveFocus(FocusDirection.Down) },
            isPassword       = true,
            passwordVisible  = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value            = confirmPassword,
            onValueChange    = { confirmPassword = it },
            label            = "CONFIRM ACCESS CODE",
            placeholder      = "Re-enter code",
            keyboardType     = KeyboardType.Password,
            imeAction        = ImeAction.Done,
            onImeAction      = { focusManager.clearFocus() },
            isPassword       = true,
            passwordVisible  = confirmVisible,
            onTogglePassword = { confirmVisible = !confirmVisible },
            isError          = confirmPassword.isNotEmpty() && password != confirmPassword
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password strength indicator
        if (password.isNotEmpty()) {
            PasswordStrengthBar(password = password)
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (authState is AuthState.Error) {
            AuthErrorBanner(message = (authState as AuthState.Error).message)
            Spacer(modifier = Modifier.height(16.dp))
        }

        AuthPrimaryButton(
            label     = "FORGE IDENTITY",
            isLoading = authState is AuthState.Loading,
            onClick   = { viewModel.register(email, password, confirmPassword) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthDivider(label = "OR CONTINUE WITH")

        Spacer(modifier = Modifier.height(24.dp))

        // ── Social buttons ────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialAuthButton(
                label    = "GOOGLE",
                modifier = Modifier.weight(1f),
                iconRes  = R.drawable.ic_google,
                onClick  = {
                    coroutineScope.launch {
                        launchGoogleSignIn(context, viewModel)
                    }
                }
            )
            SocialAuthButton(
                label    = "FACEBOOK",
                modifier = Modifier.weight(1f),
                iconRes  = R.drawable.ic_facebook,
                onClick  = {
                    facebookLauncher.launch(listOf("email", "public_profile"))
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        AuthSwitchRow(
            question = "ALREADY IDENTIFIED?",
            action   = "SIGN IN",
            onClick  = { navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }}
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 3. FORGOT PASSWORD SCREEN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var sent  by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            sent = true
            viewModel.resetState()
        }
    }

    AuthScaffold(
        showBack = true,
        onBack   = { navController.popBackStack() }
    ) {
        AuthHeader(
            label    = "RECOVERY PROTOCOL",
            title    = "RESET\nACCESS CODE",
            subtitle = "WE WILL TRANSMIT A RECOVERY LINK TO YOUR EMAIL"
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (!sent) {
            // ── Input state ───────────────────────────────────────
            AuthTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "REGISTERED EMAIL",
                placeholder   = "agent@rinnsan.io",
                keyboardType  = KeyboardType.Email,
                imeAction     = ImeAction.Done,
                onImeAction   = {}
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (authState is AuthState.Error) {
                AuthErrorBanner(message = (authState as AuthState.Error).message)
                Spacer(modifier = Modifier.height(16.dp))
            }

            AuthPrimaryButton(
                label     = "TRANSMIT RESET LINK",
                isLoading = authState is AuthState.Loading,
                onClick   = { viewModel.sendPasswordReset(email) }
            )
        } else {
            // ── Success state ─────────────────────────────────────
            AuthSuccessBanner(
                title   = "TRANSMISSION SENT",
                message = "Kiểm tra hộp thư của bạn. Link reset sẽ hết hạn sau 24 giờ."
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthPrimaryButton(
                label     = "BACK TO LOGIN",
                isLoading = false,
                onClick   = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        AuthSwitchRow(
            question = "REMEMBERED YOUR CODE?",
            action   = "SIGN IN",
            onClick  = { navController.popBackStack() }
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SHARED COMPOSABLES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ── Outer scaffold with scanline bg + ticker ──────────────────────
@Composable
private fun AuthScaffold(
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Subtle grid overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f   to CyberAcid.copy(alpha = 0.02f),
                        0.5f to Color.Transparent,
                        1f   to CyberAcid.copy(alpha = 0.02f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            if (showBack) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, DimBorder)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = TeslaWhite,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            content()

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Bottom ticker
        AuthTicker(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Section header ────────────────────────────────────────────────
@Composable
private fun AuthHeader(label: String, title: String, subtitle: String) {
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Column {
        // Label row
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(CyberAcid)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = label,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = VoidBlack,
                    modifier   = Modifier.alpha(blinkAlpha)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to CyberAcid.copy(alpha = 0.5f),
                            1f to Color.Transparent
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text          = title,
            fontFamily    = AppFonts.oswald,
            fontSize      = 48.sp,
            fontWeight    = FontWeight.Black,
            color         = TeslaWhite,
            letterSpacing = (-1).sp,
            lineHeight    = 50.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text          = subtitle,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            color         = TechSilver,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Text field ────────────────────────────────────────────────────
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    isError: Boolean = false
) {
    val borderColor = when {
        isError          -> GlitchRed
        value.isNotEmpty() -> CyberAcid.copy(alpha = 0.6f)
        else             -> DimBorder
    }

    Column {
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = if (isError) GlitchRed else TechSilver,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = borderColor)
                .background(FieldBg)
        ) {
            BasicAuthField(
                value            = value,
                onValueChange    = onValueChange,
                placeholder      = placeholder,
                keyboardType     = keyboardType,
                imeAction        = imeAction,
                onImeAction      = onImeAction,
                isPassword       = isPassword,
                passwordVisible  = passwordVisible,
                onTogglePassword = onTogglePassword
            )
        }

        if (isError && value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = "// PASSWORD KHÔNG KHỚP",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 9.sp,
                color      = GlitchRed
            )
        }
    }
}

@Composable
private fun BasicAuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    isPassword: Boolean,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit
) {
    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    TextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = {
            Text(
                text       = placeholder,
                fontFamily = AppFonts.spaceMono,
                fontSize   = 12.sp,
                color      = TechSilver.copy(alpha = 0.4f)
            )
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = AppFonts.spaceMono,
            fontSize   = 13.sp,
            color      = TeslaWhite
        ),
        singleLine            = true,
        keyboardOptions       = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction    = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        visualTransformation  = visualTransformation,
        trailingIcon          = if (isPassword) {{
            IconButton(onClick = onTogglePassword) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TechSilver,
                    modifier = Modifier.size(18.dp)
                )
            }
        }} else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = FieldBg,
            unfocusedContainerColor = FieldBg,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor             = CyberAcid
        )
    )
}

// ── Primary button ────────────────────────────────────────────────
@Composable
private fun AuthPrimaryButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val shimmer by rememberInfiniteTransition(label = "btn_shimmer").animateFloat(
        initialValue  = 0.7f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isLoading) CyberAcid.copy(alpha = shimmer) else CyberAcid)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color     = VoidBlack,
                    modifier  = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text          = "PROCESSING...",
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = VoidBlack,
                    letterSpacing = 2.sp
                )
            }
        } else {
            Text(
                text          = label,
                fontFamily    = AppFonts.oswald,
                fontSize      = 18.sp,
                fontWeight    = FontWeight.Black,
                color         = VoidBlack,
                letterSpacing = 2.sp
            )
        }
    }
}

// ── Social button ─────────────────────────────────────────────────
@Composable
private fun SocialAuthButton(
    label: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .border(width = 1.dp, color = DimBorder)
            .background(ScanlineGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.dp, TechSilver.copy(alpha = 0.4f))
                )
            }
            Text(
                text          = label,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = TechSilver,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Divider with label ────────────────────────────────────────────
@Composable
private fun AuthDivider(label: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DimBorder)
        )
        Text(
            text          = "  $label  ",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 2.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DimBorder)
        )
    }
}

// ── Error banner ──────────────────────────────────────────────────
@Composable
private fun AuthErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlitchRed.copy(alpha = 0.1f))
            .border(width = 1.dp, color = GlitchRed.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text       = "//",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = GlitchRed
        )
        Text(
            text       = message,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            color      = GlitchRed,
            lineHeight = 18.sp,
            modifier   = Modifier.weight(1f)
        )
    }
}

// ── Success banner ────────────────────────────────────────────────
@Composable
private fun AuthSuccessBanner(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberAcid.copy(alpha = 0.08f))
            .border(width = 1.dp, color = CyberAcid.copy(alpha = 0.4f))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text          = title,
            fontFamily    = AppFonts.oswald,
            fontSize      = 22.sp,
            fontWeight    = FontWeight.Black,
            color         = CyberAcid,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = message,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            color      = TechSilver,
            lineHeight = 20.sp
        )
    }
}

// ── Switch row (Login ↔ Register) ─────────────────────────────────
@Composable
private fun AuthSwitchRow(question: String, action: String, onClick: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = question,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            color         = TechSilver.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text          = action,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = CyberAcid,
            letterSpacing = 0.5.sp,
            modifier      = Modifier.clickable { onClick() }
        )
    }
}

// ── Password strength bar ─────────────────────────────────────────
@Composable
private fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length < 6                          -> 0
        password.length < 8                          -> 1
        password.any { it.isDigit() } &&
                password.any { it.isUpperCase() }            -> 3
        else                                         -> 2
    }
    val (label, color, fraction) = when (strength) {
        0    -> Triple("WEAK",   GlitchRed,              0.2f)
        1    -> Triple("FAIR",   Color(0xFFFF8C00),       0.45f)
        2    -> Triple("GOOD",   Color(0xFFCCFF00),       0.7f)
        else -> Triple("STRONG", Color(0xFF00FF88),       1f)
    }

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = "CODE STRENGTH",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 9.sp,
                color      = TechSilver.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Text(
                text       = label,
                fontFamily = AppFonts.spaceMono,
                fontSize   = 9.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(DimBorder)
        ) {
            val animFraction by animateFloatAsState(
                targetValue   = fraction,
                animationSpec = tween(400),
                label         = "strength"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animFraction)
                    .background(color)
            )
        }
    }
}

// ── Bottom ticker ─────────────────────────────────────────────────
@Composable
private fun AuthTicker(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val message     = "RINNSAN_CREAVITY // SECURE CHANNEL // IDENTITY ENCRYPTED // ACCESS CONTROLLED // "

    LaunchedEffect(Unit) {
        delay(300)
        while (true) {
            val target = scrollState.maxValue
            scrollState.animateScrollTo(
                value         = target,
                animationSpec = tween(
                    durationMillis = ((target / 25f) * 1000).toInt().coerceAtLeast(6000),
                    easing         = LinearEasing
                )
            )
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF080808))
            .border(width = 1.dp, color = CyberAcid.copy(alpha = 0.15f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(50) {
                Text(
                    text          = message,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 9.sp,
                    color         = CyberAcid.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// GOOGLE SIGN-IN HELPER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private suspend fun launchGoogleSignIn(
    context: android.content.Context,
    viewModel: AuthViewModel
) {
    try {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        viewModel.signInWithGoogle(idToken)
    } catch (e: GetCredentialCancellationException) {
        // Người dùng hủy — không làm gì
        Log.d("GoogleSignIn", "User cancelled sign-in")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Sign-in failed", e)
        viewModel.setError("// GOOGLE SIGN-IN THẤT BẠI: ${e.localizedMessage?.uppercase() ?: "LỖI KHÔNG XÁC ĐỊNH"}")
    }
}