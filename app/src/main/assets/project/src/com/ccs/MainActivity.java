package com.ccs;

import android.os.*;
import android.app.*;
import android.webkit.*;
import com.ccs.R;



public class MainActivity extends Activity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {       
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      WebView myWebView = (WebView) findViewById(R.id.webview);
       WebSettings webSettings = myWebView.getSettings();
      webSettings.setJavaScriptEnabled(true);
myWebView.loadUrl("https://github.com");
   }
       
} //close of MainActivity












