#!/bin/sh
# Run this on a PC/Linux/Mac with Android SDK + JDK 17 installed.
# Android Studio: you can just use Build > Build APK(s) instead.
set -e

cd "$(dirname "$0")"

if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
  echo "Set ANDROID_HOME to your Android SDK path first." >&2
  echo "  export ANDROID_HOME=\$HOME/Android/Sdk" >&2
  exit 1
fi

if [ -x ./gradlew ]; then
  ./gradlew assembleDebug
elif command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug
else
  echo "No gradlew and no gradle found. Open this folder in Android Studio once" >&2
  echo "to generate the wrapper, then re-run this script." >&2
  exit 1
fi

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  cp "$APK" ./AutoReply-debug.apk
  echo "✓ APK ready: $(pwd)/AutoReply-debug.apk"
else
  echo "Build finished but APK not found at $APK" >&2
  exit 1
fi
