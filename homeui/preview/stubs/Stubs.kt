package com.hextech.aram.olympus

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlin.jvm.JvmName

// Desktop mirrors of the Android-only declarations in Palette.kt.
object Palette {
    val Void = Color(0xFF06080B.toInt())
    val Graphite = Color(0xFF0D1015.toInt())
    val Graphite2 = Color(0xFF141A22.toInt())
    val Slate = Color(0xFF1B2430.toInt())
    val SeaDeep = Color(0xFF14202B.toInt())
    val Sea = Color(0xFF243646.toInt())
    val SeaGlow = Color(0xFF4A6172.toInt())
    val MarbleLit = Color(0xFFEFE9DC.toInt())
    val Marble = Color(0xFFD9D2C3.toInt())
    val MarbleMid = Color(0xFFB6AF9F.toInt())
    val MarbleShade = Color(0xFF847E6D.toInt())
    val Ivory = Color(0xFFF5F0E6.toInt())
    val Stone = Color(0xFFAFA89A.toInt())
    val StoneDim = Color(0xFF8A8476.toInt())
    val StoneDeep = Color(0xFF655F53.toInt())
    val Bronze = Color(0xFFB18A55.toInt())
    val BronzeDim = Color(0xFF8A6B42.toInt())
    val BronzeDeep = Color(0xFF57452C.toInt())
    val Gold = Color(0xFFD8B87F.toInt())
    val Ember = Color(0xFFE9A05B.toInt())
    val SeaGlass = Color(0xFF7FA0B4.toInt())
}

enum class GlassTier { HIGH, BALANCED, LOW }

data class GlassProfile(
    val tier: GlassTier,
    val sdk: Int,
    val lowRam: Boolean,
    val cores: Int,
    val memoryClass: Int,
    val renderEffect: Boolean,
    val blurDp: Int,
    val sceneDetail: Int
) {
    val tierLabel: String get() = tier.name
}

object GlassProfiles {
    fun detect(context: Any, preference: String): GlassProfile =
        GlassProfile(GlassTier.HIGH, 33, false, 8, 256, true, 26, 2)
}

class HomeSettings(context: Any) {
    var quality: String = "auto"
    var motion: Boolean = true
    var parallax: Boolean = true
    @JvmName("applyQuality") fun setQuality(value: String) { quality = value }
    @JvmName("applyMotion") fun setMotion(value: Boolean) { motion = value }
    @JvmName("applyParallax") fun setParallax(value: Boolean) { parallax = value }
    companion object {
        const val VALUE_AUTO = "auto"
        const val VALUE_HIGH = "high"
        const val VALUE_BALANCED = "balanced"
        const val VALUE_LOW = "low"
    }
}

class OlympusFonts(val display: FontFamily, val hasDisplayFace: Boolean)

@Composable
fun rememberOlympusFonts(): OlympusFonts = OlympusFonts(FontFamily.Serif, false)
