package com.ccs.gencorelite.editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ccs.gencorelite.R;

public class DocsActivity extends AppCompatActivity {

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docs);
        WebView webView = findViewById(R.id.webview);
        ImageView imageView = findViewById(R.id.back_btn);
        webView.loadUrl("file:///android_asset/docs/index.html");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DocsActivity.this, Editor.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}