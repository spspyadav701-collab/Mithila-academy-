package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentTab: AppTab,
    ttsEnabled: Boolean,
    isSpeaking: Boolean,
    isListening: Boolean,
    isOwnerAuthenticated: Boolean,
    onToggleTts: () -> Unit,
    onNavigateToOwner: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Logo and Titles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BluePrimary, PurpleAi)
                            )
                        )
                        .border(1.dp, BlueSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "SPA Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SPA AI TEACHER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(listOf(BluePrimary, BlueSecondary)),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Mithila Academy • By SP",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Action Controls (Voice Speaking Indicator, TTS toggle, Owner Portal)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Voice Speaking Indicator
                if (isSpeaking) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(PurpleAi.copy(alpha = 0.2f))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Speaking",
                            tint = PurpleAiLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // TTS Mute / Unmute Toggle Button
                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier.testTag("tts_toggle_button")
                ) {
                    Icon(
                        imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (ttsEnabled) "Mute Voice" else "Unmute Voice",
                        tint = if (ttsEnabled) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Owner Panel Button
                FilledTonalButton(
                    onClick = onNavigateToOwner,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isOwnerAuthenticated) GreenSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isOwnerAuthenticated) GreenSuccess else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("owner_panel_button")
                ) {
                    Icon(
                        imageVector = if (isOwnerAuthenticated) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                        contentDescription = "Owner",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOwnerAuthenticated) "Owner" else "Login",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
