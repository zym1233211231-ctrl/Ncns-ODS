#!/bin/bash
# Desktop render of the REAL home code (Palette/LocalView/BackHandler are stubbed).
# Extra deps (not needed for the APK build): Compose Desktop 1.7.0 jars + skiko + a
# fontconfig-visible font set. See preview/README.md.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PREV="$ROOT/homeui/preview"
TC="${HOMEUI_TOOLCHAIN:-$ROOT/homeui/.toolchain}"
JAVA="${JAVA_HOME:-/root/atools/root/usr/lib/jvm/java-21-openjdk-arm64}/bin/java"
DESKTOP_LIBS="${DESKTOP_LIBS:?set DESKTOP_LIBS to the Compose Desktop 1.7.0 jars dir}"
DL="$(ls "$DESKTOP_LIBS"/*.jar | tr '\n' ':')"
CP="${DL}$TC/dl/kotlin-stdlib.jar:$TC/dl/annotations.jar:$TC/dl/coroutines.jar"

rm -rf "$PREV/out" && mkdir -p "$PREV/out"
"$JAVA" -Xmx2500m -cp "$TC/dl/kc.jar:$TC/dl/kotlin-stdlib.jar:$TC/dl/annotations.jar:$TC/dl/coroutines.jar:$TC/dl/trove4j.jar" \
  org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -nowarn -jvm-target 1.8 \
  -classpath "$CP" -Xplugin="$TC/dl/compose-plugin.jar" -d "$PREV/out" \
  "$PREV/stubs/Stubs.kt" "$PREV/stubs/AndroidStubs.kt" "$PREV/stubs/ActivityStubs.kt" "$PREV/stubs/ViewStubs.kt" \
  "$PREV/src/PreviewHome.kt" \
  "$ROOT/homeui/src/com/hextech/aram/LauncherScreen.kt" \
  "$ROOT/homeui/src/com/hextech/aram/olympus/Aether.kt" \
  "$ROOT/homeui/src/com/hextech/aram/olympus/Chrome.kt" \
  "$ROOT/homeui/src/com/hextech/aram/olympus/Overlays.kt" \
  "$ROOT/homeui/src/com/hextech/aram/olympus/Temple.kt"

"$JAVA" -Djava.awt.headless=true -Dskiko.renderApi=SOFTWARE -cp "$PREV/out:$CP" preview.PreviewHomeKt
