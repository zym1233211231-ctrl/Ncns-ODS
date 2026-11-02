package com.hextech.aram.olympus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AETHER GLASS.
 *
 * A panel is four layers: (1) a real, blurred transmission copy of the temple
 * scene behind it — only when the device actually supports RenderEffect;
 * (2) a cold tint that keeps text legible; (3) marble veining and a single
 * specular sweep; (4) a hairline bronze edge. No gold frames, no Greek-key
 * wallpaper, no iOS control-centre pills.
 */
@Composable
fun AetherSurface(
    modifier: Modifier,
    profile: GlassProfile,
    shape: Shape,
    panelOrigin: Offset,
    sceneSize: Size,
    tint: AetherTint = AetherTint.Panel,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.clip(shape)) {
        if (profile.blurDp > 0 && sceneSize.width > 2f && sceneSize.height > 2f) {
            Canvas(Modifier.matchParentSize().blur(profile.blurDp.dp)) {
                // Slight magnification reads as refraction through thick glass.
                translate(left = -panelOrigin.x, top = -panelOrigin.y) {
                    scale(1.012f, 1.012f, pivot = Offset(sceneSize.width * 0.5f, sceneSize.height * 0.5f)) {
                        drawOlympusScene(sceneSize, 0f, Offset.Zero, 0, 0.92f)
                    }
                }
            }
        }

        // Cold glass tint: heavier at the bottom so labels stay readable.
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        tint.top,
                        tint.mid,
                        tint.bottom
                    )
                )
            )
        )

        Box(
            Modifier.matchParentSize().drawBehind {
                // Marble veining — two long, very faint fractures.
                val vein = Palette.Ivory.copy(alpha = 0.045f)
                val p1 = androidx.compose.ui.graphics.Path()
                p1.moveTo(-size.width * 0.1f, size.height * 0.28f)
                p1.cubicTo(size.width * 0.25f, size.height * 0.22f, size.width * 0.5f, size.height * 0.40f, size.width * 1.1f, size.height * 0.30f)
                drawPath(p1, vein, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
                val p2 = androidx.compose.ui.graphics.Path()
                p2.moveTo(size.width * 0.05f, size.height * 0.72f)
                p2.cubicTo(size.width * 0.35f, size.height * 0.66f, size.width * 0.7f, size.height * 0.82f, size.width * 1.05f, size.height * 0.74f)
                drawPath(p2, vein, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))

                // Single specular sweep from the upper left.
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Palette.Ivory.copy(alpha = 0.10f), Palette.Ivory.copy(alpha = 0.02f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.55f, size.height * 0.75f)
                    ),
                    size = size
                )

                // Inner ivory highlight along the top edge.
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Palette.Ivory.copy(alpha = 0.30f), Color.Transparent)
                    ),
                    start = Offset(size.width * 0.06f, 1f),
                    end = Offset(size.width * 0.94f, 1f),
                    strokeWidth = 1f
                )
            }
        )

        Box(
            Modifier.matchParentSize().border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Palette.Bronze.copy(alpha = 0.55f),
                        Palette.BronzeDim.copy(alpha = 0.20f),
                        Palette.Bronze.copy(alpha = 0.30f)
                    )
                ),
                shape
            )
        )

        content()
    }
}

data class AetherTint(val top: Color, val mid: Color, val bottom: Color) {
    companion object {
        val Panel = AetherTint(
            top = Palette.Graphite2.copy(alpha = 0.38f),
            mid = Palette.SeaDeep.copy(alpha = 0.55f),
            bottom = Palette.Graphite.copy(alpha = 0.76f)
        )
        val Sheet = AetherTint(
            top = Palette.SeaDeep.copy(alpha = 0.66f),
            mid = Palette.Graphite2.copy(alpha = 0.78f),
            bottom = Palette.Void.copy(alpha = 0.89f)
        )
        val Dialog = AetherTint(
            top = Palette.Graphite2.copy(alpha = 0.74f),
            mid = Palette.SeaDeep.copy(alpha = 0.90f),
            bottom = Palette.Void.copy(alpha = 0.96f)
        )
    }
}

/**
 * The one primary action. It is the only element allowed a full bronze frame:
 * the visual weight of the screen has to point at ENTER OLYMPUS.
 */
@Composable
fun AetherGateButton(
    modifier: Modifier,
    fonts: OlympusFonts,
    title: String,
    latin: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press = animateFloatAsState(if (pressed) 0.986f else 1f, tween(110), label = "gatePress")
    val glow = animateFloatAsState(if (pressed) 1f else 0f, tween(170), label = "gateGlow")
    val shape = RoundedCornerShape(2.dp)

    Box(
        modifier
            .graphicsLayer {
                scaleX = press.value
                scaleY = press.value
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Palette.BronzeDeep.copy(alpha = 0.45f),
                        Palette.Graphite2.copy(alpha = 0.78f),
                        Palette.Void.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Palette.Bronze.copy(alpha = 0.85f), Palette.BronzeDim.copy(alpha = 0.35f))
                ),
                shape
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() }
    ) {
        Box(
            Modifier.matchParentSize().drawBehind {
                if (glow.value > 0.001f) {
                    drawRect(Palette.Gold.copy(alpha = 0.10f * glow.value))
                }
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Palette.Ivory.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    start = Offset(size.width * 0.05f, 1f),
                    end = Offset(size.width * 0.95f, 1f),
                    strokeWidth = 1f
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, Palette.Void.copy(alpha = 0.42f)),
                        startY = size.height * 0.40f,
                        endY = size.height
                    ),
                    size = size
                )
            }
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Txt(title, fonts.display, 18.sp, Palette.Ivory, spacing = 4.sp, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Txt(latin, fonts.display, 8.4.sp, Palette.Bronze, spacing = 3.4.sp, maxLines = 1)
            }
            Chevron(Palette.Gold.copy(alpha = 0.95f), Modifier.size(22.dp), sizeDp = 11)
        }
    }
}

/**
 * Secondary destination. Deliberately frameless: an index numeral, a short
 * rule, the name, and a chevron. Only the left rule warms on touch.
 */
@Composable
fun AetherRow(
    modifier: Modifier,
    fonts: OlympusFonts,
    numeral: String,
    title: String,
    latin: String,
    meta: String?,
    compact: Boolean,
    onClick: (() -> Unit)?
) {
    val enabled = onClick != null
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val rowModifier = if (enabled) {
        modifier.clickable(interactionSource = interaction, indication = null) { onClick?.invoke() }
    } else {
        modifier
    }

    Row(
        rowModifier
            .fillMaxWidth()
            .drawBehind {
                if (enabled && pressed) {
                    drawRect(Palette.Ivory.copy(alpha = 0.035f))
                }
                val ruleColor = if (enabled && pressed) Palette.Gold else Palette.BronzeDim
                val ruleAlpha = if (enabled) (if (pressed) 0.95f else 0.55f) else 0.22f
                drawLine(
                    color = ruleColor.copy(alpha = ruleAlpha),
                    start = Offset(0f, size.height * 0.16f),
                    end = Offset(0f, size.height * 0.84f),
                    strokeWidth = 1.2f
                )
            }
            .padding(start = 12.dp, end = 2.dp, top = if (compact) 7.dp else 10.dp, bottom = if (compact) 7.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Txt(
            numeral, fonts.display, 10.sp,
            if (enabled) Palette.Bronze.copy(alpha = 0.9f) else Palette.StoneDim.copy(alpha = 0.55f),
            modifier = Modifier.width(22.dp), spacing = 0.5.sp
        )
        Column(Modifier.weight(1f)) {
            Txt(
                title, fonts.display,
                if (compact) 13.5.sp else 16.sp,
                if (enabled) Palette.Ivory else Palette.Stone.copy(alpha = 0.55f),
                spacing = 2.sp, maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Txt(
                latin, fonts.display, 8.sp,
                if (enabled) Palette.StoneDim else Palette.StoneDeep.copy(alpha = 0.6f),
                spacing = 3.2.sp, maxLines = 1
            )
            if (meta != null && !compact) {
                Spacer(Modifier.height(3.dp))
                Txt(meta, fonts.display, 9.sp, Palette.BronzeDim.copy(alpha = if (enabled) 0.85f else 0.5f), spacing = 1.2.sp, maxLines = 1)
            }
        }
        if (!enabled) {
            Txt(
                "未开放", fonts.display, 8.sp, Palette.StoneDeep,
                spacing = 1.5.sp, modifier = Modifier.padding(end = 6.dp)
            )
        } else {
            Chevron(Palette.StoneDim, Modifier.size(14.dp), sizeDp = 8)
        }
    }
}

/** Tertiary action: settings, credits. No frame at all. */
@Composable
fun AetherAux(
    fonts: OlympusFonts,
    label: String,
    latin: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val enabled = onClick != null
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val m = if (enabled) {
        modifier.clickable(interactionSource = interaction, indication = null) { onClick?.invoke() }
    } else {
        modifier
    }
    Row(m.padding(vertical = 6.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(
            label, fonts.display, 11.sp,
            (if (enabled) Palette.Stone else Palette.StoneDeep).copy(alpha = if (enabled && pressed) 1f else 0.85f),
            spacing = 2.sp
        )
        Spacer(Modifier.width(6.dp))
        Txt(
            latin, fonts.display, 7.sp,
            Palette.StoneDeep.copy(alpha = 0.85f),
            spacing = 2.sp, weight = FontWeight.Normal
        )
        if (!enabled) {
            Spacer(Modifier.width(6.dp))
            Txt("未开放", fonts.display, 7.sp, Palette.StoneDeep.copy(alpha = 0.7f), spacing = 1.sp)
        }
    }
}

/** Divider between the brand block and the gates. */
@Composable
fun AetherDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Palette.BronzeDim.copy(alpha = 0.28f)))
}

/** Small square bronze seal used next to the version line. */
@Composable
fun BronzeSeal(modifier: Modifier = Modifier) {
    Canvas(modifier.size(7.dp)) {
        drawRect(Palette.Bronze.copy(alpha = 0.75f), size = size)
        drawRect(Palette.Void.copy(alpha = 0.55f), topLeft = Offset(size.width * 0.3f, size.height * 0.3f), size = Size(size.width * 0.4f, size.height * 0.4f))
    }
}

/** Unused guard to keep StrokeCap import meaningful on some toolchains. */
internal val AetherCaps = StrokeCap.Round
