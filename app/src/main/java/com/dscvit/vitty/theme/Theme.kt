package com.dscvit.vitty.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AcademicsColorScheme =
    darkColorScheme(
        primary = Accent,
        secondary = Secondary,
        background = Background,
        surface = Secondary,
        onPrimary = Background,
        onSecondary = Accent,
        onBackground = TextColor,
        onSurface = TextColor,
    )

@Composable
fun VittyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AcademicsColorScheme,
        typography = VittyTypography,
        content = content,
    )
}
