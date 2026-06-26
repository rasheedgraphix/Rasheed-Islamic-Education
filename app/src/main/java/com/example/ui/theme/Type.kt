package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.unit.sp

// Define font families using standard system fonts
val ArabicFont = FontFamily.Serif
val UrduFont = FontFamily.Serif
val EnglishFont = FontFamily.SansSerif

// Set of Material typography styles
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp
  ),
  titleLarge = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelSmall = TextStyle(
    fontFamily = EnglishFont,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  )
)

// Helper text styles for Arabic and Urdu
val ArabicTextStyle = TextStyle(
  fontFamily = ArabicFont,
  fontWeight = FontWeight.Normal,
  fontSize = 22.sp,
  lineHeight = 36.sp
)

val UrduTextStyle = TextStyle(
  fontFamily = UrduFont,
  fontWeight = FontWeight.Normal,
  fontSize = 20.sp,
  lineHeight = 34.sp
)

