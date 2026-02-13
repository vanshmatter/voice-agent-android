# Building APK for Voice Agent AI

## Prerequisites

- Android Studio installed
- Java Development Kit (JDK) 8 or higher
- Android SDK installed

## Method 1: Using Android Studio (Recommended)

### Step 1: Open Project
1. Open Android Studio
2. Click **File → Open**
3. Navigate to: `c:\Users\HP\.vscode\VoiceAgent`
4. Click **OK**

### Step 2: Wait for Gradle Sync
- Wait for Gradle to sync all dependencies
- This may take a few minutes on first build
- Check bottom status bar for progress

### Step 3: Build APK
1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete
3. Click **locate** in the notification that appears

**APK Location**: `VoiceAgent\app\build\outputs\apk\debug\app-debug.apk`

## Method 2: Using Gradle Command Line

### Step 1: Open Terminal
1. Open PowerShell or Command Prompt
2. Navigate to project directory:
   ```powershell
   cd c:\Users\HP\.vscode\VoiceAgent
   ```

### Step 2: Build APK
Run the Gradle build command:

```powershell
# Windows
.\gradlew assembleDebug

# If gradlew.bat exists
gradlew.bat assembleDebug
```

### Step 3: Locate APK
After successful build, find APK at:
```
c:\Users\HP\.vscode\VoiceAgent\app\build\outputs\apk\debug\app-debug.apk
```

## Method 3: Build Release APK (Signed)

For a production-ready APK:

### Step 1: Generate Keystore (First Time Only)
```powershell
keytool -genkey -v -keystore voice-agent-key.keystore -alias voice-agent -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Build Release APK
1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Choose your keystore file
4. Enter keystore password and key password
5. Select **release** build variant
6. Click **Finish**

**Release APK Location**: `VoiceAgent\app\build\outputs\apk\release\app-release.apk`

## Installing APK on Device

### Option 1: USB Connection
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect device via USB
4. Run in Android Studio or use:
   ```powershell
   adb install app-debug.apk
   ```

### Option 2: Direct Transfer
1. Copy APK to your device (USB, email, cloud storage)
2. On device, open **Files** app
3. Navigate to APK location
4. Tap APK file
5. Allow **Install from Unknown Sources** if prompted
6. Tap **Install**

## Troubleshooting

### Build Fails

**Issue**: Gradle sync failed
- Check internet connection (downloads dependencies)
- Update Android Studio
- Invalidate caches: **File → Invalidate Caches / Restart**

**Issue**: SDK not found
- Open **Tools → SDK Manager**
- Install Android SDK 24 (minimum) and 34 (target)

**Issue**: Build tools version error
- Update build tools in SDK Manager
- Or modify `build.gradle` to match installed version

### APK Won't Install

**Issue**: App not installed
- Enable **Unknown Sources** in device settings
- Check device has enough storage
- Uninstall any previous version first

**Issue**: Parse error
- APK may be corrupted during transfer
- Rebuild APK
- Use different transfer method

## Quick Build Script

Save this as `build-apk.bat` in the project root:

```batch
@echo off
echo Building Voice Agent AI APK...
echo.

cd /d "%~dp0"

echo Step 1: Cleaning previous builds...
call gradlew clean

echo.
echo Step 2: Building debug APK...
call gradlew assembleDebug

echo.
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✓ Build successful!
    echo.
    echo APK Location:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    explorer /select,"app\build\outputs\apk\debug\app-debug.apk"
) else (
    echo ✗ Build failed!
    echo Check the error messages above.
)

pause
```

## APK Size

Expected APK size:
- **Debug**: ~15-25 MB
- **Release**: ~10-15 MB (with ProGuard)

## Next Steps After Installation

1. **Grant Permissions**: Microphone, Phone, SMS, Contacts, Location
2. **Battery Optimization**: Disable for wake word to work
3. **Configure Claude**: Enter API key in Settings (optional)
4. **Test Wake Word**: Say "Nekro"
5. **Try Commands**: "What time is it?"

## Notes

- **Debug APK**: For testing, not optimized
- **Release APK**: Optimized, requires signing
- **First Build**: May take 5-10 minutes
- **Subsequent Builds**: 1-2 minutes

---

**Need Help?** Check the error messages in the build output for specific issues.
