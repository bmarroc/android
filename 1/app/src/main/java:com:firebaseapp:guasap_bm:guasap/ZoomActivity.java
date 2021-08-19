package com.firebaseapp.guasap_bm.guasap;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.net.URLEncoder;
import java.util.HashMap;

public class ZoomActivity extends Activity {
    public String data;
    public  ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom_layout);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomActivity.this.finish();
            }
        });

        try {
            data = URLEncoder.encode(getIntent().getStringExtra("data"), "UTF-8");
        } catch (Exception e) {
            data = "";
        }



        WebView webView = (WebView) findViewById(R.id.webView);

        webView.setWebChromeClient(new WebChromeClient());
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String script = "updateFromAndroid(\""+data+"\")";

                view.evaluateJavascript(script, null);
            }
        });

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void onLoad() {
                Log.d("[ZoomActivity]", "window.onload");
                ZoomActivity.this.progressBar.setVisibility(View.GONE);
            }
        }, "ZoomActivity");

        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setInitialScale(1);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.loadUrl("file:///android_asset/zoom.html");

        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public String encodeText(String text) {
        String t = "";
        for (int i=0; i < text.length(); i++) {
            if (text.charAt(i) == '\'') {
                t += '\\';
            }
            if (text.charAt(i) != '\n') {
                t += text.charAt(i);
            }
            if (text.charAt(i) == '\\') {
                t += "\\";
            }
        }
        return t;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
