package com.dscvit.vitty.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R

val Poppins =
    FontFamily(
        Font(R.font.poppins_300, FontWeight.Light),
        Font(R.font.poppins_400, FontWeight.Normal),
        Font(R.font.poppins_500, FontWeight.Medium),
        Font(R.font.poppins_600, FontWeight.SemiBold),
        Font(R.font.poppins_700, FontWeight.Bold),
    )

val AcademicsTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                color = TextColor,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                color = TextColor,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                color = TextColor.copy(alpha = 0.7f),
            ),
        labelLarge =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.24.sp,
                letterSpacing = (-0.14).sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
    )
