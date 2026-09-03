package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = SanaaGold,
    onPrimary = DarkBg,
    primaryContainer = SanaaClay,
    onPrimaryContainer = Color.White,
    secondary = PoliceAccent,
    onSecondary = Color.White,
    secondaryContainer = PoliceBlue,
    onSecondaryContainer = Color.White,
    tertiary = GangGraffitiPink,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = DarkCardBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SanaaClay,
    onPrimary = Color.White,
    primaryContainer = SanaaGold,
    onPrimaryContainer = DarkBg,
    secondary = PoliceNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = PoliceNavy,
    tertiary = GangShawlRed,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF1E293B),
    surface = LightSurface,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to game dark theme for cinematic atmosphere
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.background.toArgb()
      window.navigationBarColor = colorScheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

