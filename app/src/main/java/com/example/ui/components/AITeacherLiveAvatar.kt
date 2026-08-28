package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*

/**
 * Full-screen audio-reactive AI Teacher visual layer.
 * 100% viewport edge-to-edge, zero black spaces/margins/letterboxing.
 * Smart positioning ensures the teacher's face and Mithila Academy branding are prominently framed.
 * Reacts naturally to real-time voice amplitude, idle breathing, and organic micro-movements.
 */
@Composable
fun AITeacherFullScreenLayer(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioAmplitude: Float,
    waveformBands: List<Float>,
    customImageUri: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_live_motion")

    // 1. Natural idle breathing animation (gentle physiological rhythm)
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breathing"
    )

    // 2. Subtle natural head sway & micro-nodding
    val headSwayX by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_sway_x"
    )

    val headBobY by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_bob_y"
    )

    // 3. Natural Blinking simulation
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink_cycle"
    )
    val isBlinking = blinkProgress in 0.96f..0.985f

    // 4. Voice-reactive properties derived from real-time audio amplitude
    val voiceScaleMultiplier = if (isSpeaking) {
        1.0f + (audioAmplitude * 0.04f)
    } else if (isListening) {
        1.0f + (audioAmplitude * 0.02f)
    } else {
        1.0f
    }

    val totalScale = idleBreathing * voiceScaleMultiplier

    val activeSwayX = if (isSpeaking) {
        headSwayX * (1f + audioAmplitude * 0.8f)
    } else {
        headSwayX * 0.5f
    }

    val activeBobY = if (isSpeaking) {
        headBobY * (1f + audioAmplitude * 0.9f)
    } else {
        headBobY * 0.4f
    }

    val glowAlpha = if (isSpeaking) {
        (0.25f + audioAmplitude * 0.45f).coerceIn(0.15f, 0.7f)
    } else if (isListening) {
        0.35f
    } else {
        0.08f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .testTag("ai_teacher_fullscreen_viewport")
    ) {
        // === 1. 100% FULL-SCREEN COVER IMAGE WITH SMART BIAS (CLEAR FACE + MAXIMUM LOGO) ===
        val imageModifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = totalScale
                scaleY = totalScale
                translationX = activeSwayX * density
                translationY = activeBobY * density
            }
            .testTag("ai_teacher_fullscreen_image")

        if (!customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(customImageUri),
                contentDescription = "Mithila Academy AI Teacher",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = imageModifier
            )
        } else {
            AsyncImage(
                model = R.drawable.img_mithila_wallpaper,
                contentDescription = "Mithila Academy AI Teacher",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = imageModifier
            )
        }

        // === 2. REAL-TIME AUDIO REACTIVE AURA GLOW ===
        if (isSpeaking || isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isSpeaking) PurpleAi.copy(alpha = glowAlpha * 0.45f) else AmberAccent.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 950f
                        )
                    )
            )
        }

        // === 3. SUBTLE NATURAL BLINK OVERLAY ===
        if (isBlinking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.035f))
            )
        }

        // === 4. ULTRA-SUBTLE CINEMATIC GRADIENT (Clear face, subtle shadow only for text readability) ===
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.60f), // Top header subtle backdrop
                            Color.Black.copy(alpha = 0.05f), // Crystal clear face & logo region
                            Color.Black.copy(alpha = 0.20f), // Mid chest gentle transition
                            Color.Black.copy(alpha = 0.85f)  // Bottom controls backdrop
                        )
                    )
                )
        )
    }
}
