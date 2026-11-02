package com.hextech.aram.olympus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single text primitive for the whole home screen. BasicText is used instead
 * of Material3 Text so the screen depends on nothing but Compose UI/foundation.
 */
@Composable
fun Txt(
    text: String,
    family: FontFamily,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    spacing: TextUnit = 0.sp,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    align: TextAlign = TextAlign.Start,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit? = null
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = family,
            fontSize = size,
            fontWeight = weight,
            letterSpacing = spacing,
            color = color,
            textAlign = align,
            lineHeight = lineHeight ?: (size * 1.32f)
        ),
        overflow = TextOverflow.Ellipsis,
        softWrap = softWrap,
        maxLines = maxLines
    )
}

@Composable
fun Hairline(modifier: Modifier, color: Color, thickness: Int = 1) {
    Box(modifier.height(thickness.dp).background(color))
}

@Composable
fun Diamond(color: Color, modifier: Modifier = Modifier, sizeDp: Int = 4) {
    Canvas(modifier.size(sizeDp.dp * 2)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.5f
        val p = Path()
        p.moveTo(c.x, c.y - r)
        p.lineTo(c.x + r * 0.55f, c.y)
        p.lineTo(c.x, c.y + r)
        p.lineTo(c.x - r * 0.55f, c.y)
        p.close()
        drawPath(p, color, style = Stroke(width = 1f))
    }
}

/** A classical hairline with a small diamond at its centre. */
@Composable
fun RuledOrnament(modifier: Modifier, color: Color, diamond: Color) {
    Box(modifier.fillMaxWidth().height(9.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(color))
        Box(
            Modifier
                .background(Palette.Void.copy(alpha = 0.0f))
                .size(9.dp),
            contentAlignment = Alignment.Center
        ) { Diamond(diamond) }
    }
}

/** Tiny chevron, drawn so it never depends on an arrow glyph being present. */
@Composable
fun Chevron(color: Color, modifier: Modifier = Modifier, sizeDp: Int = 10) {
    Canvas(modifier.size(sizeDp.dp)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.10f).coerceAtLeast(1f)
        drawLine(color, Offset(w * 0.30f, h * 0.20f), Offset(w * 0.68f, h * 0.50f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.68f, h * 0.50f), Offset(w * 0.30f, h * 0.80f), stroke, StrokeCap.Round)
    }
}

/** A vertical hairline used as a quiet index gutter. */
@Composable
fun VerticalRule(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.width(1.dp).background(color))
}
