/**
 * 1Flex TV - Ad-Shield Engine
 * Intercepts intrusive popups, clickjack overlays, malicious redirects, and ad trackers.
 */
(function (global) {
  'use strict';

  const AdShield = {
    stats: {
      popupsBlocked: 0,
      overlaysRemoved: 0,
      scriptsBlocked: 0,
      total: 0
    },

    // Known ad, popunder, and tracking domains
    adDomains: [
      'layla.wtf',
      'adsterra',
      'popads',
      'propellerads',
      'exoclick',
      'doubleclick',
      'google-analytics',
      'googletagmanager',
      'histats',
      'creativecdn',
      'tsyndicate',
      'adtng',
      'trafficjunky',
      'juicyads',
      'onclickmega',
      'ad-delivery',
      'monetag',
      'yadro',
      'adnxs',
      'cpmrevenue',
      'betting',
      'casin',
      'stream-ads',
      'vidsrc-ads'
    ],

    init() {
      console.log('[AdShield] Initializing Ad-Shield Engine for Fire TV...');
      this.defusePopups();
      this.defuseOverlays();
      this.injectCosmeticFilters();
      this.observeDOM();
    },

    isAdUrl(url) {
      if (!url || typeof url !== 'string') return false;
      const lower = url.toLowerCase();
      return this.adDomains.some(domain => lower.includes(domain));
    },

    recordBlock(type, detail) {
      this.stats[type] = (this.stats[type] || 0) + 1;
      this.stats.total++;
      console.warn(`[AdShield] Blocked ${type}:`, detail);

      // Dispatch event for TV HUD
      const event = new CustomEvent('adshield-blocked', {
        detail: { type, detail, stats: { ...this.stats } }
      });
      global.dispatchEvent(event);
    },

    // 1. Defuse window.open popups & popunders
    defusePopups() {
      const originalOpen = global.open;
      global.open = (url, target, features) => {
        console.log('[AdShield] Intercepted window.open attempt:', url);
        this.recordBlock('popupsBlocked', url || 'popup_window');
        // Return null/dummy to prevent opening browser/tab
        return null;
      };

      // Also neutralize alert/confirm loops commonly used by sketchy stream embeds
      const originalAlert = global.alert;
      global.alert = function (msg) {
        console.log('[AdShield] Suppressed modal alert:', msg);
      };
    },

    // 2. Disarm transparent click-hijack overlays
    defuseOverlays() {
      // Capture phase click interceptor
      document.addEventListener(
        'click',
        (e) => {
          const target = e.target;
          if (!target || !(target instanceof HTMLElement)) return;

          // Check if element is an invisible/transparent overlay capturing clicks
          const style = global.getComputedStyle(target);
          const isFullSpan =
            target.offsetWidth >= global.innerWidth * 0.7 &&
            target.offsetHeight >= global.innerHeight * 0.7;

          const isFixedOrAbsolute =
            style.position === 'fixed' || style.position === 'absolute';
          const isHighZ = parseInt(style.zIndex, 10) > 1000;
          const isTransparent =
            parseFloat(style.opacity) < 0.15 ||
            style.backgroundColor.includes('rgba(0, 0, 0, 0)') ||
            style.backgroundColor === 'transparent';

          // Check if target is an <a> tag linking to external ad domain
          let href = '';
          if (target.tagName === 'A') {
            href = target.getAttribute('href') || '';
          } else if (target.closest('a')) {
            href = target.closest('a').getAttribute('href') || '';
          }

          if (this.isAdUrl(href) || (isFullSpan && isFixedOrAbsolute && isHighZ && isTransparent)) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            console.log('[AdShield] Neutralized click-jack overlay element:', target, href);
            target.remove();
            this.recordBlock('overlaysRemoved', href || 'invisible_div_overlay');
            return false;
          }
        },
        true // Crucial: capture phase
      );
    },

    // 3. Inject cosmetic CSS hiding rules
    injectCosmeticFilters() {
      const style = document.createElement('style');
      style.id = 'adshield-cosmetic-filters';
      style.textContent = `
        /* Hide known ad slots and banner containers */
        iframe[src*="layla.wtf"],
        iframe[src*="adsterra"],
        iframe[src*="popads"],
        iframe[src*="exoclick"],
        iframe[src*="propeller"],
        iframe[src*="monetag"],
        div[class*="ad-banner"],
        div[class*="banner-ads"],
        div[id*="ad_wrapper"],
        div[id*="advertisement"],
        div[class*="native-ad"],
        div[id*="google_ads"],
        .adsbygoogle,
        a[href*="layla.wtf"],
        a[href*="onclickmega"],
        a[href*="monetag"] {
          display: none !important;
          visibility: hidden !important;
          height: 0 !important;
          width: 0 !important;
          pointer-events: none !important;
          opacity: 0 !important;
        }
      `;
      (document.head || document.documentElement).appendChild(style);
    },

    // 4. MutationObserver to continuously strip suspicious overlay injection
    observeDOM() {
      const observer = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
          for (const node of mutation.addedNodes) {
            if (node.nodeType === Node.ELEMENT_NODE) {
              const el = node;

              // Check if script node contains ad domains
              if (el.tagName === 'SCRIPT') {
                const src = el.getAttribute('src') || '';
                if (this.isAdUrl(src)) {
                  el.remove();
                  this.recordBlock('scriptsBlocked', src);
                }
              }

              // Check if iframe node points to ad domains
              if (el.tagName === 'IFRAME') {
                const src = el.getAttribute('src') || '';
                if (this.isAdUrl(src)) {
                  el.remove();
                  this.recordBlock('scriptsBlocked', src);
                }
              }
            }
          }
        }
      });

      observer.observe(document.documentElement, {
        childList: true,
        subtree: true
      });
    }
  };

  // Expose globally
  global.AdShield = AdShield;

  // Auto-init on script load
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => AdShield.init());
  } else {
    AdShield.init();
  }
})(typeof window !== 'undefined' ? window : this);
