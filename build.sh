#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME=/root/atools/root/usr/lib/jvm/java-21-openjdk-arm64
JAVA="$JAVA_HOME/bin/java"
TOOLS="$ROOT/tools/apktool-tools"
CP="$(find "$TOOLS/apktool/usr/share/apktool" "$TOOLS/apktool/usr/share/java" -name '*.jar' | tr '\n' ':')$TOOLS/jcommander.jar"
mkdir -p "$ROOT/build/dex"
BASE="${BASE_APK:-$ROOT/base/current-v9.apk}"
OUT="${OUT_APK:-$ROOT/build/odyssey-$(date +%H%M%S).apk}"
if [ "${BUILD_HOMEUI:-1}" = "1" ] && [ -x "$ROOT/homeui/build-home.sh" ]; then
  echo "[1a/5] compose home (homeui/src) -> classes3.dex"
  "$ROOT/homeui/build-home.sh"
fi
echo "[1/5] assembling classes2 smali -> dex"
rm -f "$ROOT/build/dex/classes2.dex"
"$JAVA" -Xmx8g -cp "$CP" org.jf.smali.Main a "$ROOT/smali/classes2" -o "$ROOT/build/dex/classes2.dex"
echo "[2/5] base -> out"
cp "$BASE" "$OUT"
cd "$ROOT/build/dex" && zip -q -u -9 "$OUT" classes2.dex
if [ -f "$ROOT/build/dex/classes3.dex" ]; then
  echo "[2a/5] packaging classes3.dex (compose home)"
  cd "$ROOT/build/dex" && zip -q -u -9 "$OUT" classes3.dex
fi
if [ -f "$ROOT/assets/fonts/EBGaramond.ttf" ]; then
  echo "[2a/5] packaging display font"
  cd "$ROOT" && zip -q -u -9 "$OUT" assets/fonts/EBGaramond.ttf
fi
if [ "${BUILD_CLASSES1:-0}" = "1" ]; then
  echo "[2b] assembling classes1 smali"
  rm -f "$ROOT/build/dex/classes.dex"
  "$JAVA" -Xmx8g -cp "$CP" org.jf.smali.Main a "$ROOT/smali/classes1" -o "$ROOT/build/dex/classes.dex"
  zip -q -u -9 "$OUT" classes.dex
fi
if [ "${BUILD_ASSETS:-0}" = "1" ]; then
  echo "[2c] updating assets"
  cd "$ROOT/assets/models" && zip -q -u -9 "$OUT" assets/models/*.glb 2>/dev/null || true
fi
echo "[3/5] zipalign"
export LD_LIBRARY_PATH=/root/atools/root/usr/lib:/root/atools/root/usr/lib/aarch64-linux-gnu:/root/atools/root/usr/lib/aarch64-linux-gnu/android:/root/atools/root/usr/lib/7zip
"$ROOT/tools/apktool-tools/../../atools/root/usr/lib/android-sdk/build-tools/debian/zipalign" -p -f 4 "$OUT" "$ROOT/build/aligned.apk" 2>/dev/null || \
/root/atools/root/usr/lib/android-sdk/build-tools/debian/zipalign -p -f 4 "$OUT" "$ROOT/build/aligned.apk"
echo "[4/5] sign"
"$JAVA" -jar /root/atools/root/usr/share/java/apksigner.jar sign \
  --ks "$ROOT/sign/oracle.keystore" --ks-key-alias oracle \
  --ks-pass pass:oracle2026 --key-pass pass:oracle2026 \
  --out "${OUT%.apk}-signed.apk" "$ROOT/build/aligned.apk"
echo "[5/5] verify"
"$JAVA" -jar /root/atools/root/usr/share/java/apksigner.jar verify --verbose "${OUT%.apk}-signed.apk" | head -6
sha256sum "${OUT%.apk}-signed.apk"
