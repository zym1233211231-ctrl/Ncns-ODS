package com.hextech.aram.olympus

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** One slow mote of dust in the god-light. Deterministic, no allocation per frame. */
data class DustMote(val x: Float, val y: Float, val radius: Float, val drift: Float, val phase: Float)

private val DustMotes: List<DustMote> = buildList {
    var seed = 0x2F6E2B1
    fun next(): Float {
        seed = seed * 1103515245 + 12345
        return ((seed ushr 16) and 0x7FFF) / 32767f
    }
    repeat(24) {
        add(
            DustMote(
                x = next(),
                y = next(),
                radius = 0.6f + next() * 1.7f,
                drift = 0.12f + next() * 0.55f,
                phase = next() * 6.2831f
            )
        )
    }
}

/**
 * The temple itself. Everything is drawn from the passed [screen] size, so the
 * same routine can paint the full-bleed backdrop and the faint, blurred
 * transmission copy that lives inside an AETHER GLASS panel.
 *
 * @param detail 0 = low (flat silhouettes), 1 = balanced, 2 = high (rays, dust, marble veining)
 * @param light  0..1 master exposure used by the entrance fade
 */
fun DrawScope.drawOlympusScene(
    screen: Size,
    seconds: Float,
    tilt: Offset,
    detail: Int,
    light: Float
) {
    val w = screen.width
    val h = screen.height
    if (w <= 1f || h <= 1f) return

    val portrait = h > w
    val u = min(w, h)
    val sway = if (detail > 0) 1f else 0.35f
    val shiftX = tilt.x * u * 0.022f * sway
    val shiftY = tilt.y * u * 0.014f * sway
    val cx = (if (portrait) w * 0.5f else w * 0.365f) + shiftX
    // Portrait: the temple lives entirely above the gate sheet (top 56% of screen).
    val horizon = h * (if (portrait) 0.300f else 0.640f) + shiftY * 0.4f

    // ---- sky -----------------------------------------------------------------
    drawRect(
        brush = Brush.verticalGradient(
            listOf(
                Palette.Void,
                Palette.Graphite,
                Palette.SeaDeep,
                Palette.Sea,
                Color(0xFF4C4A3E.toInt()),
                Color(0xFF2A2820.toInt())
            )
        ),
        size = screen
    )

    // Cold dawn behind the pediment.
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Palette.Gold.copy(alpha = 0.30f * light),
                Palette.Bronze.copy(alpha = 0.10f * light),
                Color.Transparent
            ),
            center = Offset(cx, horizon - h * 0.03f),
            radius = max(1f, w * (if (portrait) 0.72f else 0.52f))
        ),
        radius = max(1f, w * (if (portrait) 0.72f else 0.52f)),
        center = Offset(cx, horizon - h * 0.03f)
    )

    // ---- clouds --------------------------------------------------------------
    val cloudAlpha = if (detail >= 1) 1f else 0.6f
    val cloudDrift = sin(seconds * 0.045f) * u * 0.012f * sway
    fun band(y: Float, width: Float, thickness: Float, alpha: Float, phase: Float) {
        val dx = sin(seconds * 0.03f + phase) * u * 0.02f * sway + cloudDrift
        val left = cx - width * 0.5f + dx
        drawOval(
            brush = Brush.radialGradient(
                listOf(Palette.Ivory.copy(alpha = alpha * cloudAlpha * light), Color.Transparent),
                center = Offset(left + width * 0.5f, y),
                radius = max(1f, width * 0.5f)
            ),
            topLeft = Offset(left, y - thickness * 0.5f),
            size = Size(width, thickness)
        )
    }
    band(horizon - h * 0.22f, w * 0.95f, h * 0.10f, 0.085f, 0.4f)
    band(horizon - h * 0.145f, w * 0.72f, h * 0.075f, 0.105f, 1.7f)
    band(horizon - h * 0.075f, w * 1.05f, h * 0.065f, 0.09f, 3.1f)
    band(horizon - h * 0.025f, w * 0.60f, h * 0.045f, 0.075f, 4.6f)

    // ---- mountains -----------------------------------------------------------
    ridge(w, h, horizon + h * 0.01f, h * 0.155f, 0.8f, Palette.SeaGlow.copy(alpha = 0.16f * light))
    ridge(w, h, horizon + h * 0.03f, h * 0.115f, 2.4f, Palette.Slate.copy(alpha = 0.55f * light))
    ridge(w, h, horizon + h * 0.05f, h * 0.075f, 4.1f, Palette.Graphite2.copy(alpha = 0.92f * light))

    // ---- god rays (behind architecture) --------------------------------------
    if (detail >= 1) {
        val rayAlpha = if (detail >= 2) 0.055f else 0.03f
        for (i in 0 until 3) {
            val sx = cx - w * 0.26f + i * w * 0.20f
            val lean = sin(seconds * 0.08f + i) * u * 0.006f * sway
            val p = Path()
            p.moveTo(sx, -h * 0.04f)
            p.lineTo(sx + w * 0.045f, -h * 0.04f)
            p.lineTo(sx + w * 0.155f + lean, horizon + h * 0.02f)
            p.lineTo(sx + w * 0.075f + lean, horizon + h * 0.02f)
            p.close()
            drawPath(p, Palette.Ivory.copy(alpha = rayAlpha * light))
        }
    }

    // ---- ground --------------------------------------------------------------
    val groundTop = horizon - h * 0.02f
    val groundH = h - groundTop
    drawRect(
        brush = Brush.verticalGradient(
            0f to Palette.SeaDeep.copy(alpha = 0f),
            0.10f to Palette.Graphite2.copy(alpha = 0.85f),
            1f to Palette.Void,
            startY = groundTop,
            endY = groundTop + groundH
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(w, groundH)
    )

    // ---- temple geometry -----------------------------------------------------
    val groundY = h * (if (portrait) 0.365f else 0.820f) + shiftY * 0.5f
    val templeW = if (portrait) w * 0.66f else min(w * 0.44f, h * 1.18f)
    // Proportions follow the temple width, not the smaller screen edge, so the
    // colonnade never turns into a squat strip on tall portrait phones.
    val colH = templeW * 0.340f
    val plinthH = templeW * 0.075f
    val entH = templeW * 0.048f
    val pedH = templeW * 0.115f
    val colBottom = groundY - plinthH * 0.55f
    val colTop = colBottom - colH
    val entTop = colTop - entH
    val apex = entTop - pedH
    val half = templeW * 0.5f
    val colSpan = templeW * 0.74f
    val colW = templeW * 0.052f

    // Halo directly behind the pediment.
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Palette.Ivory.copy(alpha = 0.10f * light), Palette.Gold.copy(alpha = 0.05f * light), Color.Transparent),
            center = Offset(cx, entTop - pedH * 0.35f),
            radius = max(1f, templeW * 0.62f)
        ),
        radius = max(1f, templeW * 0.62f),
        center = Offset(cx, entTop - pedH * 0.35f)
    )

    // Soft cast shadow so the temple stands on the ground instead of floating.
    drawOval(
        brush = Brush.radialGradient(
            listOf(Palette.Void.copy(alpha = 0.55f * light), Color.Transparent),
            center = Offset(cx, groundY + plinthH * 0.35f),
            radius = max(1f, templeW * 0.72f)
        ),
        topLeft = Offset(cx - templeW * 0.72f, groundY - plinthH * 0.1f),
        size = Size(templeW * 1.44f, plinthH * 1.1f)
    )

    // Wide plinth / steps: three receding slabs.
    for (i in 0 until 3) {
        val grow = templeW * (0.04f + i * 0.045f)
        val top = groundY - plinthH * (0.55f - i * 0.18f)
        // Landscape: the lowest step runs off the bottom edge, so the temple
        // stands on the steps we are already climbing. Portrait: it stops above
        // the title block so the credits never sit on bright marble.
        val bottom = if (i == 2) {
            if (portrait) groundY + plinthH * 1.15f else h + 1f
        } else {
            groundY + plinthH * (i * 0.45f)
        }
        val left = cx - half - grow
        val right = cx + half + grow
        val shade = when (i) {
            0 -> Palette.MarbleMid
            1 -> Palette.MarbleShade
            else -> Palette.StoneDeep
        }
        drawRect(
            color = shade.copy(alpha = 0.95f * light),
            topLeft = Offset(left, top),
            size = Size(right - left, max(1f, bottom - top))
        )
        drawLine(
            color = Palette.MarbleLit.copy(alpha = 0.45f * light),
            start = Offset(left, top),
            end = Offset(right, top),
            strokeWidth = max(1f, u * 0.0018f)
        )
        drawLine(
            color = Palette.Graphite2.copy(alpha = 0.45f * light),
            start = Offset(left, bottom),
            end = Offset(right, bottom),
            strokeWidth = max(1f, u * 0.0014f)
        )
    }

    // Cella: the door the whole picture points at.
    val doorW = templeW * 0.20f
    val doorTop = colTop + colH * 0.16f
    drawRect(
        color = Color(0xFF07080A),
        topLeft = Offset(cx - doorW * 0.5f, doorTop),
        size = Size(doorW, colBottom - doorTop)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Palette.Ember.copy(alpha = 0.34f * light),
                Palette.Gold.copy(alpha = 0.13f * light),
                Color.Transparent
            ),
            center = Offset(cx, doorTop + (colBottom - doorTop) * 0.55f),
            radius = max(1f, doorW * 2.3f)
        ),
        radius = max(1f, doorW * 2.3f),
        center = Offset(cx, doorTop + (colBottom - doorTop) * 0.55f)
    )
    drawLine(
        color = Palette.Bronze.copy(alpha = 0.35f * light),
        start = Offset(cx - doorW * 0.5f, doorTop),
        end = Offset(cx + doorW * 0.5f, doorTop),
        strokeWidth = max(1f, u * 0.002f)
    )
    drawOval(
        brush = Brush.radialGradient(
            listOf(Palette.Gold.copy(alpha = 0.10f * light), Color.Transparent),
            center = Offset(cx, groundY - plinthH * 0.2f),
            radius = max(1f, templeW * 0.30f)
        ),
        topLeft = Offset(cx - templeW * 0.30f, groundY - plinthH * 0.55f),
        size = Size(templeW * 0.60f, plinthH * 0.70f)
    )

    // Columns.
    val n = 6
    val x0 = cx - colSpan * 0.5f
    val stepX = if (n > 1) colSpan / (n - 1) else 0f
    for (i in 0 until n) {
        val x = x0 + stepX * i
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Palette.MarbleShade.copy(alpha = 0.95f * light),
                    Palette.MarbleLit.copy(alpha = 0.98f * light),
                    Palette.Marble.copy(alpha = 0.95f * light),
                    Palette.MarbleShade.copy(alpha = 0.95f * light)
                ),
                startX = x - colW * 0.5f,
                endX = x + colW * 0.5f
            ),
            topLeft = Offset(x - colW * 0.5f, colTop),
            size = Size(colW, colH)
        )
        // flute lines
        if (detail >= 1) {
            for (f in 1..2) {
                val fx = x - colW * 0.5f + colW * f / 3f
                drawLine(
                    color = Palette.MarbleShade.copy(alpha = 0.35f * light),
                    start = Offset(fx, colTop + colH * 0.03f),
                    end = Offset(fx, colBottom - colH * 0.03f),
                    strokeWidth = max(1f, colW * 0.06f)
                )
            }
        }
        // capital + base
        drawRect(
            color = Palette.MarbleLit.copy(alpha = light),
            topLeft = Offset(x - colW * 0.82f, colTop - u * 0.014f),
            size = Size(colW * 1.64f, u * 0.014f)
        )
        drawRect(
            color = Palette.MarbleMid.copy(alpha = light),
            topLeft = Offset(x - colW * 0.76f, colBottom - u * 0.012f),
            size = Size(colW * 1.52f, u * 0.012f)
        )
    }

    // Entablature + triglyphs.
    val entLeft = cx - half * 0.96f
    val entRight = cx + half * 0.96f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Palette.MarbleLit.copy(alpha = light), Palette.MarbleMid.copy(alpha = light), Palette.MarbleShade.copy(alpha = light)),
            startY = entTop,
            endY = entTop + entH
        ),
        topLeft = Offset(entLeft, entTop),
        size = Size(entRight - entLeft, entH)
    )
    if (detail >= 1) {
        val ticks = 11
        for (i in 0 until ticks) {
            val tx = entLeft + (entRight - entLeft) * (i + 0.5f) / ticks
            drawLine(
                color = Palette.MarbleShade.copy(alpha = 0.55f * light),
                start = Offset(tx, entTop + entH * 0.25f),
                end = Offset(tx, entTop + entH * 0.78f),
                strokeWidth = max(1f, (entRight - entLeft) / ticks * 0.16f)
            )
        }
    }

    // Pediment.
    val ped = Path()
    ped.moveTo(entLeft, entTop)
    ped.lineTo(cx, apex)
    ped.lineTo(entRight, entTop)
    ped.close()
    drawPath(
        ped,
        brush = Brush.verticalGradient(
            listOf(Palette.MarbleLit.copy(alpha = light), Palette.MarbleMid.copy(alpha = light), Palette.MarbleShade.copy(alpha = light)),
            startY = apex,
            endY = entTop
        )
    )
    // Tympanum shadow.
    val inner = Path()
    inner.moveTo(entLeft + templeW * 0.085f, entTop - entH * 0.12f)
    inner.lineTo(cx, apex + pedH * 0.34f)
    inner.lineTo(entRight - templeW * 0.085f, entTop - entH * 0.12f)
    inner.close()
    drawPath(inner, Palette.Graphite2.copy(alpha = 0.42f * light))
    // Apex ornament.
    drawCircle(
        color = Palette.Bronze.copy(alpha = 0.85f * light),
        radius = max(1f, u * 0.006f),
        center = Offset(cx, apex - u * 0.006f)
    )

    // ---- torches -------------------------------------------------------------
    val flicker = if (detail >= 1) 1f + 0.10f * sin(seconds * 3.3f) else 1f
    torch(cx - half - templeW * 0.10f, groundY - plinthH * 0.10f, u, seconds, flicker, light, detail)
    torch(cx + half + templeW * 0.10f, groundY - plinthH * 0.10f, u, seconds + 1.7f, flicker, light, detail)

    // ---- foreground fog + vignette ------------------------------------------
    val fogTop = h * (if (portrait) 0.34f else 0.60f)
    val fogH = h - fogTop
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.55f to Palette.SeaDeep.copy(alpha = 0.10f * light),
            1f to Palette.Void.copy(alpha = 0.72f),
            startY = fogTop,
            endY = fogTop + fogH
        ),
        topLeft = Offset(0f, fogTop),
        size = Size(w, fogH)
    )
    val vignette = Brush.radialGradient(
        listOf(Color.Transparent, Color.Transparent, Palette.Void.copy(alpha = 0.55f)),
        center = Offset(cx, h * (if (portrait) 0.26f else 0.46f)),
        radius = max(1f, max(w, h) * 0.85f)
    )
    drawRect(brush = vignette, size = screen)

    // ---- dust ----------------------------------------------------------------
    if (detail >= 1) {
        for (m in DustMotes) {
            val dy = ((m.y - seconds * 0.0045f * m.drift) % 1f + 1f) % 1f
            val dx = m.x + sin(seconds * 0.22f * m.drift + m.phase) * 0.012f
            val px = dx * w
            val py = dy * h
            if (py < horizon - h * 0.02f) {
                drawCircle(
                    color = Palette.Ivory.copy(alpha = 0.16f * light),
                    radius = m.radius * (u / 720f) * 1.6f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private fun DrawScope.torch(
    x: Float,
    y: Float,
    u: Float,
    seconds: Float,
    flicker: Float,
    light: Float,
    detail: Int
) {
    val poleH = u * 0.105f
    drawLine(
        color = Palette.BronzeDeep.copy(alpha = 0.95f * light),
        start = Offset(x, y),
        end = Offset(x, y - poleH),
        strokeWidth = max(1f, u * 0.006f)
    )
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Palette.Bronze.copy(alpha = 0.95f * light), Palette.BronzeDeep.copy(alpha = 0.95f * light)),
            startY = y - poleH - u * 0.016f,
            endY = y - poleH + u * 0.016f
        ),
        topLeft = Offset(x - u * 0.032f, y - poleH - u * 0.016f),
        size = Size(u * 0.064f, u * 0.032f)
    )
    val flameCenter = Offset(x, y - poleH - u * 0.042f * flicker)
    val radius = max(1f, u * 0.080f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Palette.Gold.copy(alpha = 0.52f * light),
                Palette.Ember.copy(alpha = 0.26f * light),
                Color.Transparent
            ),
            center = flameCenter,
            radius = radius
        ),
        radius = radius,
        center = flameCenter
    )
    if (detail >= 1) {
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Palette.Ivory.copy(alpha = 0.9f * light), Palette.Gold.copy(alpha = 0.75f * light), Palette.Ember.copy(alpha = 0.0f)),
                startY = flameCenter.y - u * 0.020f * flicker,
                endY = flameCenter.y + u * 0.020f * flicker
            ),
            topLeft = Offset(flameCenter.x - u * 0.010f, flameCenter.y - u * 0.020f * flicker),
            size = Size(u * 0.020f, u * 0.040f * flicker)
        )
    }
}

private fun DrawScope.ridge(w: Float, h: Float, baseY: Float, amp: Float, seed: Float, color: Color) {
    val p = Path()
    p.moveTo(-w * 0.08f, baseY + amp)
    var x = -w * 0.08f
    val step = w * 1.16f / 8f
    for (i in 0..8) {
        val y = baseY - amp * (0.32f + 0.68f * abs(sin(seed + i * 1.93f)))
        p.lineTo(x, y)
        x += step
    }
    p.lineTo(w * 1.08f, h)
    p.lineTo(-w * 0.08f, h)
    p.close()
    drawPath(p, color)
}

/** Full-bleed backdrop composable. Time and tilt are lambdas read only in draw. */
@Composable
fun TempleBackdrop(
    modifier: Modifier,
    seconds: () -> Float,
    tilt: () -> Offset,
    detail: Int,
    light: State<Float>
) {
    Canvas(modifier) {
        drawOlympusScene(size, seconds(), tilt(), detail, light.value)
    }
}

/** Thin orbit ring used by the loading state of the primary gate (kept static). */
fun DrawScope.strokeCircle(color: Color, radius: Float, center: Offset, width: Float) {
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = width))
}
