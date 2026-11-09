package org.oneflex.firetv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
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

    // Fast native hash set of ad & tracking network domains
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
            "vidsrc-ads"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep Fire TV screen awake during video playback
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Configure WebView once Capacitor bridge is ready
        setupFireTVWebView();
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

        // TV / Desktop User-Agent so sites render standard rich video players
        String defaultUA = settings.getUserAgentString();
        settings.setUserAgentString(defaultUA + " LargeScreen FireTV");

        // Custom ChromeClient for Full-Screen Video playback on Fire TV
        webView.setWebChromeClient(new WebChromeClient() {
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
            }
        });

        // Intercept ad requests at the native network level
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
                    // Block malicious external app intents / ad popups
                    if (url.startsWith("intent:") || url.startsWith("market:")) {
                        return true;
                    }
                    for (String adDomain : AD_DOMAINS) {
                        if (url.contains(adDomain)) {
                            return true; // Suppress redirect
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
    }

    // Fire TV Remote Control Key Handling
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back key behavior
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                // Exit full screen video on back press
                if (bridge != null && bridge.getWebView() != null && bridge.getWebView().getWebChromeClient() != null) {
                    bridge.getWebView().getWebChromeClient().onHideCustomView();
                    return true;
                }
            }
            if (bridge != null && bridge.getWebView() != null && bridge.getWebView().canGoBack()) {
                bridge.getWebView().goBack();
                return true;
            }
        }

        // Fire TV Media Play/Pause Remote Key
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (bridge != null && bridge.getWebView() != null) {
                bridge.getWebView().evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.paused ? v.play() : v.pause(); } })()",
                        null
                );
                return true;
            }
        }

        // Fire TV Media Fast Forward / Rewind
        if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            if (bridge != null && bridge.getWebView() != null) {
                bridge.getWebView().evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.currentTime += 10; } })()",
                        null
                );
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
            if (bridge != null && bridge.getWebView() != null) {
                bridge.getWebView().evaluateJavascript(
                        "(function() { const v = document.querySelector('video'); if (v) { v.currentTime = Math.max(0, v.currentTime - 10); } })()",
                        null
                );
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
