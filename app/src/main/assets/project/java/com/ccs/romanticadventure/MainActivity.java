package com.ccs.romanticadventure;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.Activity;

import com.ccs.romanticadventure.data.PreferenceConfig;
import com.ccs.romanticadventure.system.ExitConfirmationDialog;

public class MainActivity extends Activity implements View.OnTouchListener {
    TextView startGame, settings, info, name;
    MediaPlayer mp;
    float volume;
    ImageView backgroundImage;

    //метод, який створює екран
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);



        //фіксуємо орієнтацію яка не зміниться (альбомна)
        info = findViewById(R.id.info);
        settings = findViewById(R.id.settings);
        startGame = findViewById(R.id.startBtn);
        backgroundImage = findViewById(R.id.background);
        name = findViewById(R.id.title);
        //завантажуємо дані, які нам потрібно
    }


    //Метод який завжди виконується при старті програми
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onStart() {
        super.onStart();
        volume = PreferenceConfig.getVolumeLevel(this);
        startGame.setOnTouchListener(MainActivity.this);
        settings.setOnTouchListener(this);
        info.setOnTouchListener(this);
        //створюємо пісню, яка буде нескінченною
        //і запускаємо
        mp = MediaPlayer.create(this, R.raw.intro);
        mp.setLooping(true);
        mp.setVolume(volume, volume);
        mp.start();


    }


    private void preferences() {
        volume = PreferenceConfig.getVolumeLevel(this);
    }

    @Override
    public void onBackPressed() {
        ExitConfirmationDialog.showExitConfirmationDialog(this);
    }

    public void exit() {
        finish(); // завершення актівіті
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.startBtn:
                startActivity(new Intent(MainActivity.this, Game_First_Activity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, Settings.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.info:
                startActivity(new Intent(MainActivity.this, Info.class));
                overridePendingTransition(0, 0);
                break;
        }
        return false;
    }
}