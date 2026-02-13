@echo off
echo ========================================
echo  Voice Agent AI - APK Builder
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] Checking Gradle wrapper...
if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

echo [2/4] Cleaning previous builds...
call gradlew.bat clean
if errorlevel 1 (
    echo Error: Clean failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Building debug APK...
echo This may take a few minutes on first build...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo ========================================
    echo  Build Failed!
    echo ========================================
    echo Check the error messages above.
    echo.
    echo Common issues:
    echo - No internet connection (needs to download dependencies)
    echo - Android SDK not installed
    echo - Java JDK not installed
    echo.
    pause
    exit /b 1
)

echo.
echo [4/4] Locating APK...
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if exist "%APK_PATH%" (
    echo.
    echo ========================================
    echo  Build Successful! ✓
    echo ========================================
    echo.
    echo APK Location:
    echo %CD%\%APK_PATH%
    echo.
    echo APK Size:
    for %%A in ("%APK_PATH%") do echo %%~zA bytes
    echo.
    echo Opening folder...
    explorer /select,"%APK_PATH%"
    echo.
    echo ========================================
    echo  Installation Instructions:
    echo ========================================
    echo.
    echo Option 1: USB Install
    echo   1. Enable USB Debugging on your device
    echo   2. Connect device via USB
    echo   3. Run: adb install "%APK_PATH%"
    echo.
    echo Option 2: Direct Install
    echo   1. Copy APK to your device
    echo   2. Open APK file on device
    echo   3. Allow installation from unknown sources
    echo   4. Tap Install
    echo.
) else (
    echo.
    echo ========================================
    echo  Build Failed! ✗
    echo ========================================
    echo APK file not found at expected location.
    echo.
)

pause
