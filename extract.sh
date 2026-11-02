#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
APK="${1:-$ROOT/base/current-v9.apk}"
export JAVA_HOME=/root/atools/root/usr/lib/jvm/java-21-openjdk-arm64
JAVA="$JAVA_HOME/bin/java"
TOOLS="$ROOT/tools/apktool-tools"
CP="$(find "$TOOLS/apktool/usr/share/apktool" "$TOOLS/apktool/usr/share/java" -name '*.jar' | tr '\n' ':')$TOOLS/jcommander.jar"
rm -rf "$ROOT/build/extract"; mkdir -p "$ROOT/build/extract"
unzip -q "$APK" -d "$ROOT/build/extract/apk"
for d in classes.dex classes2.dex; do
  [ -f "$ROOT/build/extract/apk/$d" ] || continue
  out="$ROOT/build/extract/smali_${d%.dex}"
  "$JAVA" -cp "$CP" org.jf.baksmali.Main d -o "$out" "$ROOT/build/extract/apk/$d"
done
echo "extracted to $ROOT/build/extract"
