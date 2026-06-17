package com.rinnsan.creavity.presentation.uplink.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.models.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY CARD V3 - WITH NAVIGATION
 * ═══════════════════════════════════════════════════════════════════
 *
 * Component hiển thị:
 * - Nếu CHƯA CÓ profile → Nút "BEGIN SCAN"
 * - Nếu ĐÃ CÓ profile → Thông tin archetype
 *
 * NOTE: Component này được gọi từ UplinkScreen
 */

@Composable
fun IdentityCardV3(
    profile: IdentityProfile?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PhantomGrey.copy(alpha = 0.5f))
            .border(1.dp, GridLineColor)
    ) {
        if (profile == null) {
            NoIdentityState(
                onBeginScan = {
                    navController.navigate(Routes.IDENTITY_SCANNER)
                }
            )
        } else {
            HasIdentityState(profile = profile)
        }
    }
}

@Composable
private fun NoIdentityState(
    onBeginScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = GlitchRed,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "IDENTITY REQUIRED",
            fontFamily = AppFonts.oswald,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GlitchRed,
            letterSpacing = 2.sp
        )

        Text(
            text = "System requires identity verification before access",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBeginScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberAcid,
                contentColor = VoidBlack
            )
        ) {
            Text(
                text = "BEGIN SCAN",
                fontFamily = AppFonts.oswald,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Text(
            text = "10 QUESTIONS // 2 MINUTES // IRREVERSIBLE",
            fontFamily = AppFonts.spaceMono,
            fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun HasIdentityState(
    profile: IdentityProfile
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ARCHETYPE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = CyberAcid,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = profile.getArchetypeLabel(),
                    fontFamily = AppFonts.oswald,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = CyberAcid,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CONFIDENCE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = TechSilver,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${profile.getConfidencePercentage()}%",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = CyberAcid,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(GridLineColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(profile.confidenceLevel)
                        .fillMaxHeight()
                        .background(CyberAcid)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SCORE BREAKDOWN",
            fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp,
            color = TechSilver,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        profile.getSortedArchetypes().forEach { (archetype, score) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = archetype.name,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TeslaWhite,
                    modifier = Modifier.weight(0.3f)
                )

                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(8.dp)
                        .background(GridLineColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(score)
                            .fillMaxHeight()
                            .background(Color(archetype.hexCode.removePrefix("#").toLong(16) or 0xFF000000))
                    )
                }

                Text(
                    text = "${(score * 100).toInt()}%",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (profile.isHybrid) {
                "STATUS: HYBRID IDENTITY // SCAN LOCKED"
            } else {
                "STATUS: PURE ARCHETYPE // SCAN LOCKED"
            },
            fontFamily = AppFonts.spaceMono,
            fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}