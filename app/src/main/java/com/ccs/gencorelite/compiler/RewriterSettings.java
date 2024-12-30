package com.ccs.gencorelite.compiler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ccs.gencorelite.data.PreferenceConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RewriterSettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {

    }

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

            if (line.startsWith("SCENE SETTINGS")) {
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
        output.println("package "+package_project+";");
        output.println();
        output.println("import android.annotation.SuppressLint;");
        output.println("import android.content.SharedPreferences;");
        output.println("import android.content.pm.ActivityInfo;");
        output.println("import android.media.MediaPlayer;");
        output.println("import android.os.Bundle;");
        output.println("import android.view.View;");
        output.println("import android.view.WindowManager;");
        output.println("import android.widget.CompoundButton;");
        output.println("import android.widget.SeekBar;");
        output.println("import android.widget.Switch;");
        output.println("import android.widget.TextView;");
        output.println();
        output.println("import android.app.Activity;");
        output.println();
        output.println("import "+package_project+".data.PreferenceConfig;");
        output.println();
        output.println("public class Settings extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {");
        output.println();
        output.println("    private MediaPlayer mediaPlayer;");
        output.println("    private SeekBar seekBar;");
        output.println("    private TextView volumeTextView, typeAnim;");
        output.println("    float volumeLvl;");
        output.println("    Switch switch_anim_value;");
        output.println("    boolean type;");
        output.println();
        output.println("    @SuppressLint(\"MissingInflatedId\")");
        output.println("    @Override");
        output.println("    protected void onCreate(Bundle savedInstanceState) {");
        output.println("        super.onCreate(savedInstanceState);");
        output.println("        setContentView(R.layout.activity_settings);");
        output.println("        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);");
        output.println("        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);");
        output.println("        volumeLvl = PreferenceConfig.getVolumeLevel(this);");
        output.println("        type = PreferenceConfig.getAnimSwitchValue(this);");
        output.println("        mediaPlayer = MediaPlayer.create(this, R.raw.intro);");
        output.println("        mediaPlayer.setLooping(true);");
        output.println("        mediaPlayer.start();");
        output.println("        mediaPlayer.setVolume(volumeLvl, volumeLvl);");
        output.println("        switch_anim_value = findViewById(R.id.switch_anim);");
        output.println("        typeAnim = findViewById(R.id.type_anim_value_text);");
        output.println("        seekBar = findViewById(R.id.volume_set);");
        output.println("        volumeTextView = findViewById(R.id.volume_text);");
        output.println();
        output.println("        View decorView = getWindow().getDecorView();");
        output.println("        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;");
        output.println("        decorView.setSystemUiVisibility(uiOptions);");
        output.println();
        output.println("        if (type) {");
        output.println("            switch_anim_value.setChecked(true);");
        output.println("            volumeTextView.setText(R.string.auto);");
        output.println("        } else {");
        output.println("            switch_anim_value.setChecked(false);");
        output.println("            volumeTextView.setText(R.string.touching);");
        output.println("        }");
        output.println("    }");
        output.println();
        output.println("    private void initializeSeekBar() {");
        output.println("        seekBar.setMax(100);");
        output.println("        seekBar.setProgress((int) (volumeLvl * 100));");
        output.println("        System.out.println(\"ініціалізація\" + (int) volumeLvl);");
        output.println();
        output.println("        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {");
        output.println("            @Override");
        output.println("            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {");
        output.println("                volumeLvl = (float) progress / 100;");
        output.println("                System.out.println(\"aaaaaaaaaaaaaaaaaaaaa\" + volumeLvl);");
        output.println("                updateVolume();");
        output.println("                PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);");
        output.println("            }");
        output.println();
        output.println("            @Override");
        output.println("            public void onStartTrackingTouch(SeekBar seekBar) {");
        output.println("            }");
        output.println();
        output.println("            @Override");
        output.println("            public void onStopTrackingTouch(SeekBar seekBar) {");
        output.println("                PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);");
        output.println("            }");
        output.println("        });");
        output.println("    }");
        output.println();
        output.println("    private void updateVolume() {");
        output.println("        mediaPlayer.setVolume(volumeLvl, volumeLvl);");
        output.println("        volumeTextView.setText(\"Гучність: \" + (int) (volumeLvl * 100) + \"%\");");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    protected void onDestroy() {");
        output.println("        super.onDestroy();");
        output.println("        mediaPlayer.release();");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    protected void onPause() {");
        output.println("        super.onPause();");
        output.println("        mediaPlayer.release();");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    public void onBackPressed() {");
        output.println("        super.onBackPressed();");
        output.println("        PreferenceConfig.setVolumeLevel(getApplicationContext(), volumeLvl);");
        output.println("        finish();");
        output.println("        overridePendingTransition(0, 0);");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    protected void onStart() {");
        output.println("        super.onStart();");
        output.println("        initializeSeekBar();");
        output.println("        updateVolume();");
        output.println("        if (switch_anim_value != null) {");
        output.println("            switch_anim_value.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);");
        output.println("        }");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {");
        output.println("        volumeLvl = PreferenceConfig.getVolumeLevel(this);");
        output.println("    }");
        output.println();
        output.println("    @Override");
        output.println("    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {");
        output.println("        if (isChecked) {");
        output.println("            type = true;");
        output.println("            PreferenceConfig.setAnimSwitchValue(getApplicationContext(), type);");
        output.println("        } else {");
        output.println("            type = false;");
        output.println("            PreferenceConfig.setAnimSwitchValue(getApplicationContext(), type);");
        output.println("        }");
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