/**
 * 1Flex TV - Remote Control Virtual Cursor Engine
 * Maps Fire TV remote D-Pad buttons to an on-screen mouse pointer with physics,
 * edge scrolling, auto-hide, and synthetic pointer events.
 */
(function (global) {
  'use strict';

  const VirtualCursor = {
    x: window.innerWidth / 2,
    y: window.innerHeight / 2,
    vx: 0,
    vy: 0,
    baseSpeed: 12,
    maxSpeed: 28,
    acceleration: 1.8,
    friction: 0.82,
    activeKeys: { up: false, down: false, left: false, right: false },
    isClicking: false,
    cursorEl: null,
    animFrameId: null,
    idleTimer: null,
    idleTimeoutMs: 3500,

    init() {
      console.log('[VirtualCursor] Initializing Fire TV Virtual Cursor...');
      this.createCursorElement();
      this.bindKeyEvents();
      this.startLoop();
      this.resetIdleTimer();
    },

    createCursorElement() {
      let el = document.getElementById('virtual-cursor');
      if (!el) {
        el = document.createElement('div');
        el.id = 'virtual-cursor';
        el.innerHTML = '<div class="cursor-ring"></div><div class="cursor-dot"></div>';
        document.body.appendChild(el);
      }
      this.cursorEl = el;
      this.updateCursorPosition();
    },

    setSpeedPreset(preset) {
      if (preset === 'slow') {
        this.baseSpeed = 8;
        this.maxSpeed = 16;
      } else if (preset === 'fast') {
        this.baseSpeed = 16;
        this.maxSpeed = 36;
      } else {
        // normal
        this.baseSpeed = 12;
        this.maxSpeed = 26;
      }
      console.log('[VirtualCursor] Speed preset set to:', preset);
    },

    bindKeyEvents() {
      window.addEventListener('keydown', (e) => this.handleKeyDown(e), true);
      window.addEventListener('keyup', (e) => this.handleKeyUp(e), true);
    },

    handleKeyDown(e) {
      this.resetIdleTimer();
      const code = e.keyCode || e.which;

      switch (code) {
        // D-Pad Up / Arrow Up
        case 19:
        case 38:
          this.activeKeys.up = true;
          e.preventDefault();
          break;

        // D-Pad Down / Arrow Down
        case 20:
        case 40:
          this.activeKeys.down = true;
          e.preventDefault();
          break;

        // D-Pad Left / Arrow Left
        case 21:
        case 37:
          this.activeKeys.left = true;
          e.preventDefault();
          break;

        // D-Pad Right / Arrow Right
        case 22:
        case 39:
          this.activeKeys.right = true;
          e.preventDefault();
          break;

        // D-Pad Center / Select / Enter
        case 23:
        case 13:
        case 66:
          if (!this.isClicking) {
            this.triggerClickDown();
          }
          e.preventDefault();
          break;

        // Fire TV Menu Button
        case 82:
          global.TVApp?.toggleHUD();
          e.preventDefault();
          break;

        // Fire TV Back Button
        case 4:
        case 27:
          if (global.TVApp?.isHUDVisible()) {
            global.TVApp.hideHUD();
            e.preventDefault();
          }
          break;
      }
    },

    handleKeyUp(e) {
      const code = e.keyCode || e.which;

      switch (code) {
        case 19:
        case 38:
          this.activeKeys.up = false;
          break;
        case 20:
        case 40:
          this.activeKeys.down = false;
          break;
        case 21:
        case 37:
          this.activeKeys.left = false;
          break;
        case 22:
        case 39:
          this.activeKeys.right = false;
          break;
        case 23:
        case 13:
        case 66:
          this.triggerClickUp();
          break;
      }
    },

    startLoop() {
      const update = () => {
        // Accelerate based on held keys
        if (this.activeKeys.left) this.vx = Math.max(this.vx - this.acceleration, -this.maxSpeed);
        if (this.activeKeys.right) this.vx = Math.min(this.vx + this.acceleration, this.maxSpeed);
        if (this.activeKeys.up) this.vy = Math.max(this.vy - this.acceleration, -this.maxSpeed);
        if (this.activeKeys.down) this.vy = Math.min(this.vy + this.acceleration, this.maxSpeed);

        // Apply friction
        if (!this.activeKeys.left && !this.activeKeys.right) this.vx *= this.friction;
        if (!this.activeKeys.up && !this.activeKeys.down) this.vy *= this.friction;

        if (Math.abs(this.vx) < 0.1) this.vx = 0;
        if (Math.abs(this.vy) < 0.1) this.vy = 0;

        if (this.vx !== 0 || this.vy !== 0) {
          this.x = Math.max(8, Math.min(window.innerWidth - 8, this.x + this.vx));
          this.y = Math.max(8, Math.min(window.innerHeight - 8, this.y + this.vy));
          this.updateCursorPosition();
          this.checkEdgeScroll();
          this.inspectElementUnderCursor();
        }

        this.animFrameId = requestAnimationFrame(update);
      };

      this.animFrameId = requestAnimationFrame(update);
    },

    updateCursorPosition() {
      if (!this.cursorEl) return;
      this.cursorEl.style.left = `${this.x}px`;
      this.cursorEl.style.top = `${this.y}px`;
    },

    checkEdgeScroll() {
      const scrollThreshold = 45;
      const scrollSpeed = 16;
      let dx = 0;
      let dy = 0;

      if (this.y < scrollThreshold && this.activeKeys.up) dy = -scrollSpeed;
      if (this.y > window.innerHeight - scrollThreshold && this.activeKeys.down) dy = scrollSpeed;
      if (this.x < scrollThreshold && this.activeKeys.left) dx = -scrollSpeed;
      if (this.x > window.innerWidth - scrollThreshold && this.activeKeys.right) dx = scrollSpeed;

      if (dx !== 0 || dy !== 0) {
        window.scrollBy({ left: dx, top: dy, behavior: 'auto' });
      }
    },

    inspectElementUnderCursor() {
      if (!this.cursorEl) return;
      // Temporarily hide cursor element to hit test what is underneath
      this.cursorEl.style.display = 'none';
      const el = document.elementFromPoint(this.x, this.y);
      this.cursorEl.style.display = 'block';

      if (!el) {
        this.cursorEl.classList.remove('hovering-clickable');
        return;
      }

      const isClickable =
        el.tagName === 'BUTTON' ||
        el.tagName === 'A' ||
        el.tagName === 'INPUT' ||
        el.tagName === 'VIDEO' ||
        el.getAttribute('role') === 'button' ||
        el.onclick !== null ||
        window.getComputedStyle(el).cursor === 'pointer' ||
        el.closest('button, a, [role="button"]');

      if (isClickable) {
        this.cursorEl.classList.add('hovering-clickable');
      } else {
        this.cursorEl.classList.remove('hovering-clickable');
      }
    },

    triggerClickDown() {
      this.isClicking = true;
      if (this.cursorEl) this.cursorEl.classList.add('clicking');

      this.cursorEl.style.display = 'none';
      const el = document.elementFromPoint(this.x, this.y);
      this.cursorEl.style.display = 'block';

      if (el) {
        const mouseEvt = new MouseEvent('mousedown', {
          bubbles: true,
          cancelable: true,
          view: window,
          clientX: this.x,
          clientY: this.y
        });
        el.dispatchEvent(mouseEvt);
      }
    },

    triggerClickUp() {
      this.isClicking = false;
      if (this.cursorEl) this.cursorEl.classList.remove('clicking');

      this.cursorEl.style.display = 'none';
      const el = document.elementFromPoint(this.x, this.y);
      this.cursorEl.style.display = 'block';

      if (el) {
        const mouseUpEvt = new MouseEvent('mouseup', {
          bubbles: true,
          cancelable: true,
          view: window,
          clientX: this.x,
          clientY: this.y
        });
        const clickEvt = new MouseEvent('click', {
          bubbles: true,
          cancelable: true,
          view: window,
          clientX: this.x,
          clientY: this.y
        });
        el.dispatchEvent(mouseUpEvt);
        el.dispatchEvent(clickEvt);

        // Auto-focus inputs for Fire TV keyboard
        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
          el.focus();
        }
      }
    },

    resetIdleTimer() {
      if (this.cursorEl) this.cursorEl.classList.remove('idle');
      clearTimeout(this.idleTimer);
      this.idleTimer = setTimeout(() => {
        if (!this.isClicking && !this.activeKeys.up && !this.activeKeys.down && !this.activeKeys.left && !this.activeKeys.right) {
          if (this.cursorEl) this.cursorEl.classList.add('idle');
        }
      }, this.idleTimeoutMs);
    }
  };

  global.VirtualCursor = VirtualCursor;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => VirtualCursor.init());
  } else {
    VirtualCursor.init();
  }
})(typeof window !== 'undefined' ? window : this);
