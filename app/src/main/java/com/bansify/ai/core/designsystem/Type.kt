package com.bansify.ai.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bansify.ai.R

/**
 * Claude-inspired editorial typography pairing:
 * - Newsreader: warm literary serif for greetings, display headings, and assistant prose.
 * - DM Sans: clean geometric humanist sans for UI controls, buttons, pills, and captions.
 * - JetBrains Mono: technical monospace for code blocks and inline code.
 */
val BansifySerifFontFamily = FontFamily(
    Font(R.font.newsreader, FontWeight.Normal),
    Font(R.font.newsreader, FontWeight.Medium),
    Font(R.font.newsreader, FontWeight.SemiBold),
    Font(R.font.newsreader, FontWeight.Bold)
)

val BansifySansFontFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.SemiBold),
    Font(R.font.dm_sans, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

object BansifyTextStyles {
    val Splash = TextStyle(
        fontFamily = BansifySerifFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    )
    val H1 = TextStyle(
        fontFamily = BansifySerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    )
    val H2 = TextStyle(
        fontFamily = BansifySerifFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.15).sp
    )
    val EditorialHeading = TextStyle(
        fontFamily = BansifySerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.1).sp
    )
    val EditorialBody = TextStyle(
        fontFamily = BansifySerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    )
    val Body = TextStyle(
        fontFamily = BansifySansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
    val BodyStrong = TextStyle(
        fontFamily = BansifySansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
    val Caption = TextStyle(
        fontFamily = BansifySansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )
    val CaptionStrong = TextStyle(
        fontFamily = BansifySansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )
    val Mono = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

val BansifyTypography = Typography(
    displaySmall = BansifyTextStyles.Splash,
    headlineSmall = BansifyTextStyles.H1,
    titleLarge = BansifyTextStyles.H2,
    titleMedium = BansifyTextStyles.BodyStrong,
    titleSmall = BansifyTextStyles.CaptionStrong,
    bodyLarge = BansifyTextStyles.Body,
    bodyMedium = BansifyTextStyles.Body,
    bodySmall = BansifyTextStyles.Caption,
    labelLarge = BansifyTextStyles.BodyStrong,
    labelMedium = BansifyTextStyles.CaptionStrong,
    labelSmall = BansifyTextStyles.Caption
)
