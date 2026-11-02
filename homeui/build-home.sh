#!/bin/bash
# Compiles the Compose home module (homeui/src) and emits build/dex/classes3.dex.
# Requires the toolchain described in homeui/toolchain/setup.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TC="${HOMEUI_TOOLCHAIN:-$ROOT/homeui/.toolchain}"
JAVA="${JAVA_HOME:-/root/atools/root/usr/lib/jvm/java-21-openjdk-arm64}/bin/java"
ANDROID_JAR="${ANDROID_JAR:-/root/atools/dl/p33/android-13/android.jar}"
D8_JAR="${D8_JAR:-/root/atools/dl/r8.jar}"

if [ ! -f "$TC/dl/kc.jar" ]; then
  echo "homeui: toolchain missing at $TC — run homeui/toolchain/setup.sh first" >&2
  exit 2
fi

OUT="$ROOT/build/homeui"
rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex" "$ROOT/build/dex"

LIBS="$(ls "$TC"/libs/*.jar | tr '\n' ':')"
CP="${LIBS}${ANDROID_JAR}:$TC/dl/kotlin-stdlib.jar:$TC/dl/annotations.jar:$TC/dl/coroutines.jar"
KC_CP="$TC/dl/kc.jar:$TC/dl/kotlin-stdlib.jar:$TC/dl/annotations.jar:$TC/dl/coroutines.jar:$TC/dl/trove4j.jar"

echo "[homeui 1/3] kotlinc + compose plugin"
"$JAVA" -Xmx2800m -cp "$KC_CP" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -no-stdlib -nowarn -jvm-target 1.8 \
  -classpath "$CP" \
  -Xplugin="$TC/dl/compose-plugin.jar" \
  -d "$OUT/classes" \
  $(find "$ROOT/homeui/src" -name '*.kt' | sort)

echo "[homeui 2/3] D8"
"$JAVA" -Xmx2g -cp "$D8_JAR" com.android.tools.r8.D8 --release --min-api 26 \
  --lib "$ANDROID_JAR" --output "$OUT/dex" \
  $(find "$OUT/classes" -name '*.class' | sort)

echo "[homeui 3/3] classes3.dex"
cp "$OUT/dex/classes.dex" "$ROOT/build/dex/classes3.dex"
ls -l "$ROOT/build/dex/classes3.dex"
