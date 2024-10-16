package com.ccs.romanticadventure;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ccs.romanticadventure.data.PreferenceConfig;

public class Settings extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView volumeTextView, typeAnim;
    float volumeLvl;
    Switch switch_anim_value;
    boolean type;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        volumeLvl = PreferenceConfig.getVolumeLevel(this);
        type = PreferenceConfig.getAnimSwitchValue(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.intro);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        mediaPlayer.setVolume(volumeLvl, volumeLvl);
        switch_anim_value = findViewById(R.id.switch_anim);
        typeAnim = findViewById(R.id.type_anim_value_text);
        seekBar = findViewById(R.id.volume_set);
        volumeTextView = findViewById(R.id.volume_text);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (type) {
            switch_anim_value.setChecked(true);
            volumeTextView.setText(R.string.auto);
        } else {
            switch_anim_value.setChecked(false);
            volumeTextView.setText(R.string.touching);
        }

    }


    private void initializeSeekBar() {

        seekBar.setMax(100); // Встановіть максимальне значення, ви можете взяти те, яке вам потрібно
        seekBar.setProgress((int) (volumeLvl * 100));
        System.out.println("ініціалізація" + (int) volumeLvl);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeLvl = (float) progress / 100; // Оновлюємо значення volumeLvl
                System.out.println("aaaaaaaaaaaaaaaaaaaaa" + volumeLvl);
                updateVolume(); // Передаємо нове значення у метод updateVolume
                PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Необов'язково реагувати на початок взаємодії з ползунком
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Необов'язково реагувати на завершення взаємодії з ползунком
                PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);

            }
        });
    }


    private void updateVolume() {
        mediaPlayer.setVolume(volumeLvl, volumeLvl);
        volumeTextView.setText("Гучність: " + (int) (volumeLvl * 100) + "%");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release(); // Важливо звільнити ресурси mediaPlayer

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release(); // Важливо звільнити ресурси mediaPlayer

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeSeekBar();
        updateVolume();
        if (switch_anim_value != null) {
            switch_anim_value.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        volumeLvl = PreferenceConfig.getVolumeLevel(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            type = true;
            PreferenceConfig.setAnimSwitchValue(getApplicationContext(), type);
        } else {
            type = false;
            PreferenceConfig.setAnimSwitchValue(getApplicationContext(), type);
        }
    }

}
