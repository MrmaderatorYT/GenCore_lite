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
//супер клас головного вікна, бо тільки так буде працювати код підтвердження виходу з програми

public class Game_First_Activity extends MainActivity {

    private WebView webView;
    private int katya, choose, textIndex = 0,value, indexArray, delayBetweenCharacters = 40, //затримка між спавном символів
            delayBetweenTexts = 2000; // затримка між спавнінгом іншого тексту з масиву;

    private static final int dialogContainerId = View.generateViewId(); // Генерируем уникальный идентификатор для контейнера

    float volumeLvl;
    MediaPlayer mediaPlayer;
    boolean type, historyBlockIsVisible = false, animationInProgress;
    private Button history, save, load,buttonElement, buttonSecondElement;
    private RelativeLayout bg;
    private TextView textElement, nameElement;
    private String[] textArray;

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
                if(!historyBlockIsVisible) {
                    showHistoryDialog();
                }else{
                    hideHistoryDialog();
                }
            }
        });

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
        // Создание диалогового контейнера
        LinearLayout dialogContainer = new LinearLayout(this);
        dialogContainer.setId(dialogContainerId);
        dialogContainer.setLayoutParams(new ViewGroup.LayoutParams(
                convertDpToPx(300), // Ширина контейнера
                convertDpToPx(200) // Высота контейнера
        ));
        dialogContainer.setOrientation(LinearLayout.VERTICAL);
        dialogContainer.setBackgroundColor(Color.WHITE); // Белый цвет фона
        dialogContainer.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Отступы внутри контейнера
        dialogContainer.setBackgroundResource(R.drawable.pink_bg); // Граница контейнера
        dialogContainer.setX(getScreenWidth() / 2f - convertDpToPx(150)); // Положение по горизонтали
        dialogContainer.setY(getScreenHeight() / 2f - convertDpToPx(100)); // Положение по вертикали

        // Создание элемента для отображения текста
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setScrollbarFadingEnabled(false);
        textContainer.setVerticalScrollBarEnabled(true);
        textContainer.setHorizontalScrollBarEnabled(false);

        // Добавление каждой строки из массива в элемент текста
        for (int i = 0; i < textIndex; i++) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            textView.setText(textArray[i]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setLineSpacing(0, 1.5f);
            textContainer.addView(textView);
            Log.d("TextIndex", String.valueOf(textIndex));
        }

        // Добавление элемента текста в диалоговый контейнер
        dialogContainer.addView(textContainer);

        // Добавление диалогового контейнера в корневую разметку активности
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
        if (!type) {
            return;
        }
        textElement.setText(""); // Очищаем текстовый элемент

        String newText = textArray[textIndex];
        if (newText.equals("росії немає") || newText.equals("абра")) {
            nameElement.setText("Степан");
        } else {
            nameElement.setText("???"); // Очищаем текст, если условие не выполняется
        }

        animateFrame(0);
    }

    private void animateFrame(final int i) {
        String newText = textArray[textIndex];
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textElement.append(Character.toString(newText.charAt(i))); // Добавляем символ в текстовый элемент

                if (i < newText.length() - 1) {
                    animateFrame(i + 1);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!type) {
                                return;
                            }
                            textIndex++;

                            if (textIndex == 4 || textIndex == 17 || textIndex == 29) {
                                buttonElement.setVisibility(View.VISIBLE);
                                buttonSecondElement.setVisibility(View.VISIBLE);
                                animationInProgress = false;
                                return;
                            } else {
                                buttonElement.setVisibility(View.GONE);
                                buttonSecondElement.setVisibility(View.GONE);
                            }

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
                            animationInProgress = true;
                            animateText();
                            indexArray = i;
                        }
                    }, delayBetweenTexts);
                }
            }
        }, delayBetweenCharacters);
    }
    private void firstBtn(){

        switch (textIndex){
            case 3:
                textIndex = 4;
                break;
            case 17:
                nameElement.setText("???");
                textElement.setText("А що ж мені купити? Список дасиш, як минулого разу?");
                textIndex = 18;
                break;
        }

    }
    private void secondBtn(){
        switch (textIndex){
            case 3:
                nameElement.setText("???");
                textElement.setText("Ні, так не піде");
                textIndex = 3;
                break;
            case 17:
                nameElement.setText("???");
                textElement.setText("А що ж мені купити? Список дасиш, як минулого разу?");
                textIndex = 18;
                break;
        }
    }
    private void writeFile(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            // Отримуємо рядок з файлу
            String fileContent = stringBuilder.toString();

            // Розділяємо рядок на масив, використовуючи кому як роздільник
            String[] dataArray = fileContent.split(",");

            // Оновлюємо textArray з отриманим масивом даних
            textArray = dataArray;

            // Використання масиву у вашому коді
            // Наприклад, вивід першого елементу масиву:
            if (textArray.length > 0) {
                System.out.println("Перший елемент масиву: " + textArray[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
