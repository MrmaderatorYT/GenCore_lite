package com.ccs.romanticadventure;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.romanticadventure.data.PreferenceConfig;
import com.ccs.romanticadventure.system.ExitConfirmationDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Game_First_Activity extends MainActivity {

    private WebView webView;
    private int katya, choose, textIndex = 0, value, indexArray, delayBetweenCharacters = 40, //затримка між спавном символів
            delayBetweenTexts = 2000; // затримка між спавнінгом іншого тексту з масиву;

    private static final int dialogContainerId = View.generateViewId(); // Генерируем уникальный идентификатор для контейнера

    float volumeLvl;
    MediaPlayer mediaPlayer;
    boolean type, historyBlockIsVisible = false, animationInProgress;
    private Button history, save, load, buttonElement, buttonSecondElement;
    private RelativeLayout bg;
    private TextView textElement, nameElement;
    private ArrayList<Pair> textArray = new ArrayList<>();

    private static class Pair {
        String name;
        String text;
        int value;

        Pair(int value, String name, String text) {
            this.name = name;
            this.text = text;
            this.value = value;

        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_first);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        choose = PreferenceConfig.getChoose(this);
        history = findViewById(R.id.buttonHistory);
        save = findViewById(R.id.fastSave_btn);
        load = findViewById(R.id.fastLoad_btn);
        bg = findViewById(R.id.bg);
        textElement = findViewById(R.id.dialog);
        nameElement = findViewById(R.id.name);
        type = PreferenceConfig.getAnimSwitchValue(this);
        buttonElement = findViewById(R.id.first_btn);
        buttonSecondElement = findViewById(R.id.second_btn);
        value = PreferenceConfig.getValue(this);

        initializeTextArray();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        volumeLvl = PreferenceConfig.getVolumeLevel(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.school);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(volumeLvl, volumeLvl);
        buttonElement.setVisibility(View.INVISIBLE);
        buttonSecondElement.setVisibility(View.INVISIBLE);

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!historyBlockIsVisible) {
                    showHistoryDialog();
                } else {
                    hideHistoryDialog();
                }
            }
        });

        // Start text animation
        animateText();

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickLoad();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickSave();
            }
        });
        buttonElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstBtn();
            }
        });
        buttonSecondElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secondBtn();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ExitConfirmationDialog.showExitConfirmationDialog(this);
    }

    private void quickLoad() {
        textElement.setText("");
        textIndex = value;
    }

    private void quickSave() {
        // Реализация быстрого сохранения
        PreferenceConfig.setValue(getApplicationContext(), textIndex);
    }

    private void showHistoryDialog() {
        historyBlockIsVisible = true;
        // Створення діалогового контейнера
        LinearLayout dialogContainer = new LinearLayout(this);
        dialogContainer.setId(dialogContainerId);
        dialogContainer.setLayoutParams(new ViewGroup.LayoutParams(
                convertDpToPx(300), // Ширина контейнера
                convertDpToPx(200) // Висота контейнера
        ));
        dialogContainer.setOrientation(LinearLayout.VERTICAL);
        dialogContainer.setBackgroundColor(Color.WHITE); // Білий колір фону
        dialogContainer.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Відступи всередині контейнера
        dialogContainer.setBackgroundResource(R.drawable.pink_bg); // Границя контейнера
        dialogContainer.setX(getScreenWidth() / 2f - convertDpToPx(150)); // Положення по горизонталі
        dialogContainer.setY(getScreenHeight() / 2f - convertDpToPx(100)); // Положення по вертикалі

        // Створення елемента для відображення тексту
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setScrollbarFadingEnabled(false);
        textContainer.setVerticalScrollBarEnabled(true);
        textContainer.setHorizontalScrollBarEnabled(false);

        // Додавання кожного ключа та значення з HashMap до текстового елемента
        for (int i = 0; i < textIndex; i++) {
            Pair pair = textArray.get(i);

            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            textView.setText(pair.name + ": " + pair.text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setLineSpacing(0, 1.5f);
            textContainer.addView(textView);
            Log.d("TextIndex", String.valueOf(textIndex));
        }

        // Додавання текстового елемента до діалогового контейнера
        dialogContainer.addView(textContainer);

        // Додавання діалогового контейнера до кореневої розмітки активності
        ((ViewGroup) getWindow().getDecorView().getRootView()).addView(dialogContainer);
    }

    // Метод для преобразования dp в px
    private int convertDpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    // Метод для получения ширины экрана
    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    // Метод для получения высоты экрана
    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private void hideHistoryDialog() {
        historyBlockIsVisible = false;
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        View dialogContainer = rootView.findViewById(dialogContainerId);
        if (dialogContainer != null) {
            rootView.removeView(dialogContainer);
        }
    }

    private void animateText() {
        if (textIndex < textArray.size()) {
            Pair pair = textArray.get(textIndex);

            // Якщо ключ не містить спешл значення, показати ім'я
            if (specialIndexes.contains(textIndex)) {
                nameElement.setText(pair.name);
            } else {
                nameElement.setText("Кейт");
            }

            textElement.setText("");
            String textToAnimate = pair.text;
            animationInProgress = true;
            new Handler().postDelayed(new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    if (i < textToAnimate.length()) {
                        textElement.append(String.valueOf(textToAnimate.charAt(i)));
                        i++;
                        new Handler().postDelayed(this, delayBetweenCharacters);
                    } else {
                        textIndex++;
                        animationInProgress = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animateText(); // Call the method recursively to show the next text
                            }
                        }, delayBetweenTexts);
                    }
                }
            }, delayBetweenCharacters);
        } else {
            textElement.setText("");
        }
    }

    private void initializeTextArray() {

        textArray.add(new Pair(646, ".", "*И от того Вани, который был раньше - уже ничего не осталось*"));



        // Додати інші пари за необхідності
    }

}
