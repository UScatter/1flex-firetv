# 1Flex Fire TV Application

A dedicated Amazon Fire TV / Android TV hybrid application for [1flex.org](https://www.1flex.org/) with native ad-blocking, popup neutralization, screen wake lock, and a D-pad remote virtual mouse cursor engine.

---

## 🌟 Key Features

- **Ad-Shield Engine**: Blocks popups, `window.open` traps, and drops requests to ad networks (`layla.wtf`, `adsterra`, `popads`, etc.) at both the native socket layer and DOM layer.
- **Click-Jack Disarmer**: Automatically purges transparent click-hijacking overlays placed over third-party video embeds.
- **Fire TV Remote Virtual Cursor**: Smooth pointer physics, acceleration, edge scrolling, and hover feedback controlled with the standard Fire TV remote D-Pad.
- **TV Quick Bar HUD**: Press the **Menu** button (`☰`) on the remote to access navigation shortcuts (Home, Back, Reload, Fullscreen, Speed Adjuster).
- **Native Video Fullscreen**: Scales HTML5 embeds to 4K/1080p full screen with remote play/pause and fast-forward controls.
- **Stay-Awake Lock**: Prevents Fire OS from dimming or activating the screensaver during playback.
- **Leanback Launcher Ready**: Includes native 320x180 and 640x360 Fire TV banner assets and manifest declarations.

---

## 📂 Project Structure

```
1flex-firetv/
├── android/                   # Native Fire OS / Android TV project (Capacitor + Gradle)
│   ├── app/src/main/
│   │   ├── AndroidManifest.xml # Fire TV Leanback & permission configurations
│   │   ├── java/org/oneflex/firetv/MainActivity.java # Native Ad-filtering, Fullscreen & Remote bridge
│   │   └── res/drawable/banner.png # Fire TV Home Screen banner
├── www/                       # PWA Web Shell
│   ├── css/tv-styles.css      # Dark TV Leanback design & cursor styling
│   ├── js/ad-shield.js        # DOM Ad & overlay neutralizer
│   ├── js/virtual-cursor.js   # Fire TV remote cursor & scroll physics engine
│   ├── js/tv-app.js           # TV HUD, navigation, and wake lock controller
│   ├── manifest.webmanifest   # PWA manifest
│   └── index.html             # Shell container
├── .github/workflows/         # Cloud CI/CD workflow to compile APK on GitHub
├── build-local-apk.ps1        # Local Windows build script
└── INSTALL_FIRE_STICK.md      # Step-by-step sideloading instructions
```

---

## 🚀 How to Build the APK

### Option 1: GitHub Actions (No local Android SDK required)
1. Push this folder to a GitHub repository.
2. Go to the **Actions** tab in your GitHub repository.
3. Select **Build Fire TV APK** and click **Run workflow**.
4. Download the generated `1Flex-TV-FireStick-Debug-APK` artifact.

### Option 2: Local Windows Build
1. Install OpenJDK 17:
   ```powershell
   winget install Microsoft.OpenJDK.17
   ```
2. Install Android Studio (or Android SDK command-line tools):
   ```powershell
   winget install Google.AndroidStudio
   ```
3. Run the build script:
   ```powershell
   .\build-local-apk.ps1
   ```

For installation on your Fire Stick, refer to [`INSTALL_FIRE_STICK.md`](INSTALL_FIRE_STICK.md).
