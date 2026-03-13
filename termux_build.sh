#!/data/data/com.termux/files/usr/bin/bash
set -e

# ─────────────────────────────────────────────
#  TwinMind Local — Termux Build Script
#  Run once on a fresh Termux install:
#    chmod +x termux_build.sh && ./termux_build.sh
# ─────────────────────────────────────────────

ANDROID_SDK="$HOME/android-sdk"
CMDLINE_TOOLS="$ANDROID_SDK/cmdline-tools/latest"
REPO_DIR="$HOME/twinmind-local"

echo ""
echo "===== Step 1: Update packages ====="
pkg update -y && pkg upgrade -y

echo ""
echo "===== Step 2: Install dependencies ====="
# openjdk-17 is not in Termux repos; openjdk-21 compiles Java 17 targets fine
pkg install -y git wget unzip openjdk-21

echo ""
echo "===== Step 3: Set JAVA_HOME ====="
export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"
export PATH="$JAVA_HOME/bin:$PATH"
java -version

echo ""
echo "===== Step 4: Download Android command-line tools ====="
mkdir -p "$ANDROID_SDK/cmdline-tools"
cd "$ANDROID_SDK/cmdline-tools"

if [ ! -f "cmdline-tools.zip" ]; then
  wget -q --show-progress \
    "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    -O cmdline-tools.zip
fi

echo "Extracting..."
unzip -q -o cmdline-tools.zip
# Google zips it as "cmdline-tools/" — rename to "latest"
[ -d "cmdline-tools" ] && mv cmdline-tools latest

echo ""
echo "===== Step 5: Set ANDROID_HOME and PATH ====="
export ANDROID_HOME="$ANDROID_SDK"
export PATH="$CMDLINE_TOOLS/bin:$ANDROID_SDK/platform-tools:$PATH"

# Persist to .bashrc so future sessions work
grep -q "ANDROID_HOME" "$HOME/.bashrc" 2>/dev/null || cat >> "$HOME/.bashrc" <<EOF

# Android SDK
export JAVA_HOME="$JAVA_HOME"
export ANDROID_HOME="$ANDROID_HOME"
export PATH="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$JAVA_HOME/bin:\$PATH"
EOF

echo ""
echo "===== Step 6: Accept licenses ====="
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo ""
echo "===== Step 7: Install SDK components ====="
sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;33.0.1"

echo ""
echo "===== Step 8: Clone / update repo ====="
if [ -d "$REPO_DIR/.git" ]; then
  echo "Repo exists — pulling latest..."
  cd "$REPO_DIR"
  git fetch origin
  git checkout claude/twinmind-local-android-6JiIn
  git pull origin claude/twinmind-local-android-6JiIn
else
  git clone \
    --branch claude/twinmind-local-android-6JiIn \
    https://github.com/tarekujjaman/twinmind-local.git \
    "$REPO_DIR"
  cd "$REPO_DIR"
fi

echo ""
echo "===== Step 9: Configure Gradle memory (low-RAM fix) ====="
mkdir -p "$REPO_DIR/.gradle"
cat > "$REPO_DIR/gradle.properties" <<EOF
org.gradle.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=256m
org.gradle.daemon=false
org.gradle.parallel=false
android.useAndroidX=true
EOF

echo ""
echo "===== Step 10: Build debug APK ====="
cd "$REPO_DIR"
chmod +x gradlew
./gradlew assembleDebug --no-daemon --stacktrace

echo ""
echo "============================================"
echo " BUILD SUCCESSFUL"
APK=$(find . -name "*.apk" | head -1)
echo " APK: $APK"
echo " Copy to Downloads:"
echo "   cp $APK /sdcard/Download/twinmind-debug.apk"
echo "============================================"
