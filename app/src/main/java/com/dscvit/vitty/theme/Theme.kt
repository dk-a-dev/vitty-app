package com.dscvit.vitty.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VittyColorScheme =
    lightColorScheme(
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
        colorScheme = VittyColorScheme,
        typography = VittyTypography,
        content = content,
    )
}
