package com.ccs.romanticadventure.data;

import android.webkit.JavascriptInterface;

public class WebAppInterface {
    private boolean type;
    private int value;

    public WebAppInterface(boolean type, int value) {
        this.type = type;
        this.value = value;
    }

    @JavascriptInterface
    public boolean getValue() {
        return type;
    }

    @JavascriptInterface
    public int indexFromJS(int value) {
        this.value = value;
        return value;
    }

}

