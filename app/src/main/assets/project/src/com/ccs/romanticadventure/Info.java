package com.ccs.romanticadventure;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Info extends AppCompatActivity {
    private ScrollView scrollView;
    private TextView textView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        scrollView = findViewById(R.id.scrollView);
        textView = findViewById(R.id.textView);
        handler = new Handler();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Пример текста для отображения
        String longText = "Mr_maderator_YT - головний кодер, редактор фотографій, сюжет\n" +
                "Costic Antonii a.k.a ssokeer - QA, сюжет\n" +
                "amygoxy - робота з фотографіями, тестувальник \n" +
                "man_humor - тестувальник \n" +
                "\n" +
                "Dalee-3 - створювач картинок (yes)";

        // Устанавливаем текст
        textView.setText(longText);

        // Автоматическая прокрутка до конца текста
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);
    }
}
