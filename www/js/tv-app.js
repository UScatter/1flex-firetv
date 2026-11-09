/**
 * 1Flex TV - App Controller
 * Orchestrates HUD, navigation, frame loading, wake lock, and toast notifications.
 */
(function (global) {
  'use strict';

  const TARGET_URL = 'https://www.1flex.org/';

  const TVApp = {
    hudVisible: false,
    wakeLockInstance: null,
    currentSpeed: 'normal',

    init() {
      console.log('[TVApp] Booting 1Flex TV App Controller...');
      this.bindElements();
      this.bindEvents();
      this.requestWakeLock();
      this.loadTarget();
    },

    bindElements() {
      this.hudEl = document.getElementById('tv-hud');
      this.frameEl = document.getElementById('content-frame');
      this.splashEl = document.getElementById('boot-splash');
      this.shieldBadgeEl = document.getElementById('shield-count');
      this.speedBtnEl = document.getElementById('btn-speed');
      this.toastContainer = document.getElementById('toast-container');
    },

    bindEvents() {
      // Listen for AdShield block events
      window.addEventListener('adshield-blocked', (e) => {
        const stats = e.detail?.stats;
        if (stats && this.shieldBadgeEl) {
          this.shieldBadgeEl.textContent = `Shield: ${stats.total} Blocked`;
        }
        if (e.detail?.type === 'popupsBlocked') {
          this.showToast('Intrusive popup blocked', 'shield');
        } else if (e.detail?.type === 'overlaysRemoved') {
          this.showToast('Clickjack overlay removed', 'shield');
        }
      });

      // Frame load event
      if (this.frameEl) {
        this.frameEl.addEventListener('load', () => {
          setTimeout(() => {
            if (this.splashEl) this.splashEl.classList.add('hidden');
          }, 600);
        });
      }

      // Wake lock auto-renew on visibility change
      document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') {
          this.requestWakeLock();
        }
      });
    },

    loadTarget() {
      if (this.frameEl) {
        this.frameEl.src = TARGET_URL;
      }
    },

    goHome() {
      if (this.frameEl) {
        this.frameEl.src = TARGET_URL;
        this.showToast('Navigating to 1Flex Home');
      }
      this.hideHUD();
    },

    reload() {
      if (this.frameEl) {
        this.frameEl.src = this.frameEl.src || TARGET_URL;
        this.showToast('Reloading page...');
      }
      this.hideHUD();
    },

    goBack() {
      if (this.frameEl && this.frameEl.contentWindow) {
        try {
          this.frameEl.contentWindow.history.back();
        } catch {
          this.frameEl.src = TARGET_URL;
        }
      }
      this.hideHUD();
    },

    toggleHUD() {
      if (this.hudVisible) {
        this.hideHUD();
      } else {
        this.showHUD();
      }
    },

    showHUD() {
      this.hudVisible = true;
      if (this.hudEl) this.hudEl.classList.add('visible');
    },

    hideHUD() {
      this.hudVisible = false;
      if (this.hudEl) this.hudEl.classList.remove('visible');
    },

    isHUDVisible() {
      return this.hudVisible;
    },

    cycleSpeed() {
      if (this.currentSpeed === 'normal') {
        this.currentSpeed = 'fast';
      } else if (this.currentSpeed === 'fast') {
        this.currentSpeed = 'slow';
      } else {
        this.currentSpeed = 'normal';
      }

      global.VirtualCursor?.setSpeedPreset(this.currentSpeed);
      if (this.speedBtnEl) {
        this.speedBtnEl.textContent = `Speed: ${this.currentSpeed.toUpperCase()}`;
      }
      this.showToast(`Cursor Speed: ${this.currentSpeed.toUpperCase()}`);
    },

    toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch((err) => {
          console.warn('Fullscreen request failed:', err);
        });
      } else {
        document.exitFullscreen().catch((err) => {
          console.warn('Exit fullscreen failed:', err);
        });
      }
      this.hideHUD();
    },

    async requestWakeLock() {
      try {
        if ('wakeLock' in navigator) {
          this.wakeLockInstance = await navigator.wakeLock.request('screen');
          console.log('[TVApp] Screen Wake Lock acquired (Fire TV screen stay-awake active)');
        }
      } catch (err) {
        console.warn('[TVApp] Wake Lock error:', err);
      }
    },

    showToast(message, type = 'info') {
      if (!this.toastContainer) return;
      const toast = document.createElement('div');
      toast.className = `toast ${type}`;
      toast.textContent = message;
      this.toastContainer.appendChild(toast);

      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
      }, 2600);
    }
  };

  global.TVApp = TVApp;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => TVApp.init());
  } else {
    TVApp.init();
  }
})(typeof window !== 'undefined' ? window : this);
