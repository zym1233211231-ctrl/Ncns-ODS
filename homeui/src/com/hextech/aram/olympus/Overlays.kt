package com.hextech.aram.olympus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings sheet. Every control here is wired to [HomeSettings] and changes
 * something real: glass tier, ambient animation, pointer parallax. The probe
 * values come from the same [GlassProfile] the renderer uses.
 */
@Composable
fun SettingsOverlay(
    settings: HomeSettings,
    profile: GlassProfile,
    fonts: OlympusFonts,
    version: String,
    onClose: () -> Unit
) {
    ModalScrim(onClose)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AetherSurface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            profile = profile,
            shape = RoundedCornerShape(3.dp),
            panelOrigin = Offset.Zero,
            sceneSize = Size.Zero,
            tint = AetherTint.Dialog
        ) {
            Column(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 22.dp)
                ) {
                    Txt("AETHER GLASS", fonts.display, 8.sp, Palette.Bronze, spacing = 3.4.sp)
                    Spacer(Modifier.height(10.dp))
                    Txt("显示与动效", fonts.display, 22.sp, Palette.Ivory, spacing = 3.sp)
                    Spacer(Modifier.height(4.dp))
                    Txt("DISPLAY · MOTION", fonts.display, 8.sp, Palette.StoneDim, spacing = 3.sp)
                    Spacer(Modifier.height(16.dp))
                    AetherDivider()
                    Spacer(Modifier.height(16.dp))

                    Txt("玻璃质感", fonts.display, 12.sp, Palette.Ivory, spacing = 2.sp)
                    Spacer(Modifier.height(3.dp))
                    Txt(
                        "实时折射需要设备支持 RenderEffect（Android 12+）。",
                        fonts.display, 9.sp, Palette.StoneDim,
                        spacing = 0.8.sp, maxLines = 2, softWrap = true
                    )
                    Spacer(Modifier.height(10.dp))
                    QualityChoices(settings, fonts)
                    Spacer(Modifier.height(18.dp))
                    AetherDivider()
                    Spacer(Modifier.height(14.dp))

                    ToggleRow("动态光影", "AMBIENT LIGHT", settings.motion, fonts) { settings.setMotion(!settings.motion) }
                    ToggleRow("轻微视差", "PARALLAX", settings.parallax, fonts) { settings.setParallax(!settings.parallax) }

                    Spacer(Modifier.height(16.dp))
                    AetherDivider()
                    Spacer(Modifier.height(14.dp))
                    InfoLine("当前档位", profile.tierLabel, fonts)
                    InfoLine("实时折射", if (profile.renderEffect) "支持" else "不可用", fonts)
                    InfoLine("设备", "API ${profile.sdk} · ${profile.cores} 核 · ${profile.memoryClass}MB", fonts)
                    InfoLine("版本", version, fonts)
                    Spacer(Modifier.height(10.dp))
                }
                Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp)) {
                    DialogClose("关闭 / CLOSE", fonts, onClose)
                }
            }
        }
    }
}

@Composable
private fun QualityChoices(settings: HomeSettings, fonts: OlympusFonts) {
    val options = listOf(
        HomeSettings.VALUE_AUTO to "自动",
        HomeSettings.VALUE_HIGH to "高",
        HomeSettings.VALUE_BALANCED to "均衡",
        HomeSettings.VALUE_LOW to "低"
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        for ((value, label) in options) {
            val selected = settings.quality == value
            Column(
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { settings.setQuality(value) }
            ) {
                Txt(
                    label, fonts.display, 13.sp,
                    if (selected) Palette.Ivory else Palette.StoneDim,
                    spacing = 2.sp
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier
                        .width(if (selected) 26.dp else 14.dp)
                        .height(1.dp)
                        .background(if (selected) Palette.Bronze else Palette.StoneDeep.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    latin: String,
    on: Boolean,
    fonts: OlympusFonts,
    onToggle: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onToggle() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Txt(label, fonts.display, 12.sp, Palette.Ivory, spacing = 2.sp)
            Spacer(Modifier.height(2.dp))
            Txt(latin, fonts.display, 7.sp, Palette.StoneDeep, spacing = 3.sp)
        }
        Txt(if (on) "开" else "关", fonts.display, 11.sp, if (on) Palette.Bronze else Palette.StoneDeep, spacing = 2.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .width(30.dp)
                .height(1.dp)
                .background(if (on) Palette.Bronze.copy(alpha = 0.9f) else Palette.StoneDeep.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String, fonts: OlympusFonts) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(label, fonts.display, 10.sp, Palette.StoneDim, spacing = 1.6.sp, modifier = Modifier.width(84.dp))
        Txt(value, fonts.display, 10.sp, Palette.Stone, spacing = 1.2.sp, maxLines = 1)
    }
}

@Composable
fun CreditsOverlay(
    fonts: OlympusFonts,
    version: String,
    profile: GlassProfile,
    onClose: () -> Unit
) {
    ModalScrim(onClose)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AetherSurface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            profile = profile,
            shape = RoundedCornerShape(3.dp),
            panelOrigin = Offset.Zero,
            sceneSize = Size.Zero,
            tint = AetherTint.Dialog
        ) {
            Column(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 26.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Txt("THE GATES OF OLYMPUS", fonts.display, 7.5.sp, Palette.Bronze, spacing = 3.6.sp, align = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Txt("奥林匹斯秘闻", FontFamily.Serif, 24.sp, Palette.Ivory, spacing = 5.sp, align = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Txt("ODYSSEY · MYTHIC ARENA", fonts.display, 9.sp, Palette.Stone, spacing = 3.6.sp, align = TextAlign.Center, maxLines = 1)
                    Spacer(Modifier.height(18.dp))
                    RuledOrnament(Modifier.fillMaxWidth(0.7f), Palette.BronzeDim.copy(alpha = 0.4f), Palette.Bronze.copy(alpha = 0.7f))
                    Spacer(Modifier.height(18.dp))
                    Txt("by NeverCryNow", fonts.display, 12.sp, Palette.Bronze, spacing = 2.sp, align = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Txt(
                        "别看了 只有郑壹鸣 凡人的故事凡人写",
                        FontFamily.Serif, 11.sp, Palette.Stone,
                        spacing = 1.2.sp, maxLines = 2, softWrap = true, align = TextAlign.Center
                    )
                    Spacer(Modifier.height(22.dp))
                    AetherDivider()
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BronzeSeal()
                        Spacer(Modifier.width(8.dp))
                        Txt("v$version", fonts.display, 9.sp, Palette.StoneDim, spacing = 2.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Box(Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 12.dp)) {
                    DialogClose("关闭 / CLOSE", fonts, onClose)
                }
            }
        }
    }
}

@Composable
private fun DialogClose(label: String, fonts: OlympusFonts, onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Brush.horizontalGradient(listOf(Palette.BronzeDeep.copy(alpha = 0.28f), Palette.Graphite2.copy(alpha = 0.5f))))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Txt(label, fonts.display, 10.sp, Palette.Stone, spacing = 3.sp, align = TextAlign.Center)
    }
}

@Composable
private fun ModalScrim(onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.Void.copy(alpha = 0.78f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
    )
}
