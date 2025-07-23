package com.example.ecodeliandroid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Couleurs personnalisées pour EcoDeli
private val EcoGreen = Color(0xFF2E7D32)       // Vert principal
private val EcoGreenLight = Color(0xFF4CAF50)  // Vert clair
private val EcoGreenDark = Color(0xFF1B5E20)   // Vert foncé
private val EcoBlue = Color(0xFF1976D2)        // Bleu principal
private val EcoBlueLight = Color(0xFF42A5F5)   // Bleu clair
private val EcoBlueDark = Color(0xFF0D47A1)    // Bleu foncé
private val EcoOrange = Color(0xFFFF8F00)      // Orange accent
private val EcoOrangeLight = Color(0xFFFFA726) // Orange clair

// Palette de couleurs sombre
private val DarkColorScheme = darkColorScheme(
    primary = EcoGreenLight,
    onPrimary = Color.White,
    primaryContainer = EcoGreenDark,
    onPrimaryContainer = Color.White,

    secondary = EcoBlueLight,
    onSecondary = Color.White,
    secondaryContainer = EcoBlueDark,
    onSecondaryContainer = Color.White,

    tertiary = EcoOrangeLight,
    onTertiary = Color.Black,
    tertiaryContainer = EcoOrange,
    onTertiaryContainer = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),

    error = Color(0xFFFF5252),
    onError = Color.White,
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color.White,

    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242)
)

// Palette de couleurs claire
private val LightColorScheme = lightColorScheme(
    primary = EcoGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E8),
    onPrimaryContainer = EcoGreenDark,

    secondary = EcoBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = EcoBlueDark,

    tertiary = EcoOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),

    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun EcoDeliAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}