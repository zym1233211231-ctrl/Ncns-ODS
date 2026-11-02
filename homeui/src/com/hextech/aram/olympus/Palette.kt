package com.hextech.aram.olympus

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

/**
 * THE GATES OF OLYMPUS — palette.
 *
 * Deep graphite, sea blue, marble, ivory and a very low-saturation bronze.
 * Nothing here is a pure black or a saturated gold: the home screen must read
 * as carved stone and cold sea air, not as a game-store banner.
 */
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

/** Visual quality of the AETHER GLASS panels. Three real levels, no pretend blur. */
enum class GlassTier { HIGH, BALANCED, LOW }

/**
 * Result of the device capability probe. `renderEffect` is the real gate for
 * background transmission/refraction: Compose can only blur through
 * RenderEffect on API 31+, so below that the home screen never claims to blur.
 */
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
    val tierLabel: String
        get() = when (tier) {
            GlassTier.HIGH -> "HIGH"
            GlassTier.BALANCED -> "BALANCED"
            GlassTier.LOW -> "LOW"
        }
}

object GlassProfiles {
    private const val PREF_AUTO = "auto"

    fun detect(context: Context, preference: String): GlassProfile {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val lowRam = am?.isLowRamDevice ?: false
        val cores = Runtime.getRuntime().availableProcessors()
        val mem = am?.memoryClass ?: 128
        val sdk = Build.VERSION.SDK_INT
        val renderEffect = sdk >= 31 && !lowRam

        val auto = when {
            renderEffect && cores >= 6 && mem >= 192 -> GlassTier.HIGH
            renderEffect -> GlassTier.BALANCED
            else -> GlassTier.LOW
        }
        val tier = when (preference) {
            "high" -> if (renderEffect) GlassTier.HIGH else GlassTier.BALANCED
            "balanced" -> GlassTier.BALANCED
            "low" -> GlassTier.LOW
            PREF_AUTO -> auto
            else -> auto
        }
        val blur = when (tier) {
            GlassTier.HIGH -> 26
            GlassTier.BALANCED -> if (renderEffect) 12 else 0
            GlassTier.LOW -> 0
        }
        val detail = when (tier) {
            GlassTier.HIGH -> 2
            GlassTier.BALANCED -> 1
            GlassTier.LOW -> 0
        }
        return GlassProfile(tier, sdk, lowRam, cores, mem, renderEffect, blur, detail)
    }
}

/**
 * Small, real, persisted preferences owned by the home screen: glass quality,
 * ambient motion, pointer parallax. Stored in SharedPreferences so the choice
 * survives process death instead of being a decorative switch.
 */
class HomeSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("olympus.home.v1", Context.MODE_PRIVATE)

    private val qualityState: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_GLASS, VALUE_AUTO) ?: VALUE_AUTO)
    private val motionState: MutableState<Boolean> =
        mutableStateOf(prefs.getBoolean(KEY_MOTION, true))
    private val parallaxState: MutableState<Boolean> =
        mutableStateOf(prefs.getBoolean(KEY_PARALLAX, true))

    val quality: String get() = qualityState.value
    val motion: Boolean get() = motionState.value
    val parallax: Boolean get() = parallaxState.value

    fun setQuality(value: String) {
        if (qualityState.value == value) return
        qualityState.value = value
        prefs.edit().putString(KEY_GLASS, value).apply()
    }

    fun setMotion(value: Boolean) {
        if (motionState.value == value) return
        motionState.value = value
        prefs.edit().putBoolean(KEY_MOTION, value).apply()
    }

    fun setParallax(value: Boolean) {
        if (parallaxState.value == value) return
        parallaxState.value = value
        prefs.edit().putBoolean(KEY_PARALLAX, value).apply()
    }

    companion object {
        const val VALUE_AUTO = "auto"
        const val VALUE_HIGH = "high"
        const val VALUE_BALANCED = "balanced"
        const val VALUE_LOW = "low"
        private const val KEY_GLASS = "glass"
        private const val KEY_MOTION = "motion"
        private const val KEY_PARALLAX = "parallax"
    }
}

/** Display face: EB Garamond shipped in assets/fonts, system serif as fallback. */
class OlympusFonts(val display: FontFamily, val hasDisplayFace: Boolean)

@Composable
fun rememberOlympusFonts(): OlympusFonts {
    val context = LocalContext.current
    return remember(context) {
        val typeface: Typeface? = runCatching {
            Typeface.createFromAsset(context.assets, "fonts/EBGaramond.ttf")
        }.getOrNull()
        OlympusFonts(
            display = if (typeface != null) FontFamily(typeface) else FontFamily.Serif,
            hasDisplayFace = typeface != null
        )
    }
}
