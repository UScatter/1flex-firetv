# 1Flex Fire TV Application

[![Release](https://img.shields.io/github/v/release/UScatter/1flex-firetv?color=red&style=for-the-badge)](https://github.com/UScatter/1flex-firetv/releases/latest)
[![Download APK](https://img.shields.io/badge/Direct%20Download-Fire%20TV%20APK-e50914?style=for-the-badge&logo=android)](https://github.com/UScatter/1flex-firetv/releases/download/v1.0.0/1flex-tv-firestick.apk)

A dedicated Amazon Fire TV / Android TV hybrid application for [1flex.org](https://www.1flex.org/) with native ad-blocking, popup neutralization, screen stay-awake lock, and a D-pad remote virtual mouse cursor engine.

---

## 📥 Direct APK Downloads

You can download the compiled Fire Stick APK directly from the **[Releases](https://github.com/UScatter/1flex-firetv/releases/latest)** section:

| Asset | Link |
|---|---|
| **1Flex TV Fire Stick APK** | [**Download 1flex-tv-firestick.apk**](https://github.com/UScatter/1flex-firetv/releases/download/v1.0.0/1flex-tv-firestick.apk) |
| **Standard Name APK** | [**Download 1flex-tv.apk**](https://github.com/UScatter/1flex-firetv/releases/download/v1.0.0/1flex-tv.apk) |
| **Debug APK** | [**Download app-debug.apk**](https://github.com/UScatter/1flex-firetv/releases/download/v1.0.0/app-debug.apk) |
| **LimeWire Mirror** | [**Download via LimeWire**](https://limewire.com/d/FMvjf#Ypdy4tn5j2) |

---

## 📺 How to Install on Fire Stick (via Downloader App)

1. On your Fire TV, open the **Downloader** app.
2. In the URL bar, type:
   ```
   https://github.com/UScatter/1flex-firetv/releases/download/v1.0.0/1flex-tv-firestick.apk
   ```
3. Click **Go**, wait for the download to finish, and click **Install**.

---

## 🌟 Key Features

- **Ad-Shield Engine**: Blocks popups, `window.open` traps, and drops requests to ad networks (`layla.wtf`, `adsterra`, `popads`, etc.) at both the native socket layer and DOM layer.
- **Click-Jack Disarmer**: Automatically purges transparent click-hijacking overlays placed over third-party video embeds.
- **Fire TV Remote Virtual Cursor**: Smooth pointer physics, acceleration, edge scrolling, and hover feedback controlled with the standard Fire TV remote D-Pad.
- **Native Video Fullscreen**: Scales HTML5 embeds to 4K/1080p full screen with remote play/pause and fast-forward controls.
- **Stay-Awake Lock**: Prevents Fire OS from dimming or activating the screensaver during playback.
- **Leanback Launcher Ready**: Includes native 320x180 and 640x360 Fire TV banner assets and manifest declarations.

---

## 🎮 Fire TV Remote Controls

| Button | Action |
|---|---|
| **D-Pad Arrows (Up/Down/Left/Right)** | Move the on-screen mouse pointer with acceleration physics |
| **Edge Navigation** | Move pointer near screen top/bottom to auto-scroll |
| **Center / Select Button** | Clicks element under cursor (dispatches hardware touch events across frames) |
| **Back Button (◀)** | Go back in page history / exit fullscreen |
| **Play / Pause (⏯)** | Toggle stream play/pause directly |
| **Fast Forward (⏩) / Rewind (⏪)** | Seek +/- 10 seconds |
