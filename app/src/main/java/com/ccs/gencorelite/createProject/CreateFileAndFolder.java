package com.ccs.gencorelite.createProject;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateFileAndFolder {
    public static void createFileInDocuments(String fileName, String content) {
        // Отримання шляху до папки Documents
        File documentsDirectory = new File(System.getenv("EXTERNAL_STORAGE"), "Documents");

        // Перевірка, чи папка Documents існує, та створення її, якщо потрібно
        if (!documentsDirectory.exists()) {
            documentsDirectory.mkdirs();
        }

        // Формування шляху до нового файлу
        File file = new File(documentsDirectory, fileName);

        try {
            // Створення об'єкта FileWriter для запису в файл
            FileWriter writer = new FileWriter(file);
            // Запис контенту у файл
            writer.write(content);
            // Закриття потоку запису
            writer.close();
            System.out.println("Файл успішно створено: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Помилка при створенні файлу");
        }
    }


        public static boolean createFolder(Context context, String folderName) {
            // Отримання шляху до внутрішньої директорії вашого застосунку
            File appDirectory = new File(context.getFilesDir(), "Projects");

            // Перевірка, чи папка Projects існує, та створення її, якщо потрібно
            if (!appDirectory.exists()) {
                if (appDirectory.mkdirs()) {
                    System.out.println("Папка 'Projects' успішно створена: " + appDirectory.getAbsolutePath());
                } else {
                    System.out.println("Помилка під час створення папки 'Projects'");
                    return false;
                }
            }

            // Створення папки в папці "Projects"
            File folder = new File(appDirectory, folderName);

            // Перевірка, чи папка вже існує
            if (!folder.exists()) {
                // Створення папки
                if (folder.mkdirs()) {
                    System.out.println("Папка '" + folderName + "' успішно створена: " + folder.getAbsolutePath());
                    return true;
                } else {
                    System.out.println("Помилка під час створення папки '" + folderName + "'");
                    return false;
                }
            } else {
                System.out.println("Папка '" + folderName + "' вже існує: " + folder.getAbsolutePath());
                return false;
            }
        }
    public static void makeFolderSchema(String directoryPath){
        // Створення об'єкта File для представлення папки
        File directory = new File(directoryPath);

        // Перевірка, чи існує вже папка "Schema"
        if (!directory.exists()) {
            // Створення папки "Schema"
            boolean directoryCreated = directory.mkdir();

            // Перевірка, чи вдалося створити папку
            if (directoryCreated) {
                System.out.println("Папка 'Schema' успішно створена");
            } else {
                System.out.println("Не вдалося створити папку 'Schema'");
                return; // Вихід з програми, якщо не вдалося створити папку
            }
        }

        // Шлях до файлу, в який буде записано код
        String filePath = directoryPath + File.separator + "Game.java";

        // Код, який буде записано у файл
        String code = "import android.annotation.SuppressLint;\n" +
                "import android.content.pm.ActivityInfo;\n" +
                "import android.graphics.Color;\n" +
                "import android.media.MediaPlayer;\n" +
                "import android.os.Bundle;\n" +
                "import android.os.Handler;\n" +
                "import android.text.method.ScrollingMovementMethod;\n" +
                "import android.util.DisplayMetrics;\n" +
                "import android.util.Log;\n" +
                "import android.util.TypedValue;\n" +
                "import android.view.View;\n" +
                "import android.view.ViewGroup;\n" +
                "import android.view.WindowManager;\n" +
                "import android.webkit.JavascriptInterface;\n" +
                "import android.webkit.WebChromeClient;\n" +
                "import android.webkit.WebResourceRequest;\n" +
                "import android.webkit.WebSettings;\n" +
                "import android.webkit.WebView;\n" +
                "import android.webkit.WebViewClient;\n" +
                "import android.widget.Button;\n" +
                "import android.widget.LinearLayout;\n" +
                "import android.widget.RelativeLayout;\n" +
                "import android.widget.TextView;\n" +
                "import android.widget.Toast;\n" +
                "\n" +
                "import com.ccs.romanticadventure.data.PreferenceConfig;\n" +
                "import com.ccs.romanticadventure.system.ExitConfirmationDialog;\n" +
                "//супер клас головного вікна, бо тільки так буде працювати код підтвердження виходу з програми\n" +
                "\n" +
                "public class Game_First_Activity extends MainActivity {\n" +
                "\n" +
                "    private WebView webView;\n" +
                "    private int katya, choose, textIndex = 0,value, indexArray, delayBetweenCharacters = 40, //затримка між спавном символів\n" +
                "            delayBetweenTexts = 2000; // затримка між спавнінгом іншого тексту з масиву;\n" +
                "\n" +
                "    private static final int dialogContainerId = View.generateViewId(); // Генерируем уникальный идентификатор для контейнера\n" +
                "\n" +
                "    float volumeLvl;\n" +
                "    MediaPlayer mediaPlayer;\n" +
                "    boolean type, historyBlockIsVisible = false, animationInProgress;\n" +
                "    private Button history, save, load,buttonElement, buttonSecondElement;\n" +
                "    private RelativeLayout bg;\n" +
                "    private TextView textElement, nameElement;\n" +
                "    private String[] textArray;\n" +
                "\n" +
                "    @SuppressLint({\"SetJavaScriptEnabled\", \"WrongViewCast\"})\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.activity_game_first);\n" +
                "        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);\n" +
                "        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);\n" +
                "        choose = PreferenceConfig.getChoose(this);\n" +
                "        history = findViewById(R.id.buttonHistory);\n" +
                "        save = findViewById(R.id.fastSave_btn);\n" +
                "        load = findViewById(R.id.fastLoad_btn);\n" +
                "        bg = findViewById(R.id.bg);\n" +
                "        textElement = findViewById(R.id.dialog);\n" +
                "        nameElement = findViewById(R.id.name);\n" +
                "        type = PreferenceConfig.getAnimSwitchValue(this);\n" +
                "        buttonElement = findViewById(R.id.first_btn);\n" +
                "        buttonSecondElement = findViewById(R.id.second_btn);\n" +
                "        value = PreferenceConfig.getValue(this);\n" +
                "\n" +
                "\n" +
                "        View decorView = getWindow().getDecorView();\n" +
                "        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION\n" +
                "                | View.SYSTEM_UI_FLAG_FULLSCREEN;\n" +
                "        decorView.setSystemUiVisibility(uiOptions);\n" +
                "        volumeLvl = PreferenceConfig.getVolumeLevel(this);\n" +
                "        mediaPlayer = MediaPlayer.create(this, R.raw.school);\n" +
                "        mediaPlayer.setLooping(true);\n" +
                "        mediaPlayer.setVolume(volumeLvl, volumeLvl);\n" +
                "        buttonElement.setVisibility(View.INVISIBLE);\n" +
                "        buttonSecondElement.setVisibility(View.INVISIBLE);\n" +
                "\n" +
                "        history.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                if(!historyBlockIsVisible) {\n" +
                "                    showHistoryDialog();\n" +
                "                }else{\n" +
                "                    hideHistoryDialog();\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "        animateText();\n" +
                "\n" +
                "        load.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                quickLoad();\n" +
                "            }\n" +
                "        });\n" +
                "        save.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                quickSave();\n" +
                "            }\n" +
                "        });\n" +
                "        buttonElement.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                firstBtn();\n" +
                "            }\n" +
                "        });\n" +
                "        buttonSecondElement.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                secondBtn();\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "    @Override\n" +
                "    public void onBackPressed() {\n" +
                "        ExitConfirmationDialog.showExitConfirmationDialog(this);\n" +
                "    }\n" +
                "\n" +
                "    private void quickLoad() {\n" +
                "        textElement.setText(\"\");\n" +
                "        textIndex = value;\n" +
                "    }\n" +
                "\n" +
                "    private void quickSave() {\n" +
                "        // Реализация быстрого сохранения\n" +
                "        PreferenceConfig.setValue(getApplicationContext(), textIndex);\n" +
                "    }\n" +
                "\n" +
                "    private void showHistoryDialog() {\n" +
                "        historyBlockIsVisible = true;\n" +
                "        // Создание диалогового контейнера\n" +
                "        LinearLayout dialogContainer = new LinearLayout(this);\n" +
                "        dialogContainer.setId(dialogContainerId);\n" +
                "        dialogContainer.setLayoutParams(new ViewGroup.LayoutParams(\n" +
                "                convertDpToPx(300), // Ширина контейнера\n" +
                "                convertDpToPx(200) // Высота контейнера\n" +
                "        ));\n" +
                "        dialogContainer.setOrientation(LinearLayout.VERTICAL);\n" +
                "        dialogContainer.setBackgroundColor(Color.WHITE); // Белый цвет фона\n" +
                "        dialogContainer.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Отступы внутри контейнера\n" +
                "        dialogContainer.setBackgroundResource(R.drawable.pink_bg); // Граница контейнера\n" +
                "        dialogContainer.setX(getScreenWidth() / 2f - convertDpToPx(150)); // Положение по горизонтали\n" +
                "        dialogContainer.setY(getScreenHeight() / 2f - convertDpToPx(100)); // Положение по вертикали\n" +
                "\n" +
                "        // Создание элемента для отображения текста\n" +
                "        LinearLayout textContainer = new LinearLayout(this);\n" +
                "        textContainer.setLayoutParams(new LinearLayout.LayoutParams(\n" +
                "                ViewGroup.LayoutParams.MATCH_PARENT,\n" +
                "                ViewGroup.LayoutParams.MATCH_PARENT\n" +
                "        ));\n" +
                "        textContainer.setOrientation(LinearLayout.VERTICAL);\n" +
                "        textContainer.setScrollbarFadingEnabled(false);\n" +
                "        textContainer.setVerticalScrollBarEnabled(true);\n" +
                "        textContainer.setHorizontalScrollBarEnabled(false);\n" +
                "\n" +
                "        // Добавление каждой строки из массива в элемент текста\n" +
                "        for (int i = 0; i < textIndex; i++) {\n" +
                "            TextView textView = new TextView(this);\n" +
                "            textView.setLayoutParams(new LinearLayout.LayoutParams(\n" +
                "                    ViewGroup.LayoutParams.MATCH_PARENT,\n" +
                "                    ViewGroup.LayoutParams.WRAP_CONTENT\n" +
                "            ));\n" +
                "            textView.setText(textArray[i]);\n" +
                "            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);\n" +
                "            textView.setLineSpacing(0, 1.5f);\n" +
                "            textContainer.addView(textView);\n" +
                "            Log.d(\"TextIndex\", String.valueOf(textIndex));\n" +
                "        }\n" +
                "\n" +
                "        // Добавление элемента текста в диалоговый контейнер\n" +
                "        dialogContainer.addView(textContainer);\n" +
                "\n" +
                "        // Добавление диалогового контейнера в корневую разметку активности\n" +
                "        ((ViewGroup) getWindow().getDecorView().getRootView()).addView(dialogContainer);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    // Метод для преобразования dp в px\n" +
                "    private int convertDpToPx(int dp) {\n" +
                "        float scale = getResources().getDisplayMetrics().density;\n" +
                "        return (int) (dp * scale + 0.5f);\n" +
                "    }\n" +
                "\n" +
                "    // Метод для получения ширины экрана\n" +
                "    private int getScreenWidth() {\n" +
                "        DisplayMetrics displayMetrics = new DisplayMetrics();\n" +
                "        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);\n" +
                "        return displayMetrics.widthPixels;\n" +
                "    }\n" +
                "\n" +
                "    // Метод для получения высоты экрана\n" +
                "    private int getScreenHeight() {\n" +
                "        DisplayMetrics displayMetrics = new DisplayMetrics();\n" +
                "        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);\n" +
                "        return displayMetrics.heightPixels;\n" +
                "    }\n" +
                "\n" +
                "    private void hideHistoryDialog() {\n" +
                "        historyBlockIsVisible = false;\n" +
                "        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();\n" +
                "        View dialogContainer = rootView.findViewById(dialogContainerId);\n" +
                "        if (dialogContainer != null) {\n" +
                "            rootView.removeView(dialogContainer);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void animateText() {\n" +
                "        if (!type) {\n" +
                "            return;\n" +
                "        }\n" +
                "        textElement.setText(\"\"); // Очищаем текстовый элемент\n" +
                "\n" +
                "        String newText = textArray[textIndex];\n" +
                "        if (newText.equals(\"росії немає\") || newText.equals(\"абра\")) {\n" +
                "            nameElement.setText(\"Степан\");\n" +
                "        } else {\n" +
                "            nameElement.setText(\"???\"); // Очищаем текст, если условие не выполняется\n" +
                "        }\n" +
                "\n" +
                "        animateFrame(0);\n" +
                "    }\n" +
                "\n" +
                "    private void animateFrame(final int i) {\n" +
                "        String newText = textArray[textIndex];\n" +
                "        new Handler().postDelayed(new Runnable() {\n" +
                "            @Override\n" +
                "            public void run() {\n" +
                "                textElement.append(Character.toString(newText.charAt(i))); // Добавляем символ в текстовый элемент\n" +
                "\n" +
                "                if (i < newText.length() - 1) {\n" +
                "                    animateFrame(i + 1);\n" +
                "                } else {\n" +
                "                    new Handler().postDelayed(new Runnable() {\n" +
                "                        @Override\n" +
                "                        public void run() {\n" +
                "                            if (!type) {\n" +
                "                                return;\n" +
                "                            }\n" +
                "                            textIndex++;\n" +
                "\n" +
                "                            if (textIndex == 4 || textIndex == 17 || textIndex == 29) {\n" +
                "                                buttonElement.setVisibility(View.VISIBLE);\n" +
                "                                buttonSecondElement.setVisibility(View.VISIBLE);\n" +
                "                                animationInProgress = false;\n" +
                "                                return;\n" +
                "                            } else {\n" +
                "                                buttonElement.setVisibility(View.GONE);\n" +
                "                                buttonSecondElement.setVisibility(View.GONE);\n" +
                "                            }\n" +
                "\n" +
                "                            buttonElement.setOnClickListener(new View.OnClickListener() {\n" +
                "                                @Override\n" +
                "                                public void onClick(View v) {\n" +
                "                                    firstBtn();\n" +
                "                                }\n" +
                "                            });\n" +
                "                            buttonSecondElement.setOnClickListener(new View.OnClickListener() {\n" +
                "                                @Override\n" +
                "                                public void onClick(View v) {\n" +
                "                                    secondBtn();\n" +
                "                                }\n" +
                "                            });\n" +
                "                            animationInProgress = true;\n" +
                "                            animateText();\n" +
                "                            indexArray = i;\n" +
                "                        }\n" +
                "                    }, delayBetweenTexts);\n" +
                "                }\n" +
                "            }\n" +
                "        }, delayBetweenCharacters);\n" +
                "    }\n" +
                "    private void firstBtn(){\n" +
                "\n" +
                "        switch (textIndex){\n" +
                "            case 3:\n" +
                "                textIndex = 4;\n" +
                "                break;\n" +
                "            case 17:\n" +
                "                nameElement.setText(\"???\");\n" +
                "                textElement.setText(\"А що ж мені купити? Список дасиш, як минулого разу?\");\n" +
                "                textIndex = 18;\n" +
                "                break;\n" +
                "        }\n" +
                "\n" +
                "    }\n" +
                "    private void secondBtn(){\n" +
                "        switch (textIndex){\n" +
                "            case 3:\n" +
                "                nameElement.setText(\"???\");\n" +
                "                textElement.setText(\"Ні, так не піде\");\n" +
                "                textIndex = 3;\n" +
                "                break;\n" +
                "            case 17:\n" +
                "                nameElement.setText(\"???\");\n" +
                "                textElement.setText(\"А що ж мені купити? Список дасиш, як минулого разу?\");\n" +
                "                textIndex = 18;\n" +
                "                break;\n" +
                "        }\n" +
                "    }\n" +
                "    private void writeFile(String file) {\n" +
                "        try {\n" +
                "            BufferedReader reader = new BufferedReader(new FileReader(file));\n" +
                "            StringBuilder stringBuilder = new StringBuilder();\n" +
                "            String line;\n" +
                "            while ((line = reader.readLine()) != null) {\n" +
                "                stringBuilder.append(line);\n" +
                "            }\n" +
                "            reader.close();\n" +
                "\n" +
                "            // Отримуємо рядок з файлу\n" +
                "            String fileContent = stringBuilder.toString();\n" +
                "\n" +
                "            // Розділяємо рядок на масив, використовуючи кому як роздільник\n" +
                "            String[] dataArray = fileContent.split(\",\");\n" +
                "\n" +
                "            // Оновлюємо textArray з отриманим масивом даних\n" +
                "            textArray = dataArray;\n" +
                "\n" +
                "            // Використання масиву у вашому коді\n" +
                "            // Наприклад, вивід першого елементу масиву:\n" +
                "            if (textArray.length > 0) {\n" +
                "                System.out.println(\"Перший елемент масиву: \" + textArray[0]);\n" +
                "            }\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "    @Override\n" +
                "    protected void onStart() {\n" +
                "        super.onCreate();\n" +
                "        writeFile();\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "\n" +
                "}\n";

        // Запис коду у файл
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(code);
            writer.close();
            System.out.println("Код успішно записано у файл " + filePath);
        } catch (IOException e) {
            System.out.println("Помилка під час запису у файл: " + e.getMessage());
        }
    }
    }
