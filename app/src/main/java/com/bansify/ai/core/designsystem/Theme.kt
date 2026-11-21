package com.bansify.ai.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

private val LightColorScheme = lightColorScheme(
    primary = BansifyAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightOnAccentContainer,
    secondary = LightOnSurfaceVariant,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightOnSurface,
    background = LightBg,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightBg,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = BansifyDanger,
    onError = Color.White,
    errorContainer = LightDangerContainer,
    onErrorContainer = LightOnDangerContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = BansifyAccent,
    onPrimary = Color.White,
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkOnAccentContainer,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = BansifyDanger,
    onError = Color.White,
    errorContainer = DarkDangerContainer,
    onErrorContainer = DarkOnDangerContainer
)

@Composable
fun BansifyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    fontScaleMultiplier: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val currentDensity = LocalDensity.current
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = (currentDensity.fontScale * fontScaleMultiplier).coerceIn(0.85f, 2.2f)
    )

    CompositionLocalProvider(
        LocalBansifyColors provides extendedColors,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BansifyTypography,
            shapes = BansifyShapes,
            content = content
        )
    }
}

object BansifyThemeTokens {
    val extendedColors: BansifyExtendedColors
        @Composable
        get() = LocalBansifyColors.current
}
