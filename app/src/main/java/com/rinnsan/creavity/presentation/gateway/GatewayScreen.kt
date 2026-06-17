package com.rinnsan.creavity.presentation.gateway

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.rinnsan.creavity.core.theme.AppFonts
import com.rinnsan.creavity.core.theme.VoidBlack
import com.rinnsan.creavity.core.theme.TeslaWhite
import com.rinnsan.creavity.core.theme.CyberAcid
import com.rinnsan.creavity.core.theme.TechSilver
import com.rinnsan.creavity.core.theme.PhantomGrey
import kotlinx.coroutines.delay

@Composable
fun GatewayScreen(
    onNavigateToHome: () -> Unit
) {
    // Animation states
    var logoAlpha by remember { mutableFloatStateOf(0f) }
    var blurAmount by remember { mutableFloatStateOf(10f) }
    var sloganAlpha by remember { mutableFloatStateOf(0f) }
    var sloganOffsetY by remember { mutableFloatStateOf(1f) }
    var buttonAlpha by remember { mutableFloatStateOf(0f) }

    // LOGO ANIMATION: fadeIn(1500ms) + blur(1000ms)
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = CubicBezierEasing(0.19f, 1f, 0.22f, 1f)
            )
        ) { value, _ ->
            logoAlpha = value
        }
    }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 10f,
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = CubicBezierEasing(0.19f, 1f, 0.22f, 1f)
            )
        ) { value, _ ->
            blurAmount = value
        }
    }

    // SLOGAN ANIMATION: fadeIn + slideY (delay 1000ms)
    LaunchedEffect(Unit) {
        delay(1000)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        ) { value, _ ->
            sloganAlpha = value
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600)
        ) { value, _ ->
            sloganOffsetY = value
        }
    }

    // BUTTON ANIMATION: fadeIn (delay 2000ms)
    LaunchedEffect(Unit) {
        delay(2000)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        ) { value, _ ->
            buttonAlpha = value
        }
    }

    // UNDERLINE ANIMATION: scaleX(begin: 0.5, end: 1.5) - repeat(reverse: true)
    val infiniteTransition = rememberInfiniteTransition(label = "underline")
    val underlineScaleX by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "underline_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // LAYER 1: BACKGROUND GRADIENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PhantomGrey,
                            VoidBlack
                        ),
                        radius = 1500f
                    )
                )
        )

        // LAYER 2: TYPOGRAPHY - Center
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO
            Text(
                text = "RINNSAN",
                fontSize = 72.sp,
                fontFamily = AppFonts.oswald,
                fontWeight = FontWeight.Light,
                color = TeslaWhite.copy(alpha = logoAlpha),
                letterSpacing = 18.sp,
                modifier = Modifier.blur(blurAmount.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SLOGAN ROW
            Row(
                modifier = Modifier.offset(y = (sloganOffsetY * 50).dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(0.5.dp)
                        .background(TechSilver.copy(alpha = 0.3f * sloganAlpha))
                )

                Spacer(modifier = Modifier.width(15.dp))

                Text(
                    text = "EST. 2026 // NEO-NOIR",
                    fontSize = 10.sp,
                    fontFamily = AppFonts.spaceMono,
                    fontWeight = FontWeight.Normal,
                    color = CyberAcid.copy(alpha = sloganAlpha),
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.width(15.dp))

                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(0.5.dp)
                        .background(TechSilver.copy(alpha = 0.3f * sloganAlpha))
                )
            }
        }

        // LAYER 3: ENTER BUTTON — luôn hiện, check session khi bấm
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNavigateToHome() // AppNavigation sẽ check isLoggedIn → HOME hoặc LOGIN
                    }
                    .padding(horizontal = 0.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENTER SYSTEM",
                    fontSize = 14.sp,
                    fontFamily = AppFonts.oswald,
                    fontWeight = FontWeight.Normal,
                    color = TeslaWhite.copy(alpha = buttonAlpha),
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .scale(scaleX = underlineScaleX, scaleY = 1f)
                        .background(CyberAcid.copy(alpha = buttonAlpha))
                )
            }
        }
    }
}