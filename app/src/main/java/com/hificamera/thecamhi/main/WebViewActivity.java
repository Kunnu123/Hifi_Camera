package com.hificamera.thecamhi.main;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.hificamera.R;
import com.hificamera.thecamhi.base.TitleView;

import static android.webkit.WebSettings.LOAD_NO_CACHE;

public class WebViewActivity extends HiActivity {
    String mtitle, webUrl;
    private WebView mwebView;
    LinearLayout llEmpty;
    RelativeLayout webParentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initView();

    }

    private void initView() {
        mtitle = getIntent().getStringExtra("title");
        webUrl = getIntent().getStringExtra("webUrl");
        TitleView title = (TitleView) findViewById(R.id.title_top);
        mwebView = findViewById(R.id.web_agreement);
        llEmpty = findViewById(R.id.ll_empty);
        mwebView.getSettings().setCacheMode(LOAD_NO_CACHE);
        mwebView.clearCache(true);
        title.setTitle(mtitle.substring(1, mtitle.length() - 1));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        WebViewActivity.this.finish();
                        break;
                }

            }
        });

        mwebView.getSettings().setJavaScriptEnabled(true);
        mwebView.getSettings().setSupportZoom(true);
        mwebView.getSettings().setBuiltInZoomControls(true);
        mwebView.getSettings().setUseWideViewPort(true);
        mwebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mwebView.getSettings().setLoadWithOverviewMode(true);
        mwebView.getSettings().setDefaultTextEncodingName("utf-8");

        mwebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                showErrorPage();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                showErrorPage();
            }
        });
        mwebView.loadUrl(webUrl);
        webParentView = (RelativeLayout) mwebView.getParent();
    }

    private void showErrorPage() {
        llEmpty.setVisibility(View.VISIBLE);
        mwebView.setVisibility(View.GONE);
    }


}
