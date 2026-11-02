# Desktop preview harness (optional)

Renders `LauncherScreen` / `SettingsOverlay` / `CreditsOverlay` with the **same Kotlin
sources** that ship in classes3.dex, so layout and overflow can be checked without a
device. Only the Android-only pieces of `Palette.kt` (SharedPreferences, ActivityManager,
Typeface) and `LocalView` / `BackHandler` are replaced by stubs in `stubs/`.

This harness is *not* part of the APK build and is not a substitute for installing on a
device.

## Dependencies

1. `homeui/toolchain/setup.sh` (Kotlin compiler + Compose plugin).
2. Compose Desktop 1.7.0 jars from Maven Central, in one directory (`DESKTOP_LIBS`):
   `ui-desktop`, `ui-graphics-desktop`, `ui-text-desktop`, `ui-unit-desktop`,
   `ui-geometry-desktop`, `ui-util-desktop`, `runtime-desktop`, `runtime-saveable-desktop`,
   `foundation-desktop`, `foundation-layout-desktop`, `animation-desktop`,
   `animation-core-desktop`, `skiko-awt`, `skiko-awt-runtime-linux-arm64`,
   `androidx.collection:collection-jvm:1.4.0`.
3. A working fontconfig + a serif font containing CJK (or `FONTCONFIG_FILE` pointing at a
   config that lists the system font dir), and OpenGL/EGL shared libraries for skiko
   (`LD_LIBRARY_PATH`), even in software rendering mode.

## Run

```bash
export DESKTOP_LIBS=/path/to/compose-desktop-jars
export LD_LIBRARY_PATH=/path/to/gl-libs:$LD_LIBRARY_PATH
export FONTCONFIG_FILE=/path/to/fonts.conf
./homeui/preview/render.sh
```

Outputs `home-*.png` / `sheet-*.png` in the current working directory.
