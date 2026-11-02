# ODYSSEY · MYTHIC ARENA — APK Reconstruction Workspace

This is a **reconstructable APK workspace**, not the original Kotlin/Gradle project.
The original source is lost; the APK is the source of truth.

## Repository scope

This repo tracks the **source and documentation** needed to rebuild the home screen
(`homeui/`), the build pipeline, and the design notes — not the 8 GB working tree.
Excluded on purpose:

| Path | Size | Why |
| --- | --- | --- |
| `base/*.apk` | 1.3 GB | 622 MB per file, over GitHub's 100 MB file limit |
| `smali/` | 149 MB | decompiled bytecode, regenerable with apktool/baksmali |
| `jadx/` | 277 MB | decompiled Java reference, regenerable |
| `tools/` | 890 MB | smali/SDK toolchain |
| `build/` | 8 GB | generated |
| `sign/oracle.keystore` | — | signing secret, never uploaded |

To rebuild from a fresh clone, restore `base/current-v9.apk` + `tools/` locally
(see below), then run the build. The produced APK is distributed separately (GitHub
Releases / local file), never committed.

## Layout
- `base/oracle-original.apk` — untouched original (622 MB, classes2 + assets).
- `base/current-v9.apk` — latest working build (v9 credits, performance, myth AI cache).
- `smali/classes2/` — classes2.dex sources: MainActivity, Oracle Atelier, myth UI, AI engine, game.
  The home screen no longer lives here (see `homeui/`).
- `smali/classes1/` — classes.dex sources: renderer/game core.
- `jadx/sources/` — decompiled Java reference (read-only; not directly buildable).
- `assets/models/` — ONNX + tokenizer; `assets/from-apk/` — illustrations/story if extracted.
- `tools/apktool-tools` — smali/baksmali/jcommander; `sign/oracle.keystore`.
- `patch-src/` — Java overrides for AI engine (compiled via javac/D8).
- `feature-src/` — Java overrides for AI art/ARAM fixes.
- `homeui/` — **buildable Compose source module for the home screen** (`LauncherScreenKt`),
  compiled to `classes3.dex` and packaged automatically by `build.sh`.
- `docs/HOME-REBUILD.md` — THE GATES OF OLYMPUS redesign notes, verification and gaps.
- `docs/preview/` — desktop renders of the real home composables (landscape / portrait / tablet / sheets).

## Build

Prerequisites: `base/current-v9.apk` and `tools/apktool-tools` restored into the workspace
(not tracked), plus `ANDROID_JAR` and the `homeui` toolchain.

```bash
./build.sh                 # classes2 + homeui classes3.dex, default base current-v9
BUILD_CLASSES1=1 ./build.sh   # also classes.dex (slow)
BUILD_ASSETS=1 ./build.sh     # also models
BUILD_HOMEUI=0 ./build.sh     # skip the Compose home module
```

The home module needs a one-time toolchain download and an `android.jar`:

```bash
export ANDROID_JAR=/path/to/android-33/android.jar
JAVA_HOME=/path/to/jdk21 ./homeui/toolchain/setup.sh
```

Output: `build/odyssey-*-signed.apk`.

Signing: `oracle.keystore`, alias `oracle`, store/key password `oracle2026`.
Do **not** lose this keystore; updating the installed app depends on it.

## Key files
- Home UI: `homeui/src/com/hextech/aram/LauncherScreen.kt` + `homeui/src/com/hextech/aram/olympus/`
  (old smali kept in `smali-removed/launcher-v9/` for reference)
- MainActivity: `smali/classes2/com/hextech/aram/MainActivity*.smali`
- AI art UI: `smali/classes2/com/hextech/aram/aiart/`
- Myth/story UI: `smali/classes2/com/olympus/chronicles/`
- AI engine source: `patch-src/src/com/olympus/chronicles/ai/`
- ONNX model: `assets/models/` (tiny-SD)
- Story: extract from `base/*.apk` `assets/story/`
- Illustrations: `assets/from-apk/illustrations/`

## Known state
- v9: credits added, AI cache bridge, prompt v2, adaptive performance.
- Myth AI: generation requires enabling local AI + autoOnEvents; results cache into `ChronicleApp.bitmaps`.
- ARAM stable: no BotBrain/Fx/high-poly GLB in current base.
- Known limitation: tiny-SD 512 quality; high-quality weights absent.
- Home UI: THE GATES OF OLYMPUS refactor implemented in `homeui/` (golden-ratio split,
  AETHER GLASS with three real performance tiers, portrait bottom sheet, staggered entrance,
  settings + credits sheets). See `docs/HOME-REBUILD.md`. Not yet verified on a device
  (no adb/emulator; Shizuku unauthorized).

## Refactor workflow
1. Edit smali or Java override.
2. `./build.sh`.
3. Install with same signature over previous build.
4. Large UI work goes in `homeui/` (Compose source), not into smali; `build.sh` packages it
   as `classes3.dex`. Run `homeui/preview/render.sh` for desktop layout screenshots.
