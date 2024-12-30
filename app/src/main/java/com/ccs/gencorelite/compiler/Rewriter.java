package com.ccs.gencorelite.compiler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ccs.gencorelite.data.PreferenceConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Rewriter {

    // Структура для хранения данных
    public static class Entry {
        String background;
        String music;
        String sound;
        String character;
        String text;
        List<String> choices;
        int nextScene;

        public Entry(String background, String music, String sound, String character, String text, List<String> choices, int nextScene) {
            this.background = background;
            this.music = music;
            this.sound = sound;
            this.character = character;
            this.text = text;
            this.choices = choices;
            this.nextScene = nextScene;
        }
    }

    // Метод для парсинга скрипта
    private static void parseScript(String scriptContent, List<Entry> entries) {
        String[] lines = scriptContent.split("\n");
        String currentBackground = "";
        String currentMusic = "";
        String currentSound = "";
        String currentCharacter = "";
        String currentText = "";
        List<String> currentChoices = new ArrayList<>();
        int nextScene = -1;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#") || line.startsWith("CHOICE")) continue; // Пропускаем пустые строки и комментарии
            Log.d("REWRITER / ParseScript", "Processing line: " + line);

            if (line.startsWith("SCENE")) {
                // Новая сцена, сохраняем предыдущую (если есть)
                if (!currentText.isEmpty()) {
                    entries.add(new Entry(currentBackground, currentMusic, currentSound, currentCharacter, currentText, currentChoices, nextScene));
                    currentChoices = new ArrayList<>();
                    Log.d("REWRITER / ParseScript", "Added entry: " + currentCharacter + ": " + currentText);
                }
                currentText = "";
            } else if (line.startsWith("BACKGROUND")) {
                currentBackground = line.split(" ")[1].replace(".png", "").replace(".jpg", ""); // Убираем расширение
            } else if (line.startsWith("MUSIC")) {
                currentMusic = line.split(" ")[1].replace(".mp3", ""); // Убираем расширение
            } else if (line.startsWith("SOUND")) {
                currentSound = line.split(" ")[1].replace(".wav", ""); // Убираем расширение
            } else if (line.startsWith("DIALOG")) {
                String[] parts = line.split(":");
                currentCharacter = parts[0].split(" ")[1];
                currentText = parts[1].trim();
                Log.d("REWRITER / ParseScript", "Found dialog: " + currentCharacter + ": " + currentText);
            }
//            else if (line.startsWith("CHOICE")) {
//                String[] parts = line.split(":")[1].split("\\|");
//                for (String choice : parts) {
//                    currentChoices.add(choice.trim());
//                }
//            }
            else if (line.startsWith("GOTO")) {
                nextScene = Integer.parseInt(line.split(" ")[1]);
            } else if (line.startsWith("END")) {
                // Конец сцены, сохраняем текущую сцену
                entries.add(new Entry(currentBackground, currentMusic, currentSound, currentCharacter, currentText, currentChoices, nextScene));
                currentText = "";
                Log.d("REWRITER / ParseScript", "Added entry: " + currentCharacter + ": " + currentText);
            }
        }
    }    // Метод для генерации Java кода
    private static void generateJavaCode(List<Entry> entries, PrintWriter output, Context context) {
        Log.d("REWRITER / GenerateJavaCode", "Starting generating Java Code");
        String package_project = PreferenceConfig.getPackage(context);
        output.println("package "+package_project+";");
        output.println();

        output.println("import android.annotation.SuppressLint;");
        output.println("import android.content.pm.ActivityInfo;");
        output.println("import android.graphics.Color;");
        output.println("import android.media.MediaPlayer;");
        output.println("import android.os.Bundle;");
        output.println("import android.os.Handler;");
        output.println("import android.text.method.ScrollingMovementMethod;");
        output.println("import android.util.DisplayMetrics;");
        output.println("import android.util.Log;");
        output.println("import android.util.TypedValue;");
        output.println("import android.view.View;");
        output.println("import android.view.ViewGroup;");
        output.println("import android.view.WindowManager;");
        output.println("import android.webkit.JavascriptInterface;");
        output.println("import android.webkit.WebChromeClient;");
        output.println("import android.webkit.WebResourceRequest;");
        output.println("import android.webkit.WebSettings;");
        output.println("import android.webkit.WebView;");
        output.println("import android.webkit.WebViewClient;");
        output.println("import android.widget.Button;");
        output.println("import android.widget.LinearLayout;");
        output.println("import android.widget.RelativeLayout;");
        output.println("import android.widget.TextView;");
        output.println("import android.widget.Toast;");
        output.println();
        output.println("import java.util.ArrayList;");
        output.println("import java.util.Arrays;");
        output.println("import java.util.HashMap;");
        output.println("import java.util.LinkedHashMap;");
        output.println("import java.util.List;");
        output.println("import java.util.Map;");
        output.println("import java.util.HashSet;");
        output.println();
        output.println("import "+package_project+".data.PreferenceConfig;");
        output.println("import "+package_project+".system.ExitConfirmationDialog;");
        output.println("import android.app.Activity;");

        output.println("public class Game_First_Activity extends Activity {");
        output.println();
        output.println("    private int textIndex = 0, delayBetweenCharacters = 40, delayBetweenTexts = 2000, value;");
        output.println("    private static final int dialogContainerId = View.generateViewId();");
        output.println("    private float volumeLvl;");
        output.println("    private MediaPlayer mediaPlayer;");
        output.println("    private boolean historyBlockIsVisible = false, animationInProgress;");
        output.println("    private Button history, save, load;");
        output.println("    private RelativeLayout bg;");
        output.println("    private TextView textElement, nameElement;");
        output.println("    private ArrayList<Pair> textArray = new ArrayList<>();");
        output.println("    private HashSet<Integer> specialIndexes = new HashSet<>();");
        output.println();
        output.println("    private static class Pair {");
        output.println("        String name;");
        output.println("        String text;");
        output.println("        int value;");
        output.println();
        output.println("        Pair(int value, String name, String text) {");
        output.println("            this.name = name;");
        output.println("            this.text = text;");
        output.println("            this.value = value;");
        output.println("        }");
        output.println("    }");
        output.println();
        output.println("    @SuppressLint({\"SetJavaScriptEnabled\", \"WrongViewCast\"})");
        output.println("    @Override");
        output.println("    protected void onCreate(Bundle savedInstanceState) {");
        output.println("        super.onCreate(savedInstanceState);");
        output.println("        setContentView(R.layout.activity_game_first);");
        output.println("        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);");
        output.println("        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);");
        output.println();
        output.println("        history = findViewById(R.id.buttonHistory);");
        output.println("        save = findViewById(R.id.fastSave_btn);");
        output.println("        load = findViewById(R.id.fastLoad_btn);");
        output.println("        bg = findViewById(R.id.bg);");
        output.println("        textElement = findViewById(R.id.dialog);");
        output.println("        nameElement = findViewById(R.id.name);");
        output.println();
        output.println("        initializeTextArray();");
        output.println();
        output.println("        View decorView = getWindow().getDecorView();");
        output.println("        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;");
        output.println("        decorView.setSystemUiVisibility(uiOptions);");
        output.println();
        output.println("        mediaPlayer = MediaPlayer.create(this, R.raw.school);");
        output.println("        mediaPlayer.setLooping(true);");
        output.println("        mediaPlayer.setVolume(volumeLvl, volumeLvl);");
        output.println();
        output.println("        animateText();");
        output.println();
        output.println("        history.setOnClickListener(new View.OnClickListener() {");
        output.println("            @Override");
        output.println("            public void onClick(View v) {");
        output.println("                if (!historyBlockIsVisible) {");
        output.println("                    showHistoryDialog();");
        output.println("                } else {");
        output.println("                    hideHistoryDialog();");
        output.println("                }");
        output.println("            }");
        output.println("        });");
        output.println();
        output.println("        load.setOnClickListener(new View.OnClickListener() {");
        output.println("            @Override");
        output.println("            public void onClick(View v) {");
        output.println("                quickLoad();");
        output.println("            }");
        output.println("        });");
        output.println();
        output.println("        save.setOnClickListener(new View.OnClickListener() {");
        output.println("            @Override");
        output.println("            public void onClick(View v) {");
        output.println("                quickSave();");
        output.println("            }");
        output.println("        });");
        output.println("    }");
        output.println();
        output.println("    private void initializeTextArray() {");

        // Добавляем все записи из entries
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            output.println("        textArray.add(new Pair(" + index + ", \"" + entry.character + "\", \"" + entry.text + "\"));");
            Log.d("REWRITER / GenerateJavaCode", "Added to textArray: " + entry.character + ": " + entry.text);

            // Если entry имеет особые параметры, добавляем их в specialIndexes
            if (!entry.background.isEmpty() || !entry.music.isEmpty() || !entry.sound.isEmpty()) {
                output.println("        specialIndexes.add(" + index + ");");
            }

            // Добавляем код для установки фона, музыки и звука
            if (!entry.background.isEmpty()) {
                output.println("        bg.setBackgroundResource(R.drawable." + entry.background + ");");
            }
            if (!entry.music.isEmpty()) {
                output.println("        mediaPlayer = MediaPlayer.create(this, R.raw." + entry.music + ");");
                output.println("        mediaPlayer.setLooping(true);");
                output.println("        mediaPlayer.setVolume(volumeLvl, volumeLvl);");
            }
            if (!entry.sound.isEmpty()) {
                output.println("        mediaPlayer = MediaPlayer.create(this, R.raw." + entry.sound + ");");
                output.println("        mediaPlayer.start();");
            }

            // Добавляем код для обработки выбора
//            if (!entry.choices.isEmpty()) {
//                output.println("        // Обработка выбора");
//                for (String choice : entry.choices) {
//                    output.println("        Button choiceButton = new Button(this);");
//                    output.println("        choiceButton.setText(\"" + choice + "\");");
//                    output.println("        choiceButton.setOnClickListener(new View.OnClickListener() {");
//                    output.println("            @Override");
//                    output.println("            public void onClick(View v) {");
//                    output.println("                // Логика для обработки выбора");
//                    output.println("            }");
//                    output.println("        });");
//                    output.println("        bg.addView(choiceButton);");
//                }
//            }
        }

        output.println("    }");
        output.println();
        output.println();

        // Додаємо метод animateText()
        output.println("    private void animateText() {");
        output.println("        if (textIndex < textArray.size()) {");
        output.println("            Pair pair = textArray.get(textIndex);");
        output.println();
        output.println("            // Якщо ключ не містить спешл значення, показати ім'я");
        output.println("            if (specialIndexes.contains(textIndex)) {");
        output.println("                nameElement.setText(pair.name);");
        output.println("            } else {");
        output.println("                nameElement.setText(\"Кейт\");");
        output.println("            }");
        output.println();
        output.println("            textElement.setText(\"\");");
        output.println("            String textToAnimate = pair.text;");
        output.println("            animationInProgress = true;");
        output.println("            new Handler().postDelayed(new Runnable() {");
        output.println("                int i = 0;");
        output.println();
        output.println("                @Override");
        output.println("                public void run() {");
        output.println("                    if (i < textToAnimate.length()) {");
        output.println("                        textElement.append(String.valueOf(textToAnimate.charAt(i)));");
        output.println("                        i++;");
        output.println("                        new Handler().postDelayed(this, delayBetweenCharacters);");
        output.println("                    } else {");
        output.println("                        textIndex++;");
        output.println("                        animationInProgress = false;");
        output.println("                        new Handler().postDelayed(new Runnable() {");
        output.println("                            @Override");
        output.println("                            public void run() {");
        output.println("                                animateText(); // Call the method recursively to show the next text");
        output.println("                            }");
        output.println("                        }, delayBetweenTexts);");
        output.println("                    }");
        output.println("                }");
        output.println("            }, delayBetweenCharacters);");
        output.println("        } else {");
        output.println("            textElement.setText(\"\");");
        output.println("        }");
        output.println("    }");
        output.println();
        // Додаємо метод hideHistoryDialog()
        output.println("    private void hideHistoryDialog() {");
        output.println("        historyBlockIsVisible = false;");
        output.println("        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();");
        output.println("        View dialogContainer = rootView.findViewById(dialogContainerId);");
        output.println("        if (dialogContainer != null) {");
        output.println("            rootView.removeView(dialogContainer);");
        output.println("        }");
        output.println("    }");
        output.println();

        // Додаємо метод showHistoryDialog()
        output.println("    private void showHistoryDialog() {");
        output.println("        historyBlockIsVisible = true;");
        output.println("        // Створення діалогового контейнера");
        output.println("        LinearLayout dialogContainer = new LinearLayout(this);");
        output.println("        dialogContainer.setId(dialogContainerId);");
        output.println("        dialogContainer.setLayoutParams(new ViewGroup.LayoutParams(");
        output.println("                convertDpToPx(300), // Ширина контейнера");
        output.println("                convertDpToPx(200) // Висота контейнера");
        output.println("        ));");
        output.println("        dialogContainer.setOrientation(LinearLayout.VERTICAL);");
        output.println("        dialogContainer.setBackgroundColor(Color.WHITE); // Білий колір фону");
        output.println("        dialogContainer.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Відступи всередині контейнера");
        output.println("        dialogContainer.setBackgroundResource(R.drawable.pink_bg); // Границя контейнера");
        output.println("        dialogContainer.setX(getScreenWidth() / 2f - convertDpToPx(150)); // Положення по горизонталі");
        output.println("        dialogContainer.setY(getScreenHeight() / 2f - convertDpToPx(100)); // Положення по вертикалі");
        output.println();
        output.println("        // Створення елемента для відображення тексту");
        output.println("        LinearLayout textContainer = new LinearLayout(this);");
        output.println("        textContainer.setLayoutParams(new LinearLayout.LayoutParams(");
        output.println("                ViewGroup.LayoutParams.MATCH_PARENT,");
        output.println("                ViewGroup.LayoutParams.MATCH_PARENT");
        output.println("        ));");
        output.println("        textContainer.setOrientation(LinearLayout.VERTICAL);");
        output.println("        textContainer.setScrollbarFadingEnabled(false);");
        output.println("        textContainer.setVerticalScrollBarEnabled(true);");
        output.println("        textContainer.setHorizontalScrollBarEnabled(false);");
        output.println();
        output.println("        // Додавання кожного ключа та значення з HashMap до текстового елемента");
        output.println("        for (int i = 0; i < textIndex; i++) {");
        output.println("            Pair pair = textArray.get(i);");
        output.println();
        output.println("            TextView textView = new TextView(this);");
        output.println("            textView.setLayoutParams(new LinearLayout.LayoutParams(");
        output.println("                    ViewGroup.LayoutParams.MATCH_PARENT,");
        output.println("                    ViewGroup.LayoutParams.WRAP_CONTENT");
        output.println("            ));");
        output.println("            textView.setText(pair.name + \": \" + pair.text);");
        output.println("            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);");
        output.println("            textView.setLineSpacing(0, 1.5f);");
        output.println("            textContainer.addView(textView);");
        output.println("            Log.d(\"TextIndex\", String.valueOf(textIndex));");
        output.println("        }");
        output.println();
        output.println("        // Додавання текстового елемента до діалогового контейнера");
        output.println("        dialogContainer.addView(textContainer);");
        output.println();
        output.println("        // Додавання діалогового контейнера до кореневої розмітки активності");
        output.println("        ((ViewGroup) getWindow().getDecorView().getRootView()).addView(dialogContainer);");
        output.println("    }");
        output.println();

        // Додаємо метод quickLoad()
        output.println("    private void quickLoad() {");
        output.println("        textElement.setText(\"\");");
        output.println("        textIndex = value;");
        output.println("    }");
        output.println();

        // Додаємо метод quickSave()
        output.println("    private void quickSave() {");
        output.println("        // Реализация быстрого сохранения");
        output.println("        PreferenceConfig.setValue(getApplicationContext(), textIndex);");
        output.println("    }");
        output.println();

        // Додаємо допоміжні методи для розмірів екрану та dp
        output.println("    private int convertDpToPx(int dp) {");
        output.println("        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());");
        output.println("    }");
        output.println();
        output.println("    private int getScreenWidth() {");
        output.println("        return getResources().getDisplayMetrics().widthPixels;");
        output.println("    }");
        output.println();
        output.println("    private int getScreenHeight() {");
        output.println("        return getResources().getDisplayMetrics().heightPixels;");
        output.println("    }");
        output.println();

        output.println("}");
    }    // Основная функция для чтения данных и генерации Java кода
    public static void generateScript(String inputPath, String outputPath, Context context) throws IOException {
        Log.d("REWRITER / GenerateScript", "Starting generating. Input path: ["+inputPath+"]");
        Log.d("REWRITER / GenerateScript", "Starting generating. Output path: ["+outputPath+"]");

        BufferedReader inputFile = new BufferedReader(new FileReader(inputPath));
        PrintWriter outputFile = new PrintWriter(new FileWriter(outputPath));
        Log.d("REWRITER / GenerateScript", "Starting generating. Generating input file: ["+inputFile+"]");
        Log.d("REWRITER / GenerateScript", "Starting generating. Generating output file: ["+outputFile+"]");
        // Чтение содержимого файла
        StringBuilder scriptContent = new StringBuilder();
        String line;
        while ((line = inputFile.readLine()) != null) {
            scriptContent.append(line).append("\n");
        }

        List<Entry> entries = new ArrayList<>();
        parseScript(scriptContent.toString(), entries);
        generateJavaCode(entries, outputFile, context);

        inputFile.close();
        outputFile.close();
        Log.d("REWRITER / GenerateScript", "File was generated. Generated file: ["+outputPath+"]");
        System.out.println("Код успешно сгенерирован и записан в файл: " + outputPath);
    }

    // AsyncTask для выполнения операций ввода-вывода в фоновом потоке
    public static class FileOperationTask extends AsyncTask<Void, Void, Void> {
        private String inputPath;
        private String outputPath;
        private Context context;

        public FileOperationTask(String inputPath, String outputPath, Context context) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                generateScript(inputPath, outputPath, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Обновите UI после завершения операции
            Log.d("REWRITER / onPostExecute", "File was generated.");
        }
    }
}