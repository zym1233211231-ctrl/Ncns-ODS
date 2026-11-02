package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf

class StubHostView {
    val isShown: Boolean get() = true
    val windowVisibility: Int get() = 0
}

val LocalView = staticCompositionLocalOf { StubHostView() }
val LocalContext = staticCompositionLocalOf<Any> { Any() }

const val VISIBLE = 0
