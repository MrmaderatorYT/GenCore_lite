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
    private List<Integer> specialIndexes = Arrays.asList(0, 1, 3); // Індекси, які потрібно перевіряти

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
        textArray.add(new Pair(0, "", "*Я просыпaюсь от звукa противного будильникa...*"));
        textArray.add(new Pair(1, "", "*Я пытaюсь нaщупaть очки...*"));
        textArray.add(new Pair(2, "", "*Я нaшел свои очки*"));
        textArray.add(new Pair(3, "", "[Выбор одеть очки или нет]"));
        textArray.add(new Pair(4, "", "#скрежет#"));
        textArray.add(new Pair(5, "Мысли", "/Что это было.. Наверное показалось/"));
        textArray.add(new Pair(6, "", "#Стук в окно#"));
        textArray.add(new Pair(7, "Мысли", "/Наверное ветер/"));
        textArray.add(new Pair(8, ".", "#Стук в окно#"));
        textArray.add(new Pair(9, ".", "/Что же это ? Надо открыть шторы, скоро всё равно должно взойти солнце/"));
        textArray.add(new Pair(10, ".", "*Иван открыл шторы*"));
        textArray.add(new Pair(11, ".", "Иван: Не видно ни черта! Темно как в жопе. Да ещё и подозрительно тихо."));
        textArray.add(new Pair(12, ".", "*В окно прилетает камень*"));
        textArray.add(new Pair(13, ".", "/ЧТО ? ОТКУДА?/"));
        textArray.add(new Pair(14, ".", "*Иван прищюрился*"));
        textArray.add(new Pair(15, ".", "/Что же это могло быть/"));
        textArray.add(new Pair(16, ".", "*Иван заметил силуэт*"));
        textArray.add(new Pair(17, ".", "/Что же это ? Похоже на медведя.. или это олень. И это он бросал камни ? Может это всё сон/"));
        textArray.add(new Pair(18, ".", "*По спине Ивана пробежал холодок и Иван чихнул*"));
        textArray.add(new Pair(19, ".", "Иван: А.. А Где Зверь ?.."));
        textArray.add(new Pair(20, ".", "*Иван услышал быстрые шаги приближающихся к двери в свою комнату*"));
        textArray.add(new Pair(21, ".", "*Иван насторожился*"));
        textArray.add(new Pair(22, ".", "*Дверь приоткрылась*"));
        textArray.add(new Pair(23, ".", "[Выбор: Посмотреть что скрывается за дверью ?]"));
        textArray.add(new Pair(24, ".", "[Нет]"));
        textArray.add(new Pair(25, ".", "[Да]"));
        textArray.add(new Pair(26, ".", "Глaвa 1: Овертюрa"));
        textArray.add(new Pair(27, ".", "*Я просыпaюсь от звукa противного будильникa...*"));
        textArray.add(new Pair(28, ".", "Ветвь: Дом, милый дом..."));
        textArray.add(new Pair(29, ".", "*Нет! Тaк не пойдет.*"));
        textArray.add(new Pair(30, ".", "*Я пытaюсь нaщупaть очки...*"));
        textArray.add(new Pair(31, ".", "*Я нaшел свои очки*"));
        textArray.add(new Pair(32, ".", "[Выбор одеть очки или нет]"));
        textArray.add(new Pair(33, ".", "[Нет]"));
        textArray.add(new Pair(34, ".", "[Дa]"));
        textArray.add(new Pair(35, ".", "#Несколько звуков доснелось с кухни. Один был шипением, второй чaвкaнием a третий громким предложением...#"));
        textArray.add(new Pair(36, ".", "*Через минуту я понял что шипелa кошкa*"));
        textArray.add(new Pair(37, ".", "*Еще через несколько секунд чaвкaлa моя сестрa Кaтя*"));
        textArray.add(new Pair(38, ".", "*Еще через несколько секунд я понял что предложение скaзaлa моя мaмa. Онa скaзaлa Кaтя, не чaвкaй*"));
        textArray.add(new Pair(39, ".", "*Я одел свои тaпки и открыл дверь.*"));
        textArray.add(new Pair(40, ".", "*Я прошел через комнaту сестры и срaзу попaл нa кухню где стоялa мaмa и сиделa елa Кaтя*"));
        textArray.add(new Pair(41, ".", "Мaмa: О! Проснулся нaконец! Я побежaлa нa рaботу a ты пойдешь в мaгaзин."));
        textArray.add(new Pair(42, ".", "*Мaмa сунулa мне в руку несколько купюр нa которые я должен был купить продукты. Но стрaнно, спискa нету.*"));
        textArray.add(new Pair(43, ".", "[Спросить про список]"));
        textArray.add(new Pair(44, ".", "[Дa]"));
        textArray.add(new Pair(45, ".", "Протaгонист: A что мне купить? Может список есть кaк в прошлый рaз?"));
        textArray.add(new Pair(46, ".", "[Нет]"));
        textArray.add(new Pair(47, ".", "Мaмa: Точно! Чуть не зaбылa. Кaтя, отдaй список брaту."));
        textArray.add(new Pair(48, ".", "*Я смотрю кaк мaмa собирaется и уходит.*"));
        textArray.add(new Pair(49, ".", "Имя рaзблокировaно! Протaгонист -> Ивaн"));
        textArray.add(new Pair(50, ".", "Кaтя: Ивaн, купи мне пожaлуйстa конфет коровкa."));
        textArray.add(new Pair(51, ".", "*Выходa нет...*"));
        textArray.add(new Pair(52, ".", "Ивaн: Хорошо. Я куплю тебе конфет, только обещaй что сегодня вечером ты не будешь меня достaвaть. И пристaвкa вечером моя."));
        textArray.add(new Pair(53, ".", "Кaтя: Ну... нет. Я сегодня должнa пройти яйцо-боссa!"));
        textArray.add(new Pair(54, ".", "Ивaн: Тогдa не куплю."));
        textArray.add(new Pair(55, ".", "*Я беру и нaчинaю отходить от кухонного столa и двигaюсь в нaпрaвлении входной двери*"));
        textArray.add(new Pair(56, ".", "Кaтя: Ну хоть чaсик дaшь поигрaть?.."));
        textArray.add(new Pair(57, ".", "[Дaть Кaте поигрaть чaсик вечером]"));
        textArray.add(new Pair(58, ".", "[Не дaть Кaте игрaть]"));
        textArray.add(new Pair(59, ".", "ПодВетвь: Злой брaтик"));
        textArray.add(new Pair(60, ".", "[Дaть 1 чaс]"));
        textArray.add(new Pair(61, ".", "*Она ударила меня и я умер*"));
        textArray.add(new Pair(62, ".", "ПодВетвь: Добрый брaтик"));
        textArray.add(new Pair(63, ".", "Ивaн: Нуу.. Чaсик дaм."));
        textArray.add(new Pair(64, ".", "Кaтя: Хорошо. Договорились!"));
        textArray.add(new Pair(65, ".", "*Я нaчaл собирaться в мaгaзин. И нaшел свою шaпку которую привез мой пaпa из Польши*"));
        textArray.add(new Pair(66, ".", "*Я вышел из домa и зaкрыл дверь нa зaмок кaк мaмa просилa двa дня нaзaд. Я пошел в обход лесa*"));
        textArray.add(new Pair(67, ".", "*Через 15 минут я зaхожу в мaленький мaгaзин Мaгaзин 'УНИВЕРСAЛ' 24/7"));
        textArray.add(new Pair(68, ".", "*Я зашёл в ларёк. Он был небольшим, но в нём много чего продавалось*"));
        textArray.add(new Pair(69, ".", "*Я решил посмотреть ассортимент магазина. Может что-то нового добавили*"));
        textArray.add(new Pair(70, ".", "*Эх... Жаль, ничено нового. Что ж, возьму что в списке прописала мама*"));
        textArray.add(new Pair(71, ".", "*Я взял 3 огурцa именовaнных кaк 'Усские' и пошел к стенду с помидорaми*"));
        textArray.add(new Pair(72, ".", "*Подходя к стенду с помидорaми я нaступил нa лук и чуть ли не упaв в кого то влетел*"));
        textArray.add(new Pair(73, ".", "*Я влетел в Тётю Петровну, онa рaботaет тут кaк менеджер*"));
        textArray.add(new Pair(74, ".", "Петровнa: Ивaн!? Тебя опять мaмa зaстaвилa пойти зa покупкaми?"));
        textArray.add(new Pair(75, ".", "Иван: Угу"));
        textArray.add(new Pair(76, ".", "*И дал я свой список*"));
        textArray.add(new Pair(77, ".", "Петрована пошла брать продукты из списка"));
        textArray.add(new Pair(78, ".", "*Я решил прогуляться по магазинчику и увидел стенд с конфетами, которые хотела моя сестра*"));
        textArray.add(new Pair(79, ".", "[Купить конфеты сестре?]"));
        textArray.add(new Pair(80, ".", "Нет"));
        textArray.add(new Pair(81, ".", "Под ветвь [Смертный приговор]"));
        textArray.add(new Pair(82, ".", "*Я же сказал, что не куплю конфеты, что же сейчас должно измениться?*"));
        textArray.add(new Pair(83, ".", "Да"));
        textArray.add(new Pair(84, ".", "*Я иду к Петровне что бы забрать пакет с продуктами*"));
        textArray.add(new Pair(85, ".", "Петровна: С тебя 119 грн"));
        textArray.add(new Pair(86, ".", "Ваня: Можно ещё конфеты Коровка?"));
        textArray.add(new Pair(87, ".", "*Я даю ровно 119 грн и ухожу*"));
        textArray.add(new Pair(88, ".", "Петровна: Хорошо"));
        textArray.add(new Pair(89, ".", "[Куда идти?]"));
        textArray.add(new Pair(90, ".", "Прогуляться"));
        textArray.add(new Pair(91, ".", "*Я решил прогуляться*"));
        textArray.add(new Pair(92, ".", "Зайду-ка я в лес*"));
        textArray.add(new Pair(93, ".", "Ваня: Эх... какая-же природа Украины прекрастна. Что не глянь в любую сторону, то сразу идеальные пейзажи!"));
        textArray.add(new Pair(94, ".", "*Я подождал пару минут и мне дала Павловна пакет.*"));
        textArray.add(new Pair(95, ".", "Домой"));
        textArray.add(new Pair(96, ".", "*Любуюсь видами леса*"));
        textArray.add(new Pair(97, ".", "*Я сразу пошёл домой*"));
        textArray.add(new Pair(98, ".", "Петровна: С тебя 140 грн"));
        textArray.add(new Pair(99, ".", "Ваня: Что ж, пора возвращаться"));
        textArray.add(new Pair(100, ".", "Задержка текста на 5 сек"));
        textArray.add(new Pair(101, ".", "*Идя домой, увидел озеро, возле которого я люблю проводить время на едине, но сейчас уже не время*"));
        textArray.add(new Pair(102, ".", "Ваня: Вот"));
        textArray.add(new Pair(103, ".", "*БАЦ*"));
        textArray.add(new Pair(104, ".", "Звук спотыкания"));
        textArray.add(new Pair(105, ".", "Петровна: Спасибо"));
        textArray.add(new Pair(106, ".", "Ваня: ай-ай-ай. Надо же было так споткуться... Блин, теперь неудобно идти, а если опоздаю, то мама будет рагать..."));
        textArray.add(new Pair(107, ".", "*Думаю нужно вернуться домой, что бы дать конфеты сестре*"));
        textArray.add(new Pair(108, ".", "*Вижу подорожник*"));
        textArray.add(new Pair(109, ".", "***"));
        textArray.add(new Pair(110, ".", "*Я пошёл быстро домой*"));
        textArray.add(new Pair(111, ".", "*Завязав колено подорожником я начал ускоряться, но так и до конца шкандыбал*"));
        textArray.add(new Pair(112, ".", "*Спустя пару минут я пришёл домой*"));
        textArray.add(new Pair(113, ".", "*Прийдя домой, мама уже была дома, она посмотрела на тебя*"));
        textArray.add(new Pair(114, ".", "*Открыв двери, Катя стояла возле двери*"));
        textArray.add(new Pair(115, ".", "Мама: О Боже, где тебя носило? Быстро залетай домой, сейчас лечить тебя будем, неуклюжего"));
        textArray.add(new Pair(116, ".", "*Я послушно зашёл, разулся и пошёл в свою комнату*"));
        textArray.add(new Pair(117, ".", "Катя: Ну что, купил?"));
        textArray.add(new Pair(118, ".", "Мама крича: Ваня, ты где?"));
        textArray.add(new Pair(119, ".", "Ваня: Конечно, держи"));
        textArray.add(new Pair(120, ".", "Ваня: идууууууу....."));
        textArray.add(new Pair(121, ".", "*Сестра улыбнулась*"));
        textArray.add(new Pair(122, ".", "*Возвращяясь назад, я взял пакет возле входа и пошёл в кухню*"));
        textArray.add(new Pair(123, ".", "Мама: О, молодец,денег хватило?"));
        textArray.add(new Pair(124, ".", "Ваня: ещё 20 гривен осталось"));
        textArray.add(new Pair(125, ".", "Мама: Можешь себе оставить"));
        textArray.add(new Pair(126, ".", "Ваня: Спасибо"));
        textArray.add(new Pair(127, ".", "*И стало слишком тихо*"));
        textArray.add(new Pair(128, ".", "Мама: Ой, совсем забыла, сейчас чинить будем тебя"));
        textArray.add(new Pair(129, ".", "*Мама взяла бинт и перекись и начала обматывать рану на колене*"));
        textArray.add(new Pair(130, ".", "Черный экран, типо закрыл глаза"));
        textArray.add(new Pair(131, ".", "Мама: Вот и готово"));
        textArray.add(new Pair(132, ".", "Мама: Давно ты не издавал никаких звуков когда тебя мы лечили"));
        textArray.add(new Pair(133, ".", "Ваня: Ага.... Спасибо большое за помощь"));
        textArray.add(new Pair(134, ".", "Мама: Прийму как благодарность в том случае, если будешь внимательнее"));
        textArray.add(new Pair(135, ".", "Ваня: Хорошо..."));
        textArray.add(new Pair(136, ".", "*Ты слышишь шаги*"));
        textArray.add(new Pair(137, ".", "*Оказиваеться это сестра*"));
        textArray.add(new Pair(138, ".", "Мама: Раз все в сборе, будете есть?"));
        textArray.add(new Pair(139, ".", "Сестра: Без него"));
        textArray.add(new Pair(140, ".", "*Катя разворачиваеться и уходит назад к себе в комнату*"));
        textArray.add(new Pair(141, ".", "Мама: что случилось уже с вами?"));
        textArray.add(new Pair(142, ".", "Ваня: Ну...Она просила купить её конфеты, а я отказался. У нас фильм в это время разные идут и она сказала что даст мне половину своего фильма посмотреть, а потом она посмотрит вторую половину своего"));
        textArray.add(new Pair(143, ".", "[Сказать правду?]"));
        textArray.add(new Pair(144, ".", "Да"));
        textArray.add(new Pair(145, ".", "Мама: Понятно, но это же не повод ссориться за то что конфет не купил. Я Поговрю с ней"));
        textArray.add(new Pair(146, ".", "Ваня: Спасибо"));
        textArray.add(new Pair(147, ".", "Нет"));
        textArray.add(new Pair(148, ".", "*Мама пошла к Кати и они что-то там неразборчиво говрили в течении 20 минут, пока ты в это время ужинал сам*"));
        textArray.add(new Pair(149, ".", "*Почти закончивши ужин, мама и Катя заходят в кухню*"));
        textArray.add(new Pair(150, ".", "Сестра: ..."));
        textArray.add(new Pair(151, ".", "Сестра: Я обиделась на тебя, по тому что просто хочу конфеты, я их очень давно не ела. Они мои любимые. Но... Но.... Также сегодня последняя серия мультика, по этому я не могу дать тебе посмотреть твой фильм"));
        textArray.add(new Pair(152, ".", "*Жаль, я сегодня планировал посмотреть повтор фильма, который я пропустил. Я бы уговаривал её, на счёт того что бы она дала мне фильм посмотреть, но зади мама"));
        textArray.add(new Pair(153, ".", "Ваня: Ну... Хорошо... В следуйщий раз, я буду смотреть"));
        textArray.add(new Pair(154, ".", "Сеста: Да, конечно"));
        textArray.add(new Pair(155, ".", "* И тут я увидел небольшую улыбку своей сестры*"));
        textArray.add(new Pair(156, ".", "*Милота....*"));
        textArray.add(new Pair(157, ".", "Мама: Вот и закончим. Надеюсь таких ситуаций будет редко"));
        textArray.add(new Pair(158, ".", "Ваня и Катя: Дааааа...."));
        textArray.add(new Pair(159, ".", "Мама: Катя, иди кушать. Сынок, тебе есть чем заняться?"));
        textArray.add(new Pair(160, ".", "Ваня:Да конечно"));
        textArray.add(new Pair(161, ".", "*На самом делея обманул так как ничего интересного в моей жизни да и комнате нет"));
        textArray.add(new Pair(162, ".", "*Я иду в свою комнату, раздеваюсь, ложусь на кровать и засынаю*"));
        textArray.add(new Pair(163, ".", "*Я оказался в лесу... В очень тёном лесу...*"));
        textArray.add(new Pair(164, ".", "Ваня: интересно..."));
        textArray.add(new Pair(165, ".", "*звуки крика медведя*"));
        textArray.add(new Pair(166, ".", "Ваня: Бежать отсюда к чёрту... Какое счастье что это сон"));
        textArray.add(new Pair(167, ".", "???: Я так не думаю, хе-хе"));
        textArray.add(new Pair(168, ".", "Тут чёрный экран"));
        textArray.add(new Pair(169, ".", "Ваня: А где свет?"));
        textArray.add(new Pair(170, ".", "???: Бу"));
        textArray.add(new Pair(171, ".", "Спавним фотку мишки"));
        textArray.add(new Pair(172, ".", "*Я рефлекторно ударил её по лицу*"));
        textArray.add(new Pair(173, ".", "???: Ай-ай-ай... Больно"));
        textArray.add(new Pair(174, ".", "Ваня: Ты кто?"));
        textArray.add(new Pair(175, ".", "???: Можешь называть меня Мишка, я твой страх"));
        textArray.add(new Pair(176, ".", "*Мой страх? Та чёрт с ним, это же девчёнка говорит что звать её надо в мужском роде*"));
        textArray.add(new Pair(177, ".", "Ваня: Л-ладно..."));
        textArray.add(new Pair(178, ".", "Мишка: Умничка что подхватываешь всё на ходу"));
        textArray.add(new Pair(179, ".", "Ваня: ..."));
        textArray.add(new Pair(180, ".", "Мишка: Люблю таких, хе-хе"));
        textArray.add(new Pair(181, ".", "Ваня: Славв Богу, что это лишь сон"));
        textArray.add(new Pair(182, ".", "Мишка: Это да, но ты в своём сне, где управляю именно я и по этому только я могу тебе разрешить проснуться, хе-хе"));
        textArray.add(new Pair(183, ".", "Ваня: ..."));
        textArray.add(new Pair(184, ".", "Мишка: Вижу что ты в ужасе"));
        textArray.add(new Pair(185, ".", "Ваня: Есть такое"));
        textArray.add(new Pair(186, ".", "*Надо подавать вид, что у меня железные яйца, но на самом деле я щас закричу*"));
        textArray.add(new Pair(187, ".", "Мишка: Хм, я думаю пока что для нашей первой встречи. Так что на этой ноте попрощаемся. Увидимся ночью, хе-хе"));
        textArray.add(new Pair(188, ".", "*Я резко встаю с кровати весь в поту*"));
        textArray.add(new Pair(189, ".", "Ваня: Часы показывают 6 часов утра... Серйозно?"));
        textArray.add(new Pair(190, ".", "*Ну, мой портфель всегда собран так что может пойти в школу"));
        textArray.add(new Pair(191, ".", "*Так как я вышел рано, то можно и по лесу прогуляться*"));
        textArray.add(new Pair(192, ".", "Ваня: Надеюсь еда в столовке будет нормальная"));
        textArray.add(new Pair(193, ".", "[Пойти прямо сейчас в школу?]"));
        textArray.add(new Pair(194, ".", "Да"));
        textArray.add(new Pair(195, ".", "*Что ж делать нечего, пойду в школу*"));
        textArray.add(new Pair(196, ".", "Нет"));
        textArray.add(new Pair(197, ".", "*Идя по лесу, я внимательно следил за всем происходящим вокруг меня, что бы не упасть как вчера*"));
        textArray.add(new Pair(198, ".", "*Спустя час, я наконец-то в школе*"));
        textArray.add(new Pair(199, ".", "*Уроки идут медленно, а я как всегда не слушаю, а просто пялусь в окно*"));
        textArray.add(new Pair(200, ".", "*...*"));
        textArray.add(new Pair(201, ".", "Задержка на 5 сек"));
        textArray.add(new Pair(202, ".", "*Так у меня проходит каждый день*"));
        textArray.add(new Pair(203, ".", "*Друзей у меня нет*"));
        textArray.add(new Pair(204, ".", "*Мне они не нужны*"));
        textArray.add(new Pair(205, ".", "*...*"));
        textArray.add(new Pair(206, ".", "*Единственное что я хочу, то это девушку*"));
        textArray.add(new Pair(207, ".", "*Конечно, желательно что бы были одногодки, но на год старше, тоже допустимо*"));
        textArray.add(new Pair(208, ".", "Звук звонка"));
        textArray.add(new Pair(209, ".", "Ваня: фух... Наконец-то домой."));
        textArray.add(new Pair(210, ".", "*На этот раз я пойду домой быстрой дорогой, не через лес, а то отдохнуть охота"));
        textArray.add(new Pair(211, ".", "Тут задержка на пару секунд с фоном леса"));
        textArray.add(new Pair(212, ".", "..."));
        textArray.add(new Pair(213, ".", "Ваня: Наконец-то дома"));
        textArray.add(new Pair(214, ".", "*Войдя домой, я вижу как Катя кушает хлопья*"));
        textArray.add(new Pair(215, ".", "Ваня: Привет, как дела?"));
        textArray.add(new Pair(216, ".", "Катя: нормально, мама если что, пошла за покупками"));
        textArray.add(new Pair(217, ".", "Ваня: Понятно, ну если что, я посплю тогда"));
        textArray.add(new Pair(218, ".", "Катя: Та делай что хочешь"));
        textArray.add(new Pair(219, ".", "* Я залетаю в свою комнату, раздеваюсь и прыгаю на кровать*"));
        textArray.add(new Pair(220, ".", "..."));
        textArray.add(new Pair(221, ".", "*Я резко засыпаю*"));
        textArray.add(new Pair(222, ".", "???: Привееет!"));
        textArray.add(new Pair(223, ".", "Ваня: ..."));
        textArray.add(new Pair(224, ".", "Ваня: Привет"));
        textArray.add(new Pair(225, ".", "Мишкa И так... Ми не закончили в прошлую встречу"));
        textArray.add(new Pair(226, ".", "Ваня: та мне и прошлого раза хватило, если честно"));
        textArray.add(new Pair(227, ".", "Мишка: Ха-ха, ты забавный)"));
        textArray.add(new Pair(228, ".", "Ваня: ..."));
        textArray.add(new Pair(229, ".", "мишка: И так... Сегодня поговорим о тебе. Ты странный человек, често говоря"));
        textArray.add(new Pair(230, ".", "Ваня: ..."));
        textArray.add(new Pair(231, ".", "Мишка:Таких как ты - мало и они в целом не всегда понимают где..."));
        textArray.add(new Pair(232, ".", "Мишка: Хм..."));
        textArray.add(new Pair(233, ".", "Мишка: Лучше не буду говорить"));
        textArray.add(new Pair(234, ".", "Ваня: то есть ты говоришь мне про проблемы. а саму проблему не хочешь говорить?"));
        textArray.add(new Pair(235, ".", "Мишка: Именно"));
        textArray.add(new Pair(236, ".", "Ваня: ну это же мой сон, я хочу что бы ты сказала мне"));
        textArray.add(new Pair(237, ".", "Мишка: Напомню, это твой сон, но контролирую здесь я"));
        textArray.add(new Pair(238, ".", "*Черт я совсем забыл*"));
        textArray.add(new Pair(239, ".", "Мишка: Ну, единственное что я тебе скажу - будь акуратнее"));
        textArray.add(new Pair(240, ".", "Ваня: Где именно? В ногах, в людях? Перестань головоломки ставить. Парни не понимают намёков, скажи прямо"));
        textArray.add(new Pair(241, ".", "Мишка: Ты забавный, так бы съела, ну ладно... Просто если малейший вопрос будет  тебя в голове, то просто детально проанализируй"));
        textArray.add(new Pair(242, ".", "Ваня: ..."));
        textArray.add(new Pair(243, ".", "Ваня: Ладно?"));
        textArray.add(new Pair(244, ".", "Мишка: Та ты сам поймёшь)"));
        textArray.add(new Pair(245, ".", "Ваня: Ясно..."));
        textArray.add(new Pair(246, ".", "Мишка: Пасмурно"));
        textArray.add(new Pair(247, ".", "Мишка: ладно, на этом пока всё"));
        textArray.add(new Pair(248, ".", "Ваня: Да-да"));
        textArray.add(new Pair(249, ".", "*Я вннезапно проснулся*"));
        textArray.add(new Pair(250, ".", "Ваня: теперь опять в школу"));
        textArray.add(new Pair(251, ".", "*Я оделся, взял еду, которая мама со вчерашнего дня оставила*"));
        textArray.add(new Pair(252, ".", "*Опять пройдусь длинным путём*"));
        textArray.add(new Pair(253, ".", "..."));
        textArray.add(new Pair(254, ".", "*Какая красота в лесу*"));
        textArray.add(new Pair(255, ".", "*Прийдя в школу, я сел за парту и опять провалился в облака*"));
        textArray.add(new Pair(256, ".", "звук звонока"));
        textArray.add(new Pair(257, ".", "*звук на переменку, пойду-ка я прогуляюсь*"));
        textArray.add(new Pair(258, ".", "*Я вышел со школы, что бы намотать пару кругов во круг поля*"));
        textArray.add(new Pair(259, ".", "*Я увидел ученицу, которая сидит на лавочке и следит за мной*"));
        textArray.add(new Pair(260, ".", "*Я не буду подходить к ней так как мне нет дела к ней, лучше пойду в клас*"));
        textArray.add(new Pair(261, ".", "*Зайдя в класс, я опять провалился в мечты*"));
        textArray.add(new Pair(262, ".", "[Подойти к ней?]"));
        textArray.add(new Pair(263, ".", "Нет"));
        textArray.add(new Pair(264, ".", "Звук звонка"));
        textArray.add(new Pair(265, ".", "Да"));
        textArray.add(new Pair(266, ".", "Ваня: Ну слава Богу, это конец"));
        textArray.add(new Pair(267, ".", "*Я встал и пошёл дальше гулять по територии школы*"));
        textArray.add(new Pair(268, ".", "*На том же месте сидит та девочка которая была и в обед*"));
        textArray.add(new Pair(269, ".", "*Я её никогда не видел, наверное потому что не обращаю внимание на окружающих*"));
        textArray.add(new Pair(270, ".", "*Я сел с другой стороны поля*"));
        textArray.add(new Pair(271, ".", "*Посидел пару минут и лёг. Я смотрел в небо до того момента, как не уснул*"));
        textArray.add(new Pair(272, ".", "Мишка: Бу"));
        textArray.add(new Pair(273, ".", "Ваня:.."));
        textArray.add(new Pair(274, ".", "Мишка: Что, уже не пугает?"));
        textArray.add(new Pair(275, ".", "Ваня: не совсем уже"));
        textArray.add(new Pair(276, ".", "*Кому я это говорю?*"));
        textArray.add(new Pair(277, ".", "Мишка: Ну ладно, как знаешь, я буду страшнее тогда"));
        textArray.add(new Pair(278, ".", "Ваня:..."));
        textArray.add(new Pair(279, ".", "Ваня: Так что, зачем я опять здесь?"));
        textArray.add(new Pair(280, ".", "Мишка: А, точно. Ты же видел ту девочку?"));
        textArray.add(new Pair(281, ".", "Ваня: Мою маму?"));
        textArray.add(new Pair(282, ".", "Мишка: Шутник)"));
        textArray.add(new Pair(283, ".", "Ваня: Весь в тебя"));
        textArray.add(new Pair(284, ".", "Мишка: Ну ладно, вот та девчонка которая навпротив тебя сидит - её нельзя допустить к твоему сердцу"));
        textArray.add(new Pair(285, ".", "Ваня: Что?"));
        textArray.add(new Pair(286, ".", "Мишка: Что бы было тебе легче, скажу тебе сразу - она не настоящая"));
        textArray.add(new Pair(287, ".", "Ваня: То есть? Не искренная с эмоциями?"));
        textArray.add(new Pair(288, ".", "Мишка: ..."));
        textArray.add(new Pair(289, ".", "Ваня: И это тоже"));
        textArray.add(new Pair(290, ".", "Ваня: И что ты хочешь что бы я сделал?"));
        textArray.add(new Pair(291, ".", "Ваня: Избегай её"));
        textArray.add(new Pair(292, ".", "Ваня: Зачем?"));
        textArray.add(new Pair(293, ".", "Мишка: Просто сделай"));
        textArray.add(new Pair(294, ".", "Мишка: Зачем?"));
        textArray.add(new Pair(295, ".", "Ваня: Девушку хочу я!"));
        textArray.add(new Pair(296, ".", "Мишка: ха"));
        textArray.add(new Pair(297, ".", "Мишка: ха-ха"));
        textArray.add(new Pair(298, ".", "Мишка: ха-ха-ха-ха-ха-ха-ха-ха"));
        textArray.add(new Pair(299, ".", "Мишка: Ну да, ты же парень..."));
        textArray.add(new Pair(300, ".", "Ваня: В смысле? что то не так?"));
        textArray.add(new Pair(301, ".", "Мишка: Нет, просто пожалуйста, избегай её и ударь её когда будешь один, если получиться - окей, встречайся и делай что хочешь"));
        textArray.add(new Pair(302, ".", "Ваня: Окей"));
        textArray.add(new Pair(303, ".", "Мишка: Я много букв сказала... На этом всё"));
        textArray.add(new Pair(304, ".", "*Я проснулся и открыл глаза*"));
        textArray.add(new Pair(305, ".", "*Та девочка смотрит на меня и стоит возле меня*"));
        textArray.add(new Pair(306, ".", "???: П-Привет"));
        textArray.add(new Pair(307, ".", "Ваня: Эм... Привет?"));
        textArray.add(new Pair(308, ".", "*Та девочка улыбнулась*"));
        textArray.add(new Pair(309, ".", "..."));
        textArray.add(new Pair(310, ".", "*Милота....*"));
        textArray.add(new Pair(311, ".", "???: Я... Я Соня"));
        textArray.add(new Pair(312, ".", "Ваня: Я Ваня"));
        textArray.add(new Pair(313, ".", "Єтот текст нужно сделать микропиздрическим"));
        textArray.add(new Pair(314, ".", "Соня: Я знаю"));
        textArray.add(new Pair(315, ".", "*Мне не послышалось?*"));
        textArray.add(new Pair(316, ".", "Соня: Ты мне нравишься...."));
        textArray.add(new Pair(317, ".", "Ваня: Понятно...."));
        textArray.add(new Pair(318, ".", "Соня: ..."));
        textArray.add(new Pair(319, ".", "Соня: Давай будем гулять с тобой часто?"));
        textArray.add(new Pair(320, ".", "*Хм... А где подвох? Там же Мишка ещё говорила что нужно быть осторожным*"));
        textArray.add(new Pair(321, ".", "[Встречаться?]"));
        textArray.add(new Pair(322, ".", "Нет"));
        textArray.add(new Pair(323, ".", "Ваня: Нет, спасибо"));
        textArray.add(new Pair(324, ".", "Соня: П-почему?"));
        textArray.add(new Pair(325, ".", "Да"));
        textArray.add(new Pair(326, ".", "Ваня: Не нуждаюсь"));
        textArray.add(new Pair(327, ".", "*Та всё равно, это моя жизнь, а я ещё буду каким-то снам доверять свою жизнь*"));
        textArray.add(new Pair(328, ".", "*Мишка, я просрал первый и последний шанс!!!*"));
        textArray.add(new Pair(329, ".", "Соня: Л-ладно"));
        textArray.add(new Pair(330, ".", "Ваня: Давай встрчаться?"));
        textArray.add(new Pair(331, ".", "*Соня ехидно улыбнулась*"));
        textArray.add(new Pair(332, ".", "Соня: Д-да, давай..."));
        textArray.add(new Pair(333, ".", "*Мда...*"));
        textArray.add(new Pair(334, ".", "*А что, так ожно было!?*"));
        textArray.add(new Pair(335, ".", "Соня: НУ что же... Ты точно не хочешь?"));
        textArray.add(new Pair(336, ".", "Ваня: Давай погуляем?"));
        textArray.add(new Pair(337, ".", "Ваня: Точно не хочу"));
        textArray.add(new Pair(338, ".", "*Соня покраснела*"));
        textArray.add(new Pair(339, ".", "Соня: Точно?"));
        textArray.add(new Pair(340, ".", "*Я взёл её за руку*"));
        textArray.add(new Pair(341, ".", "Ваня: Точно"));
        textArray.add(new Pair(342, ".", "*Мы гуляли по лесу*"));
        textArray.add(new Pair(343, ".", "Соня: Точно точно?"));
        textArray.add(new Pair(344, ".", "Соня: Давай пойдём к озеру там где ты сидишь часто?"));
        textArray.add(new Pair(345, ".", "Ваня: ДА!"));
        textArray.add(new Pair(346, ".", "Ваня: Откуда ты знаешь?"));
        textArray.add(new Pair(347, ".", "Соня: То..."));
        textArray.add(new Pair(348, ".", "Соня: Я же твоя девушка, мне же нужно знать всё про тебя, хе-хе"));
        textArray.add(new Pair(349, ".", "Ваня: ДА!!!!!"));
        textArray.add(new Pair(350, ".", "*Звучит разумно, но странно*"));
        textArray.add(new Pair(351, ".", "*Соня ещё больше улыбнулась*"));
        textArray.add(new Pair(352, ".", "*Мы пришли к пляжу*"));
        textArray.add(new Pair(353, ".", "Соня: Ну что же..."));
        textArray.add(new Pair(354, ".", "Соня: Ладно... Скоро увидися"));
        textArray.add(new Pair(355, ".", "*Соня ушла*"));
        textArray.add(new Pair(356, ".", "Ваня: Что это за нахрен было?"));
        textArray.add(new Pair(357, ".", "Ваня..."));
        textArray.add(new Pair(358, ".", "Ваня: ЧТо ж, пойду домой через лес. Пятница же, так что спешить некуда"));
        textArray.add(new Pair(359, ".", "*Я встал и пошёл*"));
        textArray.add(new Pair(360, ".", "..."));
        textArray.add(new Pair(361, ".", "*Я пошёл через лес*"));
        textArray.add(new Pair(362, ".", "Ваня: Зайду-ка я на пляж возле озера"));
        textArray.add(new Pair(363, ".", "Задержка 5 секунд + картинка пляжа"));
        textArray.add(new Pair(364, ".", "Ваня: Как здесь красиво"));
        textArray.add(new Pair(365, ".", "Звуки шуршания"));
        textArray.add(new Pair(366, ".", "Ваня: Кто здесь?"));
        textArray.add(new Pair(367, ".", "???:"));
        textArray.add(new Pair(368, ".", "Ваня: выходи, я не буду бить"));
        textArray.add(new Pair(369, ".", "*Из кустов вышла Соня*"));
        textArray.add(new Pair(370, ".", "Ваня: ..."));
        textArray.add(new Pair(371, ".", "Соня: ..."));
        textArray.add(new Pair(372, ".", "Ваня: Ну, что ты делаешь тут"));
        textArray.add(new Pair(373, ".", "Соня: я тут часто гуляю"));
        textArray.add(new Pair(374, ".", "Ваня: Я думал тут никто не может быть так как это место очень далеко от месности"));
        textArray.add(new Pair(375, ".", "Соня: Это правда"));
        textArray.add(new Pair(376, ".", "*В руке Сони выглядывает мокрая тряпка и нож в другой руке*"));
        textArray.add(new Pair(377, ".", "Ваня: Зачем тебе нож и тряпка, ещё и мокрая"));
        textArray.add(new Pair(378, ".", "*Соня начала приближаться*"));
        textArray.add(new Pair(379, ".", "Соня: Сейчас покажу"));
        textArray.add(new Pair(380, ".", "[Бежать?]"));
        textArray.add(new Pair(381, ".", "Да"));
        textArray.add(new Pair(382, ".", "*Я резко бобежал домой*"));
        textArray.add(new Pair(383, ".", "Нет"));
        textArray.add(new Pair(384, ".", "*Надо сваливать от сюда*"));
        textArray.add(new Pair(385, ".", "Ваня: Х-хорошо)"));
        textArray.add(new Pair(386, ".", "*Эта странная девочка погналась за мной и странно смеясь, как будто для неё это игра, где приз это моя жизнь.*"));
        textArray.add(new Pair(387, ".", "Соня: Правильно, хе-хе"));
        textArray.add(new Pair(388, ".", "*я бежу по лесной тропе*"));
        textArray.add(new Pair(389, ".", "*Что-то мне не посебе...*"));
        textArray.add(new Pair(390, ".", "/ЧТО ЖЕ ЭТО ВСЁ ЗА ЧЕРТОВШИНА/"));
        textArray.add(new Pair(391, ".", "/КАК БУД-ТО БЕГУ УЖЕ ВЕЧНОСТЬ/"));
        textArray.add(new Pair(392, ".", "*Наверное она меня догонит, так что не стану испытывать судьбу"));
        textArray.add(new Pair(393, ".", "Нет, не убегать"));
        textArray.add(new Pair(394, ".", "[Точно не следует убегать?]"));
        textArray.add(new Pair(395, ".", "*Перепрыгиваю ров*"));
        textArray.add(new Pair(396, ".", "Убежать"));
        textArray.add(new Pair(397, ".", "*Соня набросилась на меня с улыбкой, закрыв мне глаза и нос*"));
        textArray.add(new Pair(398, ".", "*я спотыкаюсь о камень*"));
        textArray.add(new Pair(399, ".", "#Мерзские звуки похожие на смех#"));
        textArray.add(new Pair(400, ".", "*Я рвонул от неё, а она почти набросилась на меня*"));
        textArray.add(new Pair(401, ".", "Соня: Тихо, тихо, не бойся, всё будет хорошо, хе-хе"));
        textArray.add(new Pair(402, ".", "[Выбор: подобрать камень и кинуть в неё]"));
        textArray.add(new Pair(403, ".", "[Да]"));
        textArray.add(new Pair(404, ".", "Соня: Эй.... Ты куда???"));
        textArray.add(new Pair(405, ".", "[Нет]"));
        textArray.add(new Pair(406, ".", "Иван: ИДИ К ЧЁРТУ"));
        textArray.add(new Pair(407, ".", "*Я отключился*"));
        textArray.add(new Pair(408, ".", "*Я вскакиваю и начинаю бежать дальше*"));
        textArray.add(new Pair(409, ".", "*Я замахиваюсь и спеша кидю камень в тёмный силуэт*"));
        textArray.add(new Pair(410, ".", "*Я ничего не отвечая бежал*"));
        textArray.add(new Pair(411, ".", "???: Бу..."));
        textArray.add(new Pair(412, ".", "*Пробегаю каменный склон*"));
        textArray.add(new Pair(413, ".", "#КРИК#"));
        textArray.add(new Pair(414, ".", "Ваня: Мишка, не страшно"));
        textArray.add(new Pair(415, ".", "/ещё чуть чуть и я выйду на свет/"));
        textArray.add(new Pair(416, ".", "*Адреналин в крови Ивана поднялся настолько, что он не понял как оказался у двери дома*"));
        textArray.add(new Pair(417, ".", "*Иван выбегает на дорогу*"));
        textArray.add(new Pair(418, ".", "Мишка: Ладно, ладно"));
        textArray.add(new Pair(419, ".", "*Распахивает дверь дома*"));
        textArray.add(new Pair(420, ".", "*Я Мчитсь к дому не замечая прохожих*"));
        textArray.add(new Pair(421, ".", "Мишка: Я вижу ты в полном дерьме..."));
        textArray.add(new Pair(422, ".", "Безумные глаза, полные страха смотрят на семью которая накрывают на стол. Семья готовится к празднику."));
        textArray.add(new Pair(423, ".", "*Открываю дверь*"));
        textArray.add(new Pair(424, ".", "Ваня: Ага..,"));
        textArray.add(new Pair(425, ".", "*Мои глаза полные страха смотрят на семью которая накрывают на стол. Семья готовится к празднику.*"));
        textArray.add(new Pair(426, ".", "Катя: ЧТО ТО СЛУЧИЛОСЬ БРАТИК ?"));
        textArray.add(new Pair(427, ".", "Мишка: Ну прости, я не знала что так будет"));
        textArray.add(new Pair(428, ".", "Катя: ЧТО ТО СЛУЧИЛОСЬ БРАТИК ?"));
        textArray.add(new Pair(429, ".", "*Нежилая испугать родных, ябыстро попытался собраться и ответил.*"));
        textArray.add(new Pair(430, ".", "*Нежилая испугать родных, я быстро попытался собраться и ответил.*"));
        textArray.add(new Pair(431, ".", "Иван: Да так.. ничего.. просто не хотел опоздать к столу"));
        textArray.add(new Pair(432, ".", "Ваня: Хорошо, но если ответишь на вопросы"));
        textArray.add(new Pair(433, ".", "Иван: Да так.. ничего.. просто собаку испугался"));
        textArray.add(new Pair(434, ".", "#СТУК В ДВЕРЬ#"));
        textArray.add(new Pair(435, ".", "Мишка: А что если не хочу?"));
        textArray.add(new Pair(436, ".", "#СТУК В ДВЕРЬ#"));
        textArray.add(new Pair(437, ".", "*меня покрыл пот, ноги заледенели и затряслись, глаз задёргался*"));
        textArray.add(new Pair(438, ".", "*Меня покрыл пот, ноги заледенели и затряслись, в глазах пролетел ужас.*"));
        textArray.add(new Pair(439, ".", "Ваня: Буду бить тебя"));
        textArray.add(new Pair(440, ".", "*Сестра пошла открывать дверь, я попытался что то возразить, но не нашёл нужных слов.*"));
        textArray.add(new Pair(441, ".", "*Сестра пошла открывать дверь, я попытался что то возразить, но потом страх меня сковал.*"));
        textArray.add(new Pair(442, ".", "Мишка: Бидося, страшно, ха-ха. Я твоим сном управляю, ты можешь не проснуться!"));
        textArray.add(new Pair(443, ".", "*Дом сразу наполнился шумом. Это оказались гости пришившие на праздник.*"));
        textArray.add(new Pair(444, ".", "*Дом сразу наполнился шумом. Это оказались гости пришившие на праздник.*"));
        textArray.add(new Pair(445, ".", "Ваня: Мне нужны ответы"));
        textArray.add(new Pair(446, ".", "*Увидев что это были всего лишь гости, я успокоился и пошёл тоже их встречать.*"));
        textArray.add(new Pair(447, ".", "*Я увидел что это были всего лишь гости, Я успокоился и пошёл тоже их встречать.*"));
        textArray.add(new Pair(448, ".", "Мишка: Ну, ладно, давай послушаем, что тебя интересует"));
        textArray.add(new Pair(449, ".", "Гость: ИВАН! ТЫ НАС РАЗВЕ НЕ ЗАМЕТИЛ? ТЫ ПРОБИГАЛ ВОЗЛЕ НАС.. МИНУТЫ 3 НАЗАД."));
        textArray.add(new Pair(450, ".", "Гость: ИВАН! ТЫ НАС РАЗВЕ НЕ ЗАМЕТИЛ? ТЫ ПРОБИГАЛ ВОЗЛЕ НАС.. МИНУТ 5 НАЗАД."));
        textArray.add(new Pair(451, ".", "Иван: ДА НЕ ПРИЗНАЛ.. СПЕШИЛ ДОМОЙ Ответил Иван опустив взгляд на пыльный порог."));
        textArray.add(new Pair(452, ".", "Ваня: В чём смысл жизни?"));
        textArray.add(new Pair(453, ".", "Иван: ДА НЕ ПРИЗНАЛ.. СПЕШИЛ ДОМОЙ Ответил Иван опустив взгляд на порог и увидел пару капель крови."));
        textArray.add(new Pair(454, ".", "Мишка: У тебя серйозно вопросы есть..."));
        textArray.add(new Pair(455, ".", "*Я решил просто полежать*"));
        textArray.add(new Pair(456, ".", "*Я вернулся в свою комнату и разделся*"));
        textArray.add(new Pair(457, ".", "Ваня: Почему бублики готовят с дырками?"));
        textArray.add(new Pair(458, ".", "*Из не одкуда появилась Соня*"));
        textArray.add(new Pair(459, ".", "Ваня: Нужно теперь помыться"));
        textArray.add(new Pair(460, ".", "Ваня: ...."));
        textArray.add(new Pair(461, ".", "Ваня: Что за нафиг???"));
        textArray.add(new Pair(462, ".", "*Я захожу в душевую и тщательно отмываюсь от грязи и крови*"));
        textArray.add(new Pair(463, ".", "Ваня: Чем обычный человек отличается от нормального?"));
        textArray.add(new Pair(464, ".", "*Вибрация* *её ебальник на весь экран*"));
        textArray.add(new Pair(465, ".", "???: привет"));
        textArray.add(new Pair(466, ".", "Мишка: РОТ ЗАКРОЙ"));
        textArray.add(new Pair(467, ".", "*Я подскользнулся и упал*"));
        textArray.add(new Pair(468, ".", "Ваня: ..."));
        textArray.add(new Pair(469, ".", "Ваня: К-как?"));
        textArray.add(new Pair(470, ".", "Мишка: Давай нормальные вопросы"));
        textArray.add(new Pair(471, ".", "Соня: Хе-хе"));
        textArray.add(new Pair(472, ".", "Ваня: 2+2=5?"));
        textArray.add(new Pair(473, ".", "Ваня: ..."));
        textArray.add(new Pair(474, ".", "Мишка: Ты не нормальный... Пока"));
        textArray.add(new Pair(475, ".", "Соня: Ты только мой!"));
        textArray.add(new Pair(476, ".", "Ваня: Ладно, ладно... У мамы спрошу, а если на счёт таких вопросов, то кто ты?"));
        textArray.add(new Pair(477, ".", "Ваня: нет, я говорил что не буду, вот и всё"));
        textArray.add(new Pair(478, ".", "Соня: Сейчас я у тебя не спрашиваю"));
        textArray.add(new Pair(479, ".", "Мишка: Я твоё воображение и всё"));
        textArray.add(new Pair(480, ".", "*БАЦ*"));
        textArray.add(new Pair(481, ".", "Ваня: Ага, а я мать разраба"));
        textArray.add(new Pair(482, ".", "*Соня меня стукнула по голове*"));
        textArray.add(new Pair(483, ".", "Мишка: Я серйозно"));
        textArray.add(new Pair(484, ".", "*Я очнулся в лесу*"));
        textArray.add(new Pair(485, ".", "Ваня: Ну ладно, а кто такая Соня"));
        textArray.add(new Pair(486, ".", "*Внезапно появилась Мишка*"));
        textArray.add(new Pair(487, ".", "Мишка: тоже как и я - воображение твоё."));
        textArray.add(new Pair(488, ".", "Мишка: О, а ты молодец. Долго пробыл"));
        textArray.add(new Pair(489, ".", "Ваня: Как так?"));
        textArray.add(new Pair(490, ".", "Ваня: Ч-что?"));
        textArray.add(new Pair(491, ".", "Мишка: Откуда мне знать, я знаю всё что ты знаешь и думаешь!"));
        textArray.add(new Pair(492, ".", "Мишка: Ты что, даже сейчас не понял?"));
        textArray.add(new Pair(493, ".", "Ваня: Логично...."));
        textArray.add(new Pair(494, ".", "Ваня: нет, а что?"));
        textArray.add(new Pair(495, ".", "Мишка: Ну, тебе к доктору в лучшем случае нужно пойти!"));
        textArray.add(new Pair(496, ".", "Мишка: А как ты думаешь, как девочка может попасть в дом, где все окна закрыты и гости в доме?"));
        textArray.add(new Pair(497, ".", "Ваня: Да, надо..."));
        textArray.add(new Pair(498, ".", "Ваня: ..."));
        textArray.add(new Pair(499, ".", "Мишка: Так а что теперь будешь делать?"));
        textArray.add(new Pair(500, ".", "Ваня: Может быть всё что угодно, если что."));
        textArray.add(new Pair(501, ".", "Ваня: Та нужно сейчас разобраться, как такое возможно что я сам себя вырубаю..."));
        textArray.add(new Pair(502, ".", "Мишка: ..."));
        textArray.add(new Pair(503, ".", "Мишка: ха-ха. Ладно, иди просыпайся"));
        textArray.add(new Pair(504, ".", "Мишка: Ладно, ты так и не понял, ты же ещё и подросток... Надеюсь ты скоро всё поймёшь"));
        textArray.add(new Pair(505, ".", "*Я резко проснулся*"));
        textArray.add(new Pair(506, ".", "Ваня: ..."));
        textArray.add(new Pair(507, ".", "*я лежал на земле*"));
        textArray.add(new Pair(508, ".", "Мишка: Увидимся, я надеюсь..."));
        textArray.add(new Pair(509, ".", "*Я встал, обтрусился и пошёл домой*"));
        textArray.add(new Pair(510, ".", "*Я резко проснулся*"));
        textArray.add(new Pair(511, ".", "*По дороге домой, я думал как мне сказать правильно маме*"));
        textArray.add(new Pair(512, ".", "Ваня: Стоп, а где я?"));
        textArray.add(new Pair(513, ".", "*Я зашёл домой и пошёл в комнату полежать...*"));
        textArray.add(new Pair(514, ".", "*Я привязан к стульчику*"));
        textArray.add(new Pair(515, ".", "*Поспать так и не получилось*"));
        textArray.add(new Pair(516, ".", "Ваня: Ч-что?"));
        textArray.add(new Pair(517, ".", "Ваня: Где я?"));
        textArray.add(new Pair(518, ".", "Ваня: П-почему я привязан?"));
        textArray.add(new Pair(519, ".", "Ваня: К-как?"));
        textArray.add(new Pair(520, ".", "*Я огляделся*"));
        textArray.add(new Pair(521, ".", "*...*"));
        textArray.add(new Pair(522, ".", "*Никого нет*"));
        textArray.add(new Pair(523, ".", "Ваня: Что ж... Вибора нет..."));
        textArray.add(new Pair(524, ".", "[Закричать?]"));
        textArray.add(new Pair(525, ".", "Да"));
        textArray.add(new Pair(526, ".", "Ваня: СПАСИТЕЕЕЕЕЕЕ"));
        textArray.add(new Pair(527, ".", "Нет"));
        textArray.add(new Pair(528, ".", "*Я кричал секунд 20 и внезапно забежали гости и моя семья и я оказался в своей комнате*"));
        textArray.add(new Pair(529, ".", "???: Приветик! Ты уже очнулся уже? Как это здорово!"));
        textArray.add(new Pair(530, ".", "Все в один голос: что случилось?"));
        textArray.add(new Pair(531, ".", "*Это Соня...*"));
        textArray.add(new Pair(532, ".", "*Сзади выглядывает Соня*"));
        textArray.add(new Pair(533, ".", "*У неё в руке был нож... Видимо это конец...*"));
        textArray.add(new Pair(534, ".", "Ваня: СЗАДИ!!!!"));
        textArray.add(new Pair(535, ".", "Соня: Давай прогуляемся?"));
        textArray.add(new Pair(536, ".", "*ВСЕ ПОСМОТРЕЛИ НАЗАД*"));
        textArray.add(new Pair(537, ".", "*Будет ли это моим спасением?*"));
        textArray.add(new Pair(538, ".", "ВСЕ: Что? Тут никого нет..."));
        textArray.add(new Pair(539, ".", "Ваня: Давай?"));
        textArray.add(new Pair(540, ".", "*Что же за чёртовщина тут твориться?*"));
        textArray.add(new Pair(541, ".", "Соня: Ура, ура, ура"));
        textArray.add(new Pair(542, ".", "Ваня: Наверное показалось...."));
        textArray.add(new Pair(543, ".", "*Соня начала развязывать Ваню, но она до сих пор держит нож*"));
        textArray.add(new Pair(544, ".", "Гости: ты так не шути"));
        textArray.add(new Pair(545, ".", "Ваня: Спасибо..."));
        textArray.add(new Pair(546, ".", "Ваня: я наверное посплю"));
        textArray.add(new Pair(547, ".", "*Соня взяла меня за руки и сильно зжала*"));
        textArray.add(new Pair(548, ".", "*Все ушли, а я иду спать*"));
        textArray.add(new Pair(549, ".", "*Думаю лучше ничего не говорить*"));
        textArray.add(new Pair(550, ".", "*Я уснул...*"));
        textArray.add(new Pair(551, ".", "*Мы вышли со здания заброшеного и пошли в лес*"));
        textArray.add(new Pair(552, ".", "Мишка: как дела?"));
        textArray.add(new Pair(553, ".", "*Мы шли долго*"));
        textArray.add(new Pair(554, ".", "Задержка на 5 секунд"));
        textArray.add(new Pair(555, ".", "Ваня: ..."));
        textArray.add(new Pair(556, ".", "Соня: Постой смирно и закрой глаза"));
        textArray.add(new Pair(557, ".", "Мишка: как я говорила.... Думай..."));
        textArray.add(new Pair(558, ".", "*Лучше не спорить*"));
        textArray.add(new Pair(559, ".", "Ваня:..."));
        textArray.add(new Pair(560, ".", "Ваня: Х-хорошо"));
        textArray.add(new Pair(561, ".", "Мишка: Что?"));
        textArray.add(new Pair(562, ".", "*Соня мило улібнулась и обошла меня*"));
        textArray.add(new Pair(563, ".", "Ваня: То есть всё что ты говорила на счёт вымешленых - это правда?"));
        textArray.add(new Pair(564, ".", "*Она завязала мне глаза*"));
        textArray.add(new Pair(565, ".", "Мишка: Наконец-то допёрло."));
        textArray.add(new Pair(566, ".", "*...*"));
        textArray.add(new Pair(567, ".", "Ваня: Выходит что у меня проблемы с головой?"));
        textArray.add(new Pair(568, ".", "*И очень туго!*"));
        textArray.add(new Pair(569, ".", "Мишка: Та не совсем. Я с тобой вот общаюсь и нормально. Ну ты понял"));
        textArray.add(new Pair(570, ".", "*Соня молча взяла меня за руку и повела*"));
        textArray.add(new Pair(571, ".", "Ваня: То есть мне нужно сходить к доктору?"));
        textArray.add(new Pair(572, ".", "*Так мы шли 10 минут. Я каким-то чудом не споткнулся. Ещё в лесу!*"));
        textArray.add(new Pair(573, ".", "Мишка: Как знаешь...."));
        textArray.add(new Pair(574, ".", "Соня: Пришли..."));
        textArray.add(new Pair(575, ".", "[Сходить в больницу?]"));
        textArray.add(new Pair(576, ".", "Да"));
        textArray.add(new Pair(577, ".", "Ваня: Думаю, что да"));
        textArray.add(new Pair(578, ".", "Мишка: Не могу не возразить и не поддержать. Мне всё равно"));
        textArray.add(new Pair(579, ".", "*Соня развязала мне глаза*"));
        textArray.add(new Pair(580, ".", "нет"));
        textArray.add(new Pair(581, ".", "Ваня: Ну и отлично"));
        textArray.add(new Pair(582, ".", "*Мы оказались на том пляже*"));
        textArray.add(new Pair(583, ".", "Ваня: Ну, я не думаю, что это нужно"));
        textArray.add(new Pair(584, ".", "*Я резко проснулся*"));
        textArray.add(new Pair(585, ".", "Соня: Покупаемся?"));
        textArray.add(new Pair(586, ".", "*Мишка немного улыбнулась*"));
        textArray.add(new Pair(587, ".", "Ваня: Так-с, теперь нужно собираться"));
        textArray.add(new Pair(588, ".", "[Покупаться?]"));
        textArray.add(new Pair(589, ".", "нет"));
        textArray.add(new Pair(590, ".", "Мишка: Ну, мне приятно будет"));
        textArray.add(new Pair(591, ".", "*Нужно теперь с этим разобраться и поговорить с мамой*"));
        textArray.add(new Pair(592, ".", "Да"));
        textArray.add(new Pair(593, ".", "Ваня: Давай я просто посижу тут, а ты покупаешься?"));
        textArray.add(new Pair(594, ".", "Ваня: Ты всё равно моя фантазия"));
        textArray.add(new Pair(595, ".", "*Я встал и пошёл в кухню, что бы рассказать это маме*"));
        textArray.add(new Pair(596, ".", "Ваня: давай"));
        textArray.add(new Pair(597, ".", "Соня: Ну хорошо, только ты никуда не иди"));
        textArray.add(new Pair(598, ".", "Мишка: Ну и?"));
        textArray.add(new Pair(599, ".", "*Мама стоит в кухне*"));
        textArray.add(new Pair(600, ".", "Соня: Ура, ура!"));
        textArray.add(new Pair(601, ".", "Ваня Хорошо"));
        textArray.add(new Pair(602, ".", "Ваня: Ладно, тогда будем видеться каждую ночь?"));
        textArray.add(new Pair(603, ".", "Ваня: Мам, можно мы сходим к доктору?"));
        textArray.add(new Pair(604, ".", "*Я залез в воду не раздеваясь*"));
        textArray.add(new Pair(605, ".", "*Соня пошла купаться, а я  рвонул и закричал*"));
        textArray.add(new Pair(606, ".", "Мишка: Конечно"));
        textArray.add(new Pair(607, ".", "Мама: Это из-за вчерашней ситуации?"));
        textArray.add(new Pair(608, ".", "*Я и Соня поплывли до средины озера*"));
        textArray.add(new Pair(609, ".", "Ваня: Тогда, до встречи"));
        textArray.add(new Pair(610, ".", "*Не думаю что ей нужно это говорить, а лучше сразу к доктору сказать это всё*"));
        textArray.add(new Pair(611, ".", "*Тут очень глубоко*"));
        textArray.add(new Pair(612, ".", "Мишка: Агась"));
        textArray.add(new Pair(613, ".", "Мама: Ладно, одевайся"));
        textArray.add(new Pair(614, ".", "Соня: правда здесь прекрасно?"));
        textArray.add(new Pair(615, ".", "*Я проснулся и начал заниматься привычными делами.*"));
        textArray.add(new Pair(616, ".", "Ваня: Большое спасибо"));
        textArray.add(new Pair(617, ".", "Ваня: Очень"));
        textArray.add(new Pair(618, ".", "*Это происхоило днем за днём*"));
        textArray.add(new Pair(619, ".", "*Мы с мамой гуляли по лесу*"));
        textArray.add(new Pair(620, ".", "*Соня замолчала*"));
        textArray.add(new Pair(621, ".", "*Я всё большое проводил время во сне лиш бы не видеть Соню, так как Мишка меня личила*"));
        textArray.add(new Pair(622, ".", "*Зайдя в больницу, я взял талончик*"));
        textArray.add(new Pair(623, ".", "*Я развернулся и Соня напрыгнула на меня*"));
        textArray.add(new Pair(624, ".", "*Меня вызвали к доктору-психиатру*"));
        textArray.add(new Pair(625, ".", "*И одного дня я увидел, как я смтрю на самого себя, но моё тело лежит на кровати, а я стою перед ним*"));
        textArray.add(new Pair(626, ".", "*Она начала меня топить...*"));
        textArray.add(new Pair(627, ".", "*Я рассказал ему все свои проблемы*"));
        textArray.add(new Pair(628, ".", "*Я в пал кому...*"));
        textArray.add(new Pair(629, ".", "*Я начал кричать*"));
        textArray.add(new Pair(630, ".", "*Он внимательно выслушал меня*"));
        textArray.add(new Pair(631, ".", "*Хватка Сони была очень сильна и я не смог даже вынырнуть*"));
        textArray.add(new Pair(632, ".", "Сделать чёрный экран"));
        textArray.add(new Pair(633, ".", "*После этого, он выпроводил меня и пригласил мою маму*"));
        textArray.add(new Pair(634, ".", "*Я задыхался и ничего не мог сделать*"));
        textArray.add(new Pair(635, ".", "*Мама была с доктором дольше за меня*"));
        textArray.add(new Pair(636, ".", "*Она смеялась в это время...*"));
        textArray.add(new Pair(637, ".", "*Выйдя с кабинета, мама рыдала*"));
        textArray.add(new Pair(638, ".", "*Я брысгался, но это не помогло...*"));
        textArray.add(new Pair(639, ".", "*Меня ударили и я отключился*"));
        textArray.add(new Pair(640, ".", "*Меня поместили в камеру и я лечился более 10 лет*"));
        textArray.add(new Pair(641, ".", "*В этот период меня пичкали таблетками*"));
        textArray.add(new Pair(642, ".", "*Из-за этого я не видел ни Мишку, ни Соню*"));
        textArray.add(new Pair(643, ".", "*Я выпустился из больницы*"));
        textArray.add(new Pair(644, ".", "*Вернулся домой и начал работать*"));
        textArray.add(new Pair(645, ".", "*Мы были вновь вместе втроём как ни в чём не бывало*"));
        textArray.add(new Pair(646, ".", "*И от того Вани, который был раньше - уже ничего не осталось*"));






        // Додати інші пари за необхідності
    }

    private void firstBtn() {
        if (textIndex == 10) {
            textIndex = 11;
        } else if (textIndex == 155) {
            textIndex = 158;
        }
        animateText(); // Запускаємо анімацію тексту з нового індексу
    }

    private void secondBtn() {
        if (textIndex == 20) {
            textIndex = 21;
        } else if (textIndex == 255) {
            textIndex = 258;
        }
        animateText(); // Запускаємо анімацію тексту з нового індексу
    }
}
