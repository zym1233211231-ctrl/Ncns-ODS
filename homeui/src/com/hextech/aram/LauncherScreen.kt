package com.hextech.aram

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hextech.aram.olympus.AetherAux
import com.hextech.aram.olympus.AetherDivider
import com.hextech.aram.olympus.AetherGateButton
import com.hextech.aram.olympus.AetherRow
import com.hextech.aram.olympus.AetherSurface
import com.hextech.aram.olympus.AetherTint
import com.hextech.aram.olympus.BronzeSeal
import com.hextech.aram.olympus.CreditsOverlay
import com.hextech.aram.olympus.Diamond
import com.hextech.aram.olympus.GlassProfile
import com.hextech.aram.olympus.GlassProfiles
import com.hextech.aram.olympus.Hairline
import com.hextech.aram.olympus.HomeSettings
import com.hextech.aram.olympus.OlympusFonts
import com.hextech.aram.olympus.Palette
import com.hextech.aram.olympus.SettingsOverlay
import com.hextech.aram.olympus.TempleBackdrop
import com.hextech.aram.olympus.Txt
import com.hextech.aram.olympus.rememberOlympusFonts

/**
 * THE GATES OF OLYMPUS — the home screen.
 *
 * Public contract (bytecode-compatible with the decompiled launcher):
 *   LauncherScreen(String, () -> Unit, () -> Unit, () -> Unit)
 * The three callbacks keep their original meaning: arena, chronicles, atelier.
 */
@Composable
fun LauncherScreen(
    version: String,
    onPickArena: () -> Unit,
    onPickChronicle: () -> Unit,
    onPickAtelier: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember(context) { HomeSettings(context) }
    val profile = remember(settings.quality, context) { GlassProfiles.detect(context, settings.quality) }
    val fonts = rememberOlympusFonts()
    val hostView = LocalView.current

    // Presentation clock. Read only from draw lambdas, so per-frame time never
    // triggers recomposition. Frozen while the host window is not visible.
    val clock = remember { mutableStateOf(0L) }
    LaunchedEffect(settings.motion, profile.sceneDetail) {
        if (!settings.motion) return@LaunchedEffect
        var base = 0L
        var frame = 0
        while (true) {
            withFrameNanos { now ->
                val visible = hostView.isShown && hostView.windowVisibility == android.view.View.VISIBLE
                if (!visible) {
                    base = 0L
                    return@withFrameNanos
                }
                frame++
                // Low tier drops the ambient clock to ~20fps of visual change.
                if (profile.sceneDetail > 0 || frame % 3 == 0) {
                    if (base == 0L) base = now
                    clock.value = now - base
                }
            }
        }
    }
    val seconds: () -> Float = remember(clock) { { clock.value / 1_000_000_000f } }

    val tilt = remember { mutableStateOf(Offset.Zero) }
    val sceneSize = remember { mutableStateOf(Size.Zero) }
    val parallaxEnabled = settings.parallax && profile.sceneDetail > 0

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val reveal = animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    var settingsOpen by remember { mutableStateOf(false) }
    var creditsOpen by remember { mutableStateOf(false) }

    // System back closes the sheet instead of leaving the app.
    BackHandler(enabled = settingsOpen) { settingsOpen = false }
    BackHandler(enabled = creditsOpen) { creditsOpen = false }

    val parallaxModifier = if (parallaxEnabled) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val position = event.changes.firstOrNull()?.position
                    if (position != null && size.width > 0 && size.height > 0) {
                        tilt.value = Offset(
                            position.x / size.width - 0.5f,
                            position.y / size.height - 0.5f
                        )
                    }
                }
            }
        }
    } else {
        Modifier
    }

    Box(Modifier.fillMaxSize().background(Palette.Void).then(parallaxModifier)) {
        TempleBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    sceneSize.value = Size(it.size.width.toFloat(), it.size.height.toFloat())
                },
            seconds = seconds,
            tilt = { if (parallaxEnabled) tilt.value else Offset.Zero },
            detail = profile.sceneDetail,
            light = reveal
        )

        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth > maxHeight
                if (wide) {
                    LandscapeHome(
                        version = version,
                        fonts = fonts,
                        profile = profile,
                        revealed = revealed,
                        sceneSize = sceneSize.value,
                        screenW = maxWidth,
                        screenH = maxHeight,
                        onArena = onPickArena,
                        onChronicle = onPickChronicle,
                        onAtelier = onPickAtelier,
                        onSettings = { settingsOpen = true },
                        onCredits = { creditsOpen = true }
                    )
                } else {
                    PortraitHome(
                        version = version,
                        fonts = fonts,
                        profile = profile,
                        revealed = revealed,
                        sceneSize = sceneSize.value,
                        screenW = maxWidth,
                        screenH = maxHeight,
                        onArena = onPickArena,
                        onChronicle = onPickChronicle,
                        onAtelier = onPickAtelier,
                        onSettings = { settingsOpen = true },
                        onCredits = { creditsOpen = true }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = settingsOpen,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsOverlay(
                settings = settings,
                profile = profile,
                fonts = fonts,
                version = version,
                onClose = { settingsOpen = false }
            )
        }

        AnimatedVisibility(
            visible = creditsOpen,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize()
        ) {
            CreditsOverlay(
                fonts = fonts,
                version = version,
                profile = profile,
                onClose = { creditsOpen = false }
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Landscape: 61.8% temple / 38.2% gates                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun LandscapeHome(
    version: String,
    fonts: OlympusFonts,
    profile: GlassProfile,
    revealed: Boolean,
    sceneSize: Size,
    screenW: Dp,
    screenH: Dp,
    onArena: () -> Unit,
    onChronicle: () -> Unit,
    onAtelier: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit
) {
    val dense = screenH < 470.dp
    val compact = screenH < 560.dp
    val gap = when {
        dense -> 7.dp
        compact -> 9.dp
        else -> 13.dp
    }
    val titleSize = (screenH.value * (if (dense) 0.066f else 0.075f)).coerceIn(19f, 34f).sp
    val primaryHeight = when {
        dense -> 60.dp
        compact -> 62.dp
        else -> 72.dp
    }

    Row(Modifier.fillMaxSize()) {
        Box(Modifier.weight(0.618f).fillMaxHeight()) {
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 30.dp, top = 26.dp)
                    .entrance(rememberEntrance(0, revealed), 10.dp, vertical = true)
            ) {
                Txt(
                    "GATES OF OLYMPUS",
                    fonts.display,
                    7.sp,
                    Palette.StoneDeep.copy(alpha = 0.85f),
                    spacing = 3.4.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Diamond(Palette.BronzeDim.copy(alpha = 0.7f), Modifier.width(8.dp))
                    Spacer(Modifier.width(6.dp))
                    Txt("OLYMPVS", fonts.display, 6.5.sp, Palette.StoneDeep.copy(alpha = 0.7f), spacing = 3.sp)
                }
            }
        }

        SidePanel(
            modifier = Modifier.weight(0.382f).fillMaxHeight(),
            version = version,
            fonts = fonts,
            profile = profile,
            revealed = revealed,
            sceneSize = sceneSize,
            dense = dense,
            gap = gap,
            screenH = screenH,
            titleSize = titleSize,
            primaryHeight = primaryHeight,
            topPadding = if (dense) 16.dp else if (compact) 22.dp else 30.dp,
            onArena = onArena,
            onChronicle = onChronicle,
            onAtelier = onAtelier,
            onSettings = onSettings,
            onCredits = onCredits
        )
    }
}

@Composable
private fun SidePanel(
    modifier: Modifier,
    version: String,
    fonts: OlympusFonts,
    profile: GlassProfile,
    revealed: Boolean,
    sceneSize: Size,
    dense: Boolean,
    gap: Dp,
    screenH: Dp,
    titleSize: TextUnit,
    primaryHeight: Dp,
    topPadding: Dp,
    onArena: () -> Unit,
    onChronicle: () -> Unit,
    onAtelier: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp)
    AetherSurface(
        modifier = modifier.onGloballyPositioned { origin = it.positionInRoot() },
        profile = profile,
        shape = shape,
        panelOrigin = origin,
        sceneSize = sceneSize,
        tint = AetherTint.Panel
    ) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .widthIn(max = 430.dp)
                .fillMaxWidth()
                .heightIn(min = (screenH - 1.dp).coerceAtLeast(0.dp))
                .padding(start = if (dense) 24.dp else 34.dp, end = 22.dp, top = topPadding, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically)
        ) {
            Box(Modifier.entrance(rememberEntrance(1, revealed))) {
                BrandBlock(version, fonts, titleSize, compact = true, dense = dense)
            }
            Box(Modifier.entrance(rememberEntrance(2, revealed))) {
                AetherGateButton(
                    modifier = Modifier.fillMaxWidth().height(primaryHeight),
                    fonts = fonts,
                    title = "开始征程",
                    latin = "ENTER OLYMPUS",
                    onClick = onArena
                )
            }
            GateRows(
                fonts = fonts,
                compact = true,
                revealed = revealed,
                startIndex = 3,
                onChronicle = onChronicle,
                onAtelier = onAtelier,
                onArena = onArena
            )
            AuxBlock(fonts, compact = true, dense = dense, revealed = revealed, index = 7, version = version, onSettings = onSettings, onCredits = onCredits)
        }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Portrait: temple above, gates below                                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun PortraitHome(
    version: String,
    fonts: OlympusFonts,
    profile: GlassProfile,
    revealed: Boolean,
    sceneSize: Size,
    screenW: Dp,
    screenH: Dp,
    onArena: () -> Unit,
    onChronicle: () -> Unit,
    onAtelier: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit
) {
    val compact = screenH < 720.dp
    val titleSize = (screenW.value * 0.098f).coerceIn(25f, 40f).sp
    val gap = if (compact) 8.dp else 11.dp

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(0.560f).fillMaxWidth()) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Palette.Void.copy(alpha = 0.30f),
                            0.40f to Palette.Void.copy(alpha = 0.72f),
                            1.0f to Palette.Void.copy(alpha = 0.96f)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 18.dp)
                    .entrance(rememberEntrance(1, revealed))
            ) {
                BrandBlock(version, fonts, titleSize, compact = true, dense = false)
            }
        }

        var origin by remember { mutableStateOf(Offset.Zero) }
        val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        AetherSurface(
            modifier = Modifier
                .weight(0.440f)
                .fillMaxWidth()
                .onGloballyPositioned { origin = it.positionInRoot() },
            profile = profile,
            shape = shape,
            panelOrigin = origin,
            sceneSize = sceneSize,
            tint = AetherTint.Sheet
        ) {
            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .widthIn(max = 430.dp)
                    .fillMaxWidth()
                    .heightIn(min = (screenH * 0.44f).coerceAtLeast(0.dp))
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically)
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Hairline(Modifier.width(34.dp), Palette.BronzeDim.copy(alpha = 0.6f))
                }
                Box(Modifier.entrance(rememberEntrance(2, revealed))) {
                    AetherGateButton(
                        modifier = Modifier.fillMaxWidth().height(if (compact) 60.dp else 66.dp),
                        fonts = fonts,
                        title = "开始征程",
                        latin = "ENTER OLYMPUS",
                        onClick = onArena
                    )
                }
                GateRows(
                    fonts = fonts,
                    compact = true,
                    revealed = revealed,
                    startIndex = 3,
                    onChronicle = onChronicle,
                    onAtelier = onAtelier,
                    onArena = onArena
                )
                AuxBlock(fonts, compact = true, dense = compact, revealed = revealed, index = 7, version = version, onSettings = onSettings, onCredits = onCredits)
            }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Shared blocks                                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun BrandBlock(
    version: String,
    fonts: OlympusFonts,
    titleSize: TextUnit,
    compact: Boolean,
    dense: Boolean
) {
    val kickerSize = when {
        dense -> 6.4.sp
        compact -> 6.8.sp
        else -> 7.6.sp
    }
    val ruleWidth = when {
        dense -> 14.dp
        compact -> 16.dp
        else -> 24.dp
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Hairline(Modifier.width(ruleWidth), Palette.BronzeDim.copy(alpha = 0.55f))
            Spacer(Modifier.width(7.dp))
            Txt(
                "THE GATES OF OLYMPUS",
                fonts.display,
                kickerSize,
                Palette.Bronze.copy(alpha = 0.92f),
                spacing = 3.2.sp
            )
            Spacer(Modifier.width(7.dp))
            Hairline(Modifier.width(ruleWidth), Palette.BronzeDim.copy(alpha = 0.55f))
        }
        Spacer(Modifier.height(if (dense) 6.dp else if (compact) 8.dp else 12.dp))
        Txt(
            "奥林匹斯秘闻",
            FontFamily.Serif,
            titleSize,
            Palette.Ivory,
            spacing = (titleSize.value * 0.16f).sp,
            maxLines = 1
        )
        Spacer(Modifier.height(if (dense) 4.dp else if (compact) 5.dp else 7.dp))
        Txt(
            "ODYSSEY · MYTHIC ARENA",
            fonts.display,
            if (dense) 8.2.sp else if (compact) 8.6.sp else 10.sp,
            Palette.Stone,
            spacing = 4.2.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(if (dense) 5.dp else if (compact) 7.dp else 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Diamond(Palette.BronzeDim.copy(alpha = 0.65f), Modifier.width(7.dp))
            Spacer(Modifier.width(6.dp))
            Txt(
                "by NeverCryNow",
                fonts.display,
                if (dense) 7.6.sp else if (compact) 8.2.sp else 9.sp,
                Palette.Bronze.copy(alpha = 0.85f),
                spacing = 1.6.sp
            )
        }
        Spacer(Modifier.height(3.dp))
        Txt(
            "别看了 只有郑壹鸣 凡人的故事凡人写",
            FontFamily.Serif,
            if (dense) 7.0.sp else if (compact) 8.0.sp else 8.8.sp,
            Palette.StoneDeep.copy(alpha = 0.95f),
            spacing = 0.7.sp,
            maxLines = 2,
            softWrap = true
        )
    }
}

@Composable
private fun GateRows(
    fonts: OlympusFonts,
    compact: Boolean,
    revealed: Boolean,
    startIndex: Int,
    onChronicle: () -> Unit,
    onAtelier: () -> Unit,
    onArena: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.entrance(rememberEntrance(startIndex, revealed))) {
            AetherRow(
                modifier = Modifier.fillMaxWidth(),
                fonts = fonts,
                numeral = "I",
                title = "神话秘闻",
                latin = "OLYMPUS CHRONICLES",
                meta = "8 章主线 · 4 种结局 · 82 条图鉴",
                compact = compact,
                onClick = onChronicle
            )
        }
        AetherDivider(Modifier.padding(start = 12.dp))
        Box(Modifier.entrance(rememberEntrance(startIndex + 1, revealed))) {
            AetherRow(
                modifier = Modifier.fillMaxWidth(),
                fonts = fonts,
                numeral = "II",
                title = "神谕绘卷",
                latin = "ORACLE ATELIER",
                meta = "AI 绘卷 · 神话生成",
                compact = compact,
                onClick = onAtelier
            )
        }
        AetherDivider(Modifier.padding(start = 12.dp))
        Box(Modifier.entrance(rememberEntrance(startIndex + 2, revealed))) {
            AetherRow(
                modifier = Modifier.fillMaxWidth(),
                fonts = fonts,
                numeral = "III",
                title = "英雄殿堂",
                latin = "HEROES",
                meta = "选人界面 · 神恩图鉴",
                compact = compact,
                onClick = onArena
            )
        }
    }
}

@Composable
private fun AuxBlock(
    fonts: OlympusFonts,
    compact: Boolean,
    dense: Boolean,
    revealed: Boolean,
    index: Int,
    version: String,
    onSettings: () -> Unit,
    onCredits: () -> Unit
) {
    Column(Modifier.fillMaxWidth().entrance(rememberEntrance(index, revealed))) {
        AetherDivider(Modifier.padding(bottom = if (dense) 3.dp else 6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AetherAux(fonts, "设置", "SETTINGS", onClick = onSettings)
            Spacer(Modifier.width(if (dense) 8.dp else 14.dp))
            AetherAux(fonts, "制作名单", "CREDITS", onClick = onCredits)
            if (dense) {
                Spacer(Modifier.weight(1f))
                BronzeSeal()
                Spacer(Modifier.width(5.dp))
                Txt("v$version", fonts.display, 6.5.sp, Palette.StoneDeep.copy(alpha = 0.8f), spacing = 1.6.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt("图鉴 · 竞技场内", fonts.display, if (dense) 6.8.sp else 7.4.sp, Palette.StoneDeep.copy(alpha = 0.85f), spacing = 1.2.sp)
            Spacer(Modifier.width(if (dense) 10.dp else 14.dp))
            Txt("战绩存档 · 未开放", fonts.display, if (dense) 6.8.sp else 7.4.sp, Palette.StoneDeep.copy(alpha = 0.7f), spacing = 1.2.sp)
        }
        if (!dense) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BronzeSeal()
                Spacer(Modifier.width(7.dp))
                Txt("v$version · AETHER GLASS", fonts.display, 7.sp, Palette.StoneDeep.copy(alpha = 0.8f), spacing = 1.8.sp)
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Small animation helpers                                                    */
/* -------------------------------------------------------------------------- */

@Composable
private fun rememberEntrance(index: Int, revealed: Boolean): State<Float> {
    return animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 620,
            delayMillis = 90 + index * 75,
            easing = FastOutSlowInEasing
        ),
        label = "entrance$index"
    )
}

private fun Modifier.entrance(state: State<Float>, slide: Dp = 18.dp, vertical: Boolean = false): Modifier {
    return graphicsLayer {
        alpha = state.value
        if (vertical) {
            translationY = (1f - state.value) * slide.toPx()
        } else {
            translationX = (1f - state.value) * slide.toPx()
        }
    }
}
