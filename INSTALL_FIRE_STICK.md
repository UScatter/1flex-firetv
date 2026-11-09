# 1Flex TV - Amazon Fire Stick Installation Guide

This guide explains how to install and run **1Flex TV** on your Amazon Fire TV Stick.

---

## 📺 Prerequisites on Fire TV Stick

### Step 1: Enable Developer Options & Unknown Sources
1. Turn on your Fire Stick and go to **Settings** (gear icon on the right).
2. Select **My Fire TV** (or **Device & Software**).
3. If you see **Developer Options**, skip to step 4.  
   *If you do not see Developer Options*:
   - Click **About**.
   - Highlight **Fire TV Stick** (the first item) and press the **Center / Select** button on your remote **7 times rapidly**.
   - A prompt at the bottom will say *"No need, you are already a developer."*
   - Press **Back** once, and **Developer Options** will now appear.
4. Open **Developer Options**:
   - Turn **ADB Debugging** -> **ON**.
   - Click **Install unknown apps** (or **Apps from Unknown Sources**) -> Turn **ON** for the Downloader app (or turn ON globally).

---

## 🚀 Installation Methods

### Method A: Direct Install via the "Downloader" App (Recommended)
1. On your Fire TV, search for and install the free **Downloader** app from the Amazon Appstore.
2. Launch **Downloader**.
3. In the URL / Search box, enter either:
   - The direct download link to your compiled `app-debug.apk` (e.g. from GitHub Actions releases, Dropbox, Google Drive direct link, or a file host).
4. Click **Go**. Downloader will fetch the APK and prompt: **"Do you want to install this application?"**
5. Click **Install**, then click **Open**.

---

### Method B: Install via ADB over Wi-Fi (From PC)
If your PC is on the same Wi-Fi network as your Fire Stick:
1. Find your Fire Stick's IP address:
   - On Fire TV: **Settings** -> **My Fire TV** -> **About** -> **Network** (note the IP address, e.g. `192.168.1.45`).
2. On your PC terminal, run:
   ```powershell
   adb connect 192.168.1.45:5555
   ```
3. A prompt will appear on your TV: *"Allow USB debugging?"* -> Check *"Always allow from this computer"* and click **OK**.
4. Install the APK directly:
   ```powershell
   adb install "C:\Users\e\.gemini\antigravity\scratch\1flex-firetv\android\app\build\outputs\apk\debug\app-debug.apk"
   ```
5. The app will immediately appear under **Your Apps & Channels** on your Fire TV!

---

## 🎮 Fire TV Remote Control Cheat Sheet

| Button | Action |
|---|---|
| **D-Pad Arrows (Up/Down/Left/Right)** | Moves the glowing on-screen mouse pointer across the screen with acceleration physics. |
| **Edge Navigation** | Moving the pointer to the top or bottom 5% of the screen automatically scrolls the page. |
| **Center / Select Button** | Clicks the item underneath the pointer (dispatches native pointer events, activates player controls, selects stream servers). |
| **Menu Button (☰)** | Opens / closes the **TV Quick Bar HUD** (Home, Back, Reload, Cursor Speed, Ad-Shield stats). |
| **Back Button (◀)** | Navigates back in browser history, exits full screen, or closes the HUD. |
| **Play / Pause Button (⏯)** | Toggles video playback directly without needing to aim the cursor. |
| **Fast-Forward (⏩) / Rewind (⏪)** | Skips video +/- 10 seconds. |

---

## 🛡️ Built-in Ad-Shield Capabilities

- **Popup Neutralizer**: Intercepts and blocks `window.open` calls from third-party video embeds before they can spawn browser tabs.
- **Click-Jack Disarmer**: Detects invisible full-screen transparent `<div>` overlays placed over the video player and removes them on contact.
- **Socket-Level Ad Filtering**: Drops requests to known advertising networks (`layla.wtf`, `adsterra`, `popads`, etc.) before bytes download.
- **Stay-Awake Guard**: Disables the Fire TV sleep timer and screensaver during streaming so your TV never goes black mid-movie.
