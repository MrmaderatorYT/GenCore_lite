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

public class RewriterMain {

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

            if (line.startsWith("SCENE MAIN")) {
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
            } else if (line.startsWith("GOTO")) {
                nextScene = Integer.parseInt(line.split(" ")[1]);
            } else if (line.startsWith("END")) {
                // Конец сцены, сохраняем текущую сцену
                entries.add(new Entry(currentBackground, currentMusic, currentSound, currentCharacter, currentText, currentChoices, nextScene));
                currentText = "";
                Log.d("REWRITER / ParseScript", "Added entry: " + currentCharacter + ": " + currentText);
            }
        }
    }

    // Метод для генерации Java кода
    private static void generateJavaCode(List<Entry> entries, PrintWriter output, Context context) {
        Log.d("REWRITER / GenerateJavaCode", "Starting generating Java Code");
        String package_project = PreferenceConfig.getPackage(context);
        output.println("package " + package_project+";");
        output.println();

        output.println("import android.annotation.SuppressLint;");
        output.println("import android.content.Intent;");
        output.println("import android.content.pm.ActivityInfo;");
        output.println("import android.media.MediaPlayer;");
        output.println("import android.os.Bundle;");
        output.println("import android.view.MotionEvent;");
        output.println("import android.view.View;");
        output.println("import android.view.WindowManager;");
        output.println("import android.widget.TextView;");
        output.println("import android.widget.ImageView;");
        output.println();
        output.println("import android.app.Activity;");
        output.println();
        output.println("import "+package_project+".data.PreferenceConfig;");
        output.println("import "+package_project+".system.ExitConfirmationDialog;");
        output.println();
        output.println("public class MainActivity extends Activity implements View.OnTouchListener {");
        output.println("    TextView startGame, settings, info, name;");
        output.println("    MediaPlayer mp;");
        output.println("    float volume;");
        output.println("    ImageView backgroundImage;\n;");
        output.println();
        output.println("    //метод, який створює екран");
        output.println("    @SuppressLint(\"MissingInflatedId\")");
        output.println("    @Override");
        output.println("    protected void onCreate(Bundle savedInstanceState) {");
        output.println("        super.onCreate(savedInstanceState);");
        output.println("        setContentView(R.layout.activity_main);");
        output.println("        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);");
        output.println("        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);");
        output.println("        View decorView = getWindow().getDecorView();");
        output.println("        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;");
        output.println("        decorView.setSystemUiVisibility(uiOptions);");
        output.println();
        output.println("        //фіксуємо орієнтацію яка не зміниться (альбомна)");
        output.println("        info = findViewById(R.id.info);");
        output.println("        settings = findViewById(R.id.settings);");
        output.println("        startGame = findViewById(R.id.startBtn);");
        output.println("        name = findViewById(R.id.title);");
        output.println("        backgroundImage = findViewById(R.id.background);");

        output.println("        //завантажуємо дані, які нам потрібно");
        output.println("    }");
        output.println();
        output.println("    //Метод який завжди виконується при старті програми");
        output.println("    @SuppressLint(\"ClickableViewAccessibility\")");
        output.println("    @Override");
        output.println("    protected void onStart() {");
        output.println("        super.onStart();");
        output.println("        volume = PreferenceConfig.getVolumeLevel(this);");
        output.println("        startGame.setOnTouchListener(MainActivity.this);");
        output.println("        settings.setOnTouchListener(this);");
        output.println("        info.setOnTouchListener(this);");
        output.println("        //створюємо пісню, яка буде нескінченною");
        output.println("        //і запускаємо");
        output.println("        mp = MediaPlayer.create(this, R.raw.intro);");
        output.println("        mp.setLooping(true);");
        output.println("        mp.setVolume(volume, volume);");
        output.println("        mp.start();");
        output.println();
        output.println("        // Встановлюємо фон та музику згідно з даними з файлу");
        output.println("        setBackgroundAndMusic();");
        output.println("    }");
        output.println();
        output.println("    private void setBackgroundAndMusic() {");
        output.println("        // Тут встановлюємо фон та музику згідно з даними з файлу");
        for (Entry entry : entries) {
            if (!entry.background.isEmpty()) {
                output.println("        backgroundImage.setBackgroundResource(R.drawable." + entry.background + ");");
            }
            if (!entry.music.isEmpty()) {
                output.println("        mp = MediaPlayer.create(this, R.raw." + entry.music + ");");
                output.println("        mp.setLooping(true);");
                output.println("        mp.setVolume(volume, volume);");
                output.println("        mp.start();");
            }
            if(!entry.text.isEmpty()){
                output.println("        name.setText("+entry.text+");");
            }
        }
        output.println("    }");
        output.println();
        output.println("    private void preferences() {");
        output.println("        volume = PreferenceConfig.getVolumeLevel(this);");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    public void onBackPressed() {");
        output.println("        ExitConfirmationDialog.showExitConfirmationDialog(this);");
        output.println("    }");
        output.println();
        output.println("    public void exit() {");
        output.println("        finish(); // завершення актівіті");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    protected void onPause() {");
        output.println("        super.onPause();");
        output.println("        releaseMediaPlayer();");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    protected void onDestroy() {");
        output.println("        super.onDestroy();");
        output.println("        releaseMediaPlayer();");
        output.println("    }");
        output.println();
        output.println("    private void releaseMediaPlayer() {");
        output.println("        if (mp != null) {");
        output.println("            mp.release();");
        output.println("            mp = null;");
        output.println("        }");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    public boolean onTouch(View v, MotionEvent event) {");
        output.println("        switch (v.getId()) {");
        output.println("            case R.id.startBtn:");
        output.println("                startActivity(new Intent(MainActivity.this, Game_First_Activity.class));");
        output.println("                overridePendingTransition(0, 0);");
        output.println("                break;");
        output.println("            case R.id.settings:");
        output.println("                startActivity(new Intent(MainActivity.this, Settings.class));");
        output.println("                overridePendingTransition(0, 0);");
        output.println("                break;");
        output.println("            case R.id.info:");
        output.println("                break;");
        output.println("        }");
        output.println("        return false;");
        output.println("    }");
        output.println("}");
    }

    // Основная функция для чтения данных и генерации Java кода
    public static void generateScript(String inputPath, String outputPath, Context context) throws IOException {
        Log.d("REWRITER / GenerateScript", "Starting generating. Input path: [" + inputPath + "]");
        Log.d("REWRITER / GenerateScript", "Starting generating. Output path: [" + outputPath + "]");

        BufferedReader inputFile = new BufferedReader(new FileReader(inputPath));
        PrintWriter outputFile = new PrintWriter(new FileWriter(outputPath));
        Log.d("REWRITER / GenerateScript", "Starting generating. Generating input file: [" + inputFile + "]");
        Log.d("REWRITER / GenerateScript", "Starting generating. Generating output file: [" + outputFile + "]");
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
        Log.d("REWRITER / GenerateScript", "File was generated. Generated file: [" + outputPath + "]");
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