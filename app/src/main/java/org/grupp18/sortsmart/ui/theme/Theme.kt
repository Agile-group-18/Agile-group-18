package org.grupp18.sortsmart.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = GreenLight,
    onPrimary = OnGreenLight,
    background = BackgroundLight,
    onBackground = SortSmartInk,
    surface = SurfaceLight,
    onSurface = SortSmartInk,
    error = SortSmartError,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenDark,
    onPrimary = OnGreenDark,
    background = BackgroundDark,
    onBackground = BackgroundLight,
    surface = SurfaceDark,
    onSurface = BackgroundLight,
    error = SortSmartError,
)

@Composable
fun SortSmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // disabled — dynamic color overrides brand colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}