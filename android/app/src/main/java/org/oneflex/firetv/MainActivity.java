package org.oneflex.firetv;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.getcapacitor.BridgeActivity;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BridgeActivity {

    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View customView;

    // Native Virtual Cursor for Fire TV remote control
    private View cursorView;
    private float cursorX = 640f;
    private float cursorY = 360f;
    private float screenWidth = 1920f;
    private float screenHeight = 1080f;
    private final float cursorStep = 22f;
    private final Handler cursorHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideCursorRunnable = new Runnable() {
        @Override
        public void run() {
            if (cursorView != null) {
                cursorView.animate().alpha(0f).setDuration(300).start();
            }
        }
    };

    // Comprehensive list of ad, tracker, popunder, and redirect networks
    private static final Set<String> AD_DOMAINS = new HashSet<>(Arrays.asList(
            "layla.wtf",
            "adsterra",
            "popads",
            "propellerads",
            "exoclick",
            "doubleclick",
            "google-analytics",
            "googletagmanager",
            "histats",
            "creativecdn",
            "tsyndicate",
            "adtng",
            "trafficjunky",
            "juicyads",
            "onclickmega",
            "ad-delivery",
            "monetag",
            "yadro",
            "adnxs",
            "cpmrevenue",
            "betting",
            "casin",
            "stream-ads",
            "vidsrc-ads",
            "popcash",
            "adcash",
            "hilltopads",
            "richpush",
            "clickadu",
            "evadav",
            "trafficstars",
            "admaven",
            "rollerads",
            "galaksion",
            "adtrue",
            "bidgear",
            "mgid",
            "taboola",
            "outbrain",
            "infolinks",
            "cpmstar"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep Fire TV screen awake during video playback
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Measure display dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        cursorX = screenWidth / 2f;
        cursorY = screenHeight / 2f;

        // Create programmatic TV remote virtual cursor
        initNativeCursor();

        // Configure WebView once Capacitor bridge is ready
        setupFireTVWebView();
    }

    private void initNativeCursor() {
        cursorView = new View(this);
        int cursorSize = (int) (26 * getResources().getDisplayMetrics().density);

        // Circular ring with red glow border matching 1Flex theme
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor("#99e50914"));
        shape.setStroke((int) (2 * getResources().getDisplayMetrics().density), Color.WHITE);
        cursorView.setBackground(shape);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cursorSize, cursorSize);
        cursorView.setLayoutParams(params);
        cursorView.setX(cursorX - (cursorSize / 2f));
        cursorView.setY(cursorY - (cursorSize / 2f));
        cursorView.setAlpha(0f); // Hidden until remote is touched

        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        decor.addView(cursorView);
    }

    private void showCursor() {
        if (cursorView == null) return;
        cursorView.animate().alpha(1f).setDuration(150).start();
        cursorHandler.removeCallbacks(hideCursorRunnable);
        cursorHandler.postDelayed(hideCursorRunnable, 3500);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupFireTVWebView() {
        if (bridge == null || bridge.getWebView() == null) return;

        WebView webView = bridge.getWebView();
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false); // Enable auto video play on Fire TV

        // Anti-Popup Guards at WebSettings level
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // TV / Desktop User-Agent so sites render standard rich video players
        String defaultUA = settings.getUserAgentString();
        settings.setUserAgentString(defaultUA + " LargeScreen FireTV");

        // Custom ChromeClient for Full-Screen Video playback and popup rejection
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                // Reject all popup windows at WebKit engine level
                return false;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    onHideCustomView();
                    return;
                }
                customView = view;
                customViewCallback = callback;

                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                customViewContainer = new FrameLayout(MainActivity.this);
                customViewContainer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                customViewContainer.addView(customView);
                decor.addView(customViewContainer);
                webView.setVisibility(View.GONE);
                if (cursorView != null) cursorView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                if (customViewContainer != null) {
                    decor.removeView(customViewContainer);
                    customViewContainer = null;
                }
                customView = null;
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                }
                webView.setVisibility(View.VISIBLE);
                if (cursorView != null) cursorView.setVisibility(View.VISIBLE);
            }
        });

        // Intercept ad requests and redirect hijacking
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    String url = request.getUrl().toString().toLowerCase();
                    for (String adDomain : AD_DOMAINS) {
                        if (url.contains(adDomain)) {
                            // Drop request by returning an empty input stream
                            return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    String url = request.getUrl().toString().toLowerCase();

                    // Block external app intents and store schemes
                    if (url.startsWith("intent:") || url.startsWith("market:")) {
                        return true;
                    }

                    // Block known ad domains
                    for (String adDomain : AD_DOMAINS) {
                        if (url.contains(adDomain)) {
                            return true;
                        }
                    }

                    // Guard top-level redirects: protect user from being redirected away from 1flex.org
                    if (request.isForMainFrame()) {
                        String host = request.getUrl().getHost();
                        if (host != null && !host.contains("1flex.org") && !host.equals("localhost")) {
                            // Suppress redirect to external advertiser
                            return true;
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Inject client-side DOM ad-shield & overlay cleaner
                String adShieldScript =
                        "(function() {" +
                        "  window.open = function() { return null; };" +
                        "  window.alert = function() {};" +
                        "  const style = document.createElement('style');" +
                        "  style.textContent = 'iframe[src*=\"layla\"], iframe[src*=\"adsterra\"], iframe[src*=\"popads\"], .adsbygoogle { display: none !important; opacity: 0 !important; }';" +
                        "  (document.head || document.documentElement).appendChild(style);" +
                        "  document.addEventListener('click', function(e) {" +
                        "    const t = e.target;" +
                        "    if (!t) return;" +
                        "    const s = window.getComputedStyle(t);" +
                        "    if ((s.position === 'fixed' || s.position === 'absolute') && parseInt(s.zIndex) > 1000 && parseFloat(s.opacity) < 0.2) {" +
                        "      e.preventDefault(); e.stopPropagation(); t.remove();" +
                        "    }" +
                        "  }, true);" +
                        "})();";
                view.evaluateJavascript(adShieldScript, null);
            }
        });
    }

    // Fire TV Remote Control D-Pad & Key Handling
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView webView = bridge != null ? bridge.getWebView() : null;

        // D-Pad Movement Navigation
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                showCursor();
                cursorY = Math.max(16f, cursorY - cursorStep);
                updateCursorPosition();
                if (cursorY < 60f && webView != null) webView.scrollBy(0, -50);
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                showCursor();
                cursorY = Math.min(screenHeight - 16f, cursorY + cursorStep);
                updateCursorPosition();
                if (cursorY > screenHeight - 60f && webView != null) webView.scrollBy(0, 50);
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                showCursor();
                cursorX = Math.max(16f, cursorX - cursorStep);
                updateCursorPosition();
                if (cursorX < 60f && webView != null) webView.scrollBy(-50, 0);
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                showCursor();
                cursorX = Math.min(screenWidth - 16f, cursorX + cursorStep);
                updateCursorPosition();
                if (cursorX > screenWidth - 60f && webView != null) webView.scrollBy(50, 0);
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                showCursor();
                dispatchNativeClick(webView, cursorX, cursorY);
                return true;
        }

        // Back key behavior
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                // Exit full screen video on back press
                if (webView != null && webView.getWebChromeClient() != null) {
                    webView.getWebChromeClient().onHideCustomView();
                    return true;
                }
            }
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }

        // Fire TV Media Play/Pause Remote Key
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (webView != null) {
                webView.evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.paused ? v.play() : v.pause(); } })()",
                        null
                );
                return true;
            }
        }

        // Fire TV Media Fast Forward / Rewind
        if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            if (webView != null) {
                webView.evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.currentTime += 10; } })()",
                        null
                );
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
            if (webView != null) {
                webView.evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.currentTime = Math.max(0, v.currentTime - 10); } })()",
                        null
                );
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void updateCursorPosition() {
        if (cursorView == null) return;
        int halfSize = cursorView.getWidth() / 2;
        if (halfSize <= 0) halfSize = (int) (13 * getResources().getDisplayMetrics().density);
        cursorView.setX(cursorX - halfSize);
        cursorView.setY(cursorY - halfSize);
    }

    private void dispatchNativeClick(WebView webView, float x, float y) {
        if (webView == null) return;

        // Subtle click scale animation on cursor
        if (cursorView != null) {
            cursorView.animate().scaleX(0.7f).scaleY(0.7f).setDuration(80).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (cursorView != null) {
                        cursorView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
                    }
                }
            }).start();
        }

        // Dispatch hardware-level touch events through the WebView
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent downEvent = MotionEvent.obtain(
                downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0
        );
        MotionEvent upEvent = MotionEvent.obtain(
                downTime, eventTime + 50, MotionEvent.ACTION_UP, x, y, 0
        );

        webView.dispatchTouchEvent(downEvent);
        webView.dispatchTouchEvent(upEvent);

        downEvent.recycle();
        upEvent.recycle();
    }
}
