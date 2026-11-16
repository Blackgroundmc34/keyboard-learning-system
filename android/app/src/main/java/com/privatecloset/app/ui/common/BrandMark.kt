package com.privatecloset.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldDeep
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.TextSecondary

/**
 * The door + piano-key "P" mark from the brand splash screen, rendered with Canvas
 * so no external image asset is required.
 */
@Composable
fun PianoDoorMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 140.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Gold door panel on the left
        drawRoundRect(
            color = Gold,
            topLeft = Offset(w * 0.02f, h * 0.08f),
            size = Size(w * 0.42f, h * 0.84f),
            cornerRadius = CornerRadius(w * 0.04f),
        )
        // Door handle
        drawCircle(color = Color(0xFF3A2A05), radius = w * 0.02f, center = Offset(w * 0.36f, h * 0.5f))

        // The stem/loop of the "P" as a gold arc on the right, drawn as a thick stroke rounded shape
        drawRoundRect(
            color = GoldDeep,
            topLeft = Offset(w * 0.34f, h * 0.06f),
            size = Size(w * 0.34f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.17f, h * 0.25f),
            style = Stroke(width = w * 0.09f),
        )

        // Piano keys inset within the door
        val keysLeft = w * 0.08f
        val keysTop = h * 0.5f
        val keyWidth = w * 0.05f
        val keyHeight = h * 0.34f
        for (i in 0 until 5) {
            drawRect(
                color = Color.White,
                topLeft = Offset(keysLeft + i * (keyWidth + 1.5f), keysTop),
                size = Size(keyWidth, keyHeight),
            )
        }
        // black keys overlay
        val blackKeyHeight = keyHeight * 0.6f
        for (i in listOf(0, 1, 3)) {
            drawRect(
                color = Color(0xFF150D02),
                topLeft = Offset(keysLeft + i * (keyWidth + 1.5f) + keyWidth * 0.65f, keysTop),
                size = Size(keyWidth * 0.55f, blackKeyHeight),
            )
        }

        // sound wave bars to the right of the keys
        val waveX = w * 0.46f
        val barHeights = listOf(0.5f, 0.75f, 1f, 0.65f, 0.35f)
        barHeights.forEachIndexed { i, frac ->
            val bh = h * 0.28f * frac
            drawRoundRect(
                color = GoldLight,
                topLeft = Offset(waveX + i * (w * 0.035f), keysTop + (h * 0.28f - bh) / 2f),
                size = Size(w * 0.02f, bh),
                cornerRadius = CornerRadius(w * 0.01f),
            )
        }
    }
}

@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    markSize: androidx.compose.ui.unit.Dp = 140.dp,
    showTagline: Boolean = true,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        PianoDoorMark(size = markSize)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "PRIVATE",
            color = androidx.compose.ui.graphics.Color(0xFFF5F3FA),
            fontWeight = FontWeight.Light,
            fontSize = (markSize.value * 0.16f).sp,
            letterSpacing = 6.sp,
        )
        Text(
            text = "CLOSET",
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = (markSize.value * 0.22f).sp,
            letterSpacing = 6.sp,
        )
        if (showTagline) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Your sound. Your space. Your stage.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            PillBadge(text = "MUSIC SCHOOL APP")
        }
    }
}

@Composable
fun PillBadge(text: String) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
    ) {
        Text(
            text = text,
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
