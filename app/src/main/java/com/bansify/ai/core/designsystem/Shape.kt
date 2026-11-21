package com.bansify.ai.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object BansifyRadius {
    val Chip = RoundedCornerShape(8.dp)
    val Input = RoundedCornerShape(12.dp)
    val Card = RoundedCornerShape(16.dp)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val SheetFull = RoundedCornerShape(24.dp)
    val Composer = RoundedCornerShape(28.dp)
    val UserBubble = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp,
        bottomEnd = 8.dp
    )
    val Full = CircleShape
}

val BansifyShapes = Shapes(
    extraSmall = BansifyRadius.Chip,
    small = BansifyRadius.Input,
    medium = BansifyRadius.Card,
    large = BansifyRadius.SheetFull,
    extraLarge = BansifyRadius.Composer
)
