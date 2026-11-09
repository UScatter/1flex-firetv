# 1Flex TV - Local Build Script for Windows
$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  1Flex Fire TV - Local APK Builder      " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Check Node.js
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Error "Node.js is not found. Please install Node.js (v22+) to continue."
    exit 1
}

# 2. Check Java JDK
$hasJava = Get-Command java -ErrorAction SilentlyContinue
if (-not $hasJava) {
    Write-Host "[!] Java JDK (v21+) was not found in PATH." -ForegroundColor Yellow
    Write-Host "To install Java JDK 21 on Windows, run:" -ForegroundColor White
    Write-Host "  winget install Microsoft.OpenJDK.21" -ForegroundColor Green
    Write-Host ""
    Write-Host "Alternatively, push this repository to GitHub and GitHub Actions will automatically" -ForegroundColor White
    Write-Host "compile and output the APK for you in the 'Actions' tab!" -ForegroundColor Green
    exit 1
}

# 3. Check ANDROID_HOME / Android SDK
$androidHome = $env:ANDROID_HOME
if (-not $androidHome -and -not (Test-Path "$env:LOCALAPPDATA\Android\Sdk")) {
    Write-Host "[!] Android SDK was not found in ANDROID_HOME or AppData." -ForegroundColor Yellow
    Write-Host "To install Android Studio / Command-line tools, run:" -ForegroundColor White
    Write-Host "  winget install Google.AndroidStudio" -ForegroundColor Green
    exit 1
}

Write-Host "[*] Syncing Capacitor Web Assets to Android..." -ForegroundColor Cyan
npx cap sync android

Write-Host "[*] Compiling Fire TV APK via Gradle..." -ForegroundColor Cyan
Set-Location -Path "$PSScriptRoot\android"
.\gradlew.bat assembleDebug

$apkPath = "$PSScriptRoot\android\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host ""
    Write-Host "[OK] Fire TV APK successfully built!" -ForegroundColor Green
    Write-Host "Path: $apkPath" -ForegroundColor White
} else {
    Write-Error "Build finished but APK was not found at expected path."
}
