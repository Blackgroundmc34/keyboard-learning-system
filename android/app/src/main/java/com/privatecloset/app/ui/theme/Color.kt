package com.privatecloset.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Backgrounds — near-black with a purple undertone, like the reference splash/dashboard
val BgBlack = Color(0xFF07050D)
val BgDeepPurple = Color(0xFF120A24)
val BgCard = Color(0xFF1B1130)
val BgCardAlt = Color(0xFF221639)
val BgStroke = Color(0xFF32234F)

// Gold accents — used for the logo mark, badges, and highlighted text
val GoldLight = Color(0xFFF4D375)
val Gold = Color(0xFFD4AF37)
val GoldDeep = Color(0xFFB8860B)

// Purple accents — primary actions, progress, record button
val PurpleLight = Color(0xFFA855F7)
val Purple = Color(0xFF8B2FE0)
val PurpleDeep = Color(0xFF6D28D9)

// Text
val TextPrimary = Color(0xFFF5F3FA)
val TextSecondary = Color(0xFFB7A9D6)
val TextMuted = Color(0xFF7C6E97)

// Status
val SuccessGreen = Color(0xFF34D399)

val GoldGradient = Brush.horizontalGradient(listOf(GoldLight, Gold, GoldDeep))
val PurpleGradient = Brush.horizontalGradient(listOf(PurpleLight, Purple, PurpleDeep))
val ScreenBackgroundGradient = Brush.verticalGradient(listOf(BgBlack, BgDeepPurple, BgBlack))
