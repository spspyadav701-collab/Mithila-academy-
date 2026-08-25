package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BluePrimary,
    onPrimary = PureWhite,
    primaryContainer = NavyCard,
    onPrimaryContainer = BlueSecondary,
    secondary = PurpleAiLight,
    onSecondary = PureWhite,
    secondaryContainer = NavyCard,
    onSecondaryContainer = PurpleAiLight,
    tertiary = AmberAccent,
    onTertiary = NavyDark,
    background = NavyDark,
    onBackground = TextPrimaryDark,
    surface = NavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = NavyBorder,
    error = RedLive,
    onError = PureWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BluePrimary,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = BluePrimaryVariant,
    secondary = PurpleAi,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = PurpleAi,
    tertiary = AmberAccent,
    onTertiary = PureWhite,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = RedLive,
    onError = PureWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color option
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // default to sleek modern dark theme for immersive video & live learning
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

