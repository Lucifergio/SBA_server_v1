package com.template;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    SharedPreferences preferences;
    Intent main;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideStatusAndToolBar();
        setContentView(R.layout.activity_web);

        main = new Intent(this, MainActivity.class);
        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

        CookieManager cookieManager = CookieManager.getInstance();
        CookieManager.getInstance().flush();
        cookieManager.setAcceptCookie(true);

        cookieManager.flush();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            if (preferences.contains("RESPONSE")) {
                if (preferences.getString("RESPONSE", "").startsWith("http")) {
                    webView.loadUrl(preferences.getString("RESPONSE", ""));
                } else {
                    startActivity(main);
                }
            } else {
                if (LoadingActivity.getFinalLink() != null) {
                    sendHttpRequest(webSettings.getUserAgentString());

                    int counter = 0;

                    while (!preferences.contains("RESPONSE") && counter <= 10) {
                        counter++;

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (preferences.getString("RESPONSE", "").startsWith("http")) {
                            webView.loadUrl(preferences.getString("RESPONSE", ""));
                        }
                    }
                    if (!preferences.contains("RESPONSE") && counter <= 10 || preferences.getString("RESPONSE", "").equals("error")) {
                        startActivity(main);
                    } else if (preferences.getString("RESPONSE", "").startsWith("http")) {
                        webView.loadUrl(preferences.getString("RESPONSE", ""));
                    } else {
                        startActivity(main);
                    }
                }
            }
        }
    }

    private void hideStatusAndToolBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getSupportActionBar().hide();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void sendHttpRequest(String userAgent) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(LoadingActivity.getFinalLink())
                .header("User-Agent", userAgent)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        assert responseBody != null;
                        preferences.edit().putString("RESPONSE", responseBody.string()).apply();
                        throw new IOException("Ошибка: " +
                                response.code() + " " + response.message());
                    } else {
                        assert responseBody != null;
                        preferences.edit().putString("RESPONSE", responseBody.string()).apply();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}