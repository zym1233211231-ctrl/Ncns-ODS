#!/bin/bash
# Fetches the Kotlin/Compose toolchain used by homeui/build-home.sh into
# $HOMEUI_TOOLCHAIN (default: <repo>/homeui/.toolchain).
#
# Requirements on the host:
#   * JDK 21 (JAVA_HOME)
#   * an Android SDK android.jar, exported as ANDROID_JAR
#   * curl
# The artifact set is pinned: Kotlin 2.1.0, Compose 1.7.0, activity-compose 1.9.2,
# R8 8.5.35 — matching the Compose runtime shipped inside the APK.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TC="${HOMEUI_TOOLCHAIN:-$ROOT/homeui/.toolchain}"
CENTRAL="https://repo1.maven.org/maven2"
GOOGLE="${MAVEN_GOOGLE:-https://dl.google.com/dl/android/maven2}"

mkdir -p "$TC/dl" "$TC/libs"

fetch() { # url dest
  if [ -s "$2" ]; then echo "  cached $(basename "$2")"; return; fi
  echo "  get $1"
  curl -fsSL --retry 3 --max-time 600 -o "$2" "$1"
}

echo "[1/3] kotlin compiler + compose plugin"
fetch "$CENTRAL/org/jetbrains/kotlin/kotlin-compiler-embeddable/2.1.0/kotlin-compiler-embeddable-2.1.0.jar" "$TC/dl/kc.jar"
fetch "$CENTRAL/org/jetbrains/kotlin/kotlin-compose-compiler-plugin-embeddable/2.1.0/kotlin-compose-compiler-plugin-embeddable-2.1.0.jar" "$TC/dl/compose-plugin.jar"
fetch "$CENTRAL/org/jetbrains/kotlin/kotlin-stdlib/2.1.0/kotlin-stdlib-2.1.0.jar" "$TC/dl/kotlin-stdlib.jar"
fetch "$CENTRAL/org/jetbrains/intellij/deps/trove4j/1.0.20200330/trove4j-1.0.20200330.jar" "$TC/dl/trove4j.jar"
fetch "$CENTRAL/org/jetbrains/annotations/13.0/annotations-13.0.jar" "$TC/dl/annotations.jar"
fetch "$CENTRAL/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.7.3/kotlinx-coroutines-core-jvm-1.7.3.jar" "$TC/dl/coroutines.jar"
fetch "$GOOGLE/com/android/tools/r8/8.5.35/r8-8.5.35.jar" "$TC/dl/r8.jar"

echo "[2/3] androidx AARs (classes.jar extracted into libs/)"
aars="
androidx/compose/runtime/runtime-android/1.7.0/runtime-android-1.7.0.aar
androidx/compose/runtime/runtime-saveable-android/1.7.0/runtime-saveable-android-1.7.0.aar
androidx/compose/ui/ui-android/1.7.0/ui-android-1.7.0.aar
androidx/compose/ui/ui-graphics-android/1.7.0/ui-graphics-android-1.7.0.aar
androidx/compose/ui/ui-text-android/1.7.0/ui-text-android-1.7.0.aar
androidx/compose/ui/ui-unit-android/1.7.0/ui-unit-android-1.7.0.aar
androidx/compose/ui/ui-geometry-android/1.7.0/ui-geometry-android-1.7.0.aar
androidx/compose/ui/ui-util-android/1.7.0/ui-util-android-1.7.0.aar
androidx/compose/foundation/foundation-android/1.7.0/foundation-android-1.7.0.aar
androidx/compose/foundation/foundation-layout-android/1.7.0/foundation-layout-android-1.7.0.aar
androidx/compose/animation/animation-android/1.7.0/animation-android-1.7.0.aar
androidx/compose/animation/animation-core-android/1.7.0/animation-core-android-1.7.0.aar
androidx/compose/material/material-ripple-android/1.7.0/material-ripple-android-1.7.0.aar
androidx/activity/activity-compose/1.9.2/activity-compose-1.9.2.aar
"
tmp="$TC/dl/aar"
mkdir -p "$tmp"
for path in $aars; do
  name="$(basename "$path" .aar)"
  fetch "$GOOGLE/$path" "$tmp/$name.aar"
  if [ ! -s "$TC/libs/$name.jar" ]; then
    ( cd "$tmp" && unzip -o -q "$name.aar" classes.jar && mv classes.jar "$TC/libs/$name.jar" )
  fi
done

echo "[3/3] summary"
[ -f "$TC/dl/kc.jar" ] && [ -f "$TC/dl/compose-plugin.jar" ] || { echo "toolchain incomplete" >&2; exit 1; }
if [ -z "${ANDROID_JAR:-}" ] || [ ! -f "${ANDROID_JAR:-/nonexistent}" ]; then
  echo "WARNING: ANDROID_JAR is not set to a valid android.jar." >&2
  echo "         export ANDROID_JAR=\$ANDROID_SDK/platforms/android-33/android.jar" >&2
fi
echo "toolchain ready at $TC"
