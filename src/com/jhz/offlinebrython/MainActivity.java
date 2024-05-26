package com.jhz.offlinebrython;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity implements DialogInterface.OnClickListener {
    private WebView web;
    private static final String PREF_KEY_COUNT = "num_times_opened";
    private static int NUM_TIMES_OPENED;
    private ValueCallback<Uri[]> fileUploadCallback;
    private final int FILE_CHOOSER_REQUEST_CODE = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        updateAppNumOpened(this);
        showWelcomeMessage();
        web = findViewById(R.id.webPageView);
        getActionBar().hide();
        loadWebSettings();
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                    fileUploadCallback = filePathCallback;
                } catch (ActivityNotFoundException e) {
                    return false;
                }
                return true;
            }
        });
        web.loadUrl("file:///android_asset/brython/index.html");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileUploadCallback == null) {
                return;
            }
            
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }
                    
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            fileUploadCallback.onReceiveValue(results);
            fileUploadCallback = null;
        }
    }
    
    private void loadWebSettings() {
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
    }
    
    private void updateAppNumOpened(Context context) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        NUM_TIMES_OPENED = sharedPreferences.getInt(PREF_KEY_COUNT, 0) + 1;
        sharedPreferences.edit().putInt(PREF_KEY_COUNT, NUM_TIMES_OPENED).apply();
    }
    
    private void showWelcomeMessage() {
        if(NUM_TIMES_OPENED <= 2) {
            new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_launcher)
            .setTitle("Welcome to Brython")
            .setMessage("\nRun Python programs offline using Brython. Use this app for free.\n\nNote: Since brython uses JavaScript to run Python programs, that also means that it can't install some modules.\n\n\n- JHZ RMX")
            .setCancelable(false)
            .setPositiveButton("Sige", this).show();
        }
    }
    
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        
    }
    
    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        web.clearCache(true);
        super.onDestroy();
    }
}