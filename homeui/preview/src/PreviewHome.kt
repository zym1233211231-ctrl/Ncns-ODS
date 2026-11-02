package preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import com.hextech.aram.LauncherScreen
import com.hextech.aram.olympus.CreditsOverlay
import com.hextech.aram.olympus.GlassProfiles
import com.hextech.aram.olympus.HomeSettings
import com.hextech.aram.olympus.SettingsOverlay
import com.hextech.aram.olympus.drawOlympusScene
import com.hextech.aram.olympus.rememberOlympusFonts
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

fun main() {
    render("/root/work/preview/home-land.png", 2400, 1080, 2.75f) {
        LauncherScreen("1.0", {}, {}, {})
    }
    render("/root/work/preview/home-port.png", 1080, 2400, 2.75f) {
        LauncherScreen("1.0", {}, {}, {})
    }
    render("/root/work/preview/home-tab.png", 2560, 1600, 2.0f) {
        LauncherScreen("1.0", {}, {}, {})
    }
    render("/root/work/preview/sheet-settings.png", 2400, 1080, 2.75f) {
        SettingsOverlay(
            settings = HomeSettings(Any()),
            profile = GlassProfiles.detect(Any(), "high"),
            fonts = rememberOlympusFonts(),
            version = "1.0",
            onClose = {}
        )
    }
    render("/root/work/preview/sheet-credits.png", 1080, 2400, 2.75f) {
        CreditsOverlay(
            fonts = rememberOlympusFonts(),
            version = "1.0",
            profile = GlassProfiles.detect(Any(), "high"),
            onClose = {}
        )
    }
    println("rendered all")
}

private fun render(path: String, w: Int, h: Int, density: Float, content: @Composable () -> Unit) {
    val scene = ImageComposeScene(width = w, height = h, density = Density(density)) {
        Box(Modifier.fillMaxSize().background(Color(0xFF080A0D.toInt()))) {
            Canvas(Modifier.fillMaxSize()) {
                drawOlympusScene(size, 8f, Offset.Zero, 2, 1f)
            }
            content()
        }
    }
    var image = scene.render(0L)
    for (i in 1..150) {
        image = scene.render(i * 16_666_667L)
    }
    File(path).writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    scene.close()
}
