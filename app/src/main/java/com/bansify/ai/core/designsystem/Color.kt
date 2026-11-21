package com.bansify.ai.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Brand & Semantic Tokens
val BansifyAccent = Color(0xFF2F6BFF)
val BansifyBrandEnd = Color(0xFF7A5CFF)

val BansifySuccess = Color(0xFF12A150)
val BansifyWarning = Color(0xFFE8A100)
val BansifyDanger = Color(0xFFD93025)

// Light Palette
val LightBg = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF7F8FA)
val LightSurfaceVariant = Color(0xFFEEF0F4)
val LightOnSurface = Color(0xFF121417)
val LightOnSurfaceVariant = Color(0xFF5B6270)
val LightOutline = Color(0xFFE2E5EA)
val LightAccentContainer = Color(0xFFE6EDFF)
val LightOnAccentContainer = Color(0xFF0E2A6E)
val LightDangerContainer = Color(0xFFFCE8E6)
val LightOnDangerContainer = Color(0xFF7A1710)
val LightFreeBg = Color(0xFFE3F5EC)
val LightFreeFg = Color(0xFF0E7A4A)
val LightPaidBg = Color(0xFFFFF4DC)
val LightPaidFg = Color(0xFF8C5F00)

// Dark Palette
val DarkBg = Color(0xFF0F1115)
val DarkSurface = Color(0xFF171A20)
val DarkSurfaceVariant = Color(0xFF20242C)
val DarkOnSurface = Color(0xFFECEEF2)
val DarkOnSurfaceVariant = Color(0xFF9AA3B2)
val DarkOutline = Color(0xFF2A2F39)
val DarkAccentContainer = Color(0xFF16233F)
val DarkOnAccentContainer = Color(0xFFD6E3FF)
val DarkDangerContainer = Color(0xFF3A1816)
val DarkOnDangerContainer = Color(0xFFFFB4AB)
val DarkFreeBg = Color(0xFF10301F)
val DarkFreeFg = Color(0xFF7BE0AE)
val DarkPaidBg = Color(0xFF33260D)
val DarkPaidFg = Color(0xFFF5C869)

// Brand gradient — strictly for AI avatar, splash mark, and profile avatar
val BansifyBrandGradient: Brush = Brush.linearGradient(
    colors = listOf(BansifyAccent, BansifyBrandEnd)
)

@Immutable
data class BansifyExtendedColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val freeBadgeBg: Color,
    val freeBadgeFg: Color,
    val paidBadgeBg: Color,
    val paidBadgeFg: Color,
    val codeBlockBg: Color,
    val codeBlockHeaderBg: Color,
    val scrim: Color
)

val LightExtendedColors = BansifyExtendedColors(
    success = BansifySuccess,
    warning = BansifyWarning,
    danger = BansifyDanger,
    dangerContainer = LightDangerContainer,
    onDangerContainer = LightOnDangerContainer,
    freeBadgeBg = LightFreeBg,
    freeBadgeFg = LightFreeFg,
    paidBadgeBg = LightPaidBg,
    paidBadgeFg = LightPaidFg,
    codeBlockBg = LightSurface,
    codeBlockHeaderBg = LightSurfaceVariant,
    scrim = Color(0x52121417)
)

val DarkExtendedColors = BansifyExtendedColors(
    success = BansifySuccess,
    warning = BansifyWarning,
    danger = BansifyDanger,
    dangerContainer = DarkDangerContainer,
    onDangerContainer = DarkOnDangerContainer,
    freeBadgeBg = DarkFreeBg,
    freeBadgeFg = DarkFreeFg,
    paidBadgeBg = DarkPaidBg,
    paidBadgeFg = DarkPaidFg,
    codeBlockBg = DarkSurface,
    codeBlockHeaderBg = DarkSurfaceVariant,
    scrim = Color(0x88000000)
)

val LocalBansifyColors = staticCompositionLocalOf { LightExtendedColors }
