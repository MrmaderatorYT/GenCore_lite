#include <jni.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <algorithm>

// Структура для збереження даних
struct Entry {
    std::string image;
    std::string name;
    std::string text;
    std::string music;
};

// Функція для перевірки формату файлу (зображення або музика)
int validateFileFormat(const std::string& filename) {
    const std::vector<std::string> imageFormats = {".png", ".jpg", ".jpeg", ".bmp"};
    const std::vector<std::string> musicFormats = {".wav", ".mp3", ".ogg"};

    for (const auto& format : imageFormats) {
        if (filename.find(format) != std::string::npos) {
            return 1;  // Зображення
        }
    }
    for (const auto& format : musicFormats) {
        if (filename.find(format) != std::string::npos) {
            return 2;  // Музика
        }
    }
    return 0;  // Невідомий формат
}

// Функція для парсингу скрипту
void parseScript(const std::string& scriptContent, std::vector<Entry>& entries) {
    std::istringstream scriptStream(scriptContent);
    std::string line;
    std::string currentImage, currentName, currentMusic;
    bool inBlock = false;

    while (std::getline(scriptStream, line)) {
        // Видалити зайві пробіли та символи переносу рядка
        line.erase(std::remove(line.begin(), line.end(), '\r'), line.end());
        line.erase(std::remove(line.begin(), line.end(), '\n'), line.end());
        line.erase(std::remove(line.begin(), line.end(), '\t'), line.end());
        line.erase(std::remove_if(line.begin(), line.end(), ::isspace), line.end());

        if (line.empty()) continue;

        if (line.find('{') != std::string::npos) {
            // Початок блоку
            inBlock = true;
            continue;
        } else if (line.find('}') != std::string::npos) {
            // Кінець блоку
            inBlock = false;
            currentImage.clear();
            currentName.clear();
            currentMusic.clear();
            continue;
        }

        if (inBlock) {
            // Всередині блоку - текстові рядки
            Entry entry = {currentImage, currentName, line, currentMusic};
            entries.push_back(entry);
        } else {
            // Зовні блоку - оновлення currentImage, currentName, currentMusic
            std::vector<std::string> tokens;
            std::stringstream ss(line);
            std::string token;
            while (std::getline(ss, token, ':')) {
                tokens.push_back(token);
            }

            if (tokens.size() == 3) {
                currentImage = tokens[0];
                currentName = tokens[1];
                currentMusic = tokens[2];
            } else if (tokens.size() == 2) {
                currentName = tokens[0];
                currentMusic = tokens[1];
            } else if (tokens.size() == 1) {
                // Можливо, лише зображення або інше
                // Можете додати додаткову обробку, якщо необхідно
            }
        }
    }
}

// Функція для генерації Java коду
void generateJavaCode(const std::vector<Entry>& entries, std::ofstream& output) {
    output << "package com.ccs.gencorelite;\n"
              "\n"
              "import android.annotation.SuppressLint;\n"
              "import android.content.pm.ActivityInfo;\n"
              "import android.graphics.Color;\n"
              "import android.media.MediaPlayer;\n"
              "import android.os.Bundle;\n"
              "import android.os.Handler;\n"
              "import android.util.DisplayMetrics;\n"
              "import android.util.TypedValue;\n"
              "import android.view.View;\n"
              "import android.view.ViewGroup;\n"
              "import android.view.WindowManager;\n"
              "import android.widget.Button;\n"
              "import android.widget.LinearLayout;\n"
              "import android.widget.RelativeLayout;\n"
              "import android.widget.TextView;\n"
              "\n"
              "import androidx.appcompat.app.AppCompatActivity;\n"
              "\n"
              "import java.util.ArrayList;\n"
              "import java.util.HashSet;\n"
              "\n"
              "public class Game_First_Activity extends AppCompatActivity {\n"
              "\n"
              "    private int textIndex = 0, delayBetweenCharacters = 40, // затримка між символами\n"
              "            delayBetweenTexts = 2000; // затримка між текстами\n"
              "\n"
              "    private static final int dialogContainerId = View.generateViewId();\n"
              "\n"
              "    float volumeLvl;\n"
              "    MediaPlayer mediaPlayer;\n"
              "    boolean historyBlockIsVisible = false, animationInProgress;\n"
              "    private Button history, save, load, buttonElement, buttonSecondElement;\n"
              "    private RelativeLayout bg;\n"
              "    private TextView textElement, nameElement;\n"
              "    private ArrayList<Pair> textArray = new ArrayList<>();\n"
              "    private HashSet<Integer> specialIndexes = new HashSet<>();\n"
              "\n"
              "    private static class Pair {\n"
              "        String name;\n"
              "        String text;\n"
              "        int value;\n"
              "\n"
              "        Pair(int value, String name, String text) {\n"
              "            this.name = name;\n"
              "            this.text = text;\n"
              "            this.value = value;\n"
              "        }\n"
              "    }\n"
              "\n"
              "    @SuppressLint({\"SetJavaScriptEnabled\", \"WrongViewCast\"})\n"
              "    @Override\n"
              "    protected void onCreate(Bundle savedInstanceState) {\n"
              "        super.onCreate(savedInstanceState);\n"
              "        setContentView(R.layout.activity_game_first);\n"
              "        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);\n"
              "        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);\n"
              "\n"
              "        history = findViewById(R.id.buttonHistory);\n"
              "        save = findViewById(R.id.fastSave_btn);\n"
              "        load = findViewById(R.id.fastLoad_btn);\n"
              "        bg = findViewById(R.id.bg);\n"
              "        textElement = findViewById(R.id.dialog);\n"
              "        nameElement = findViewById(R.id.name);\n"
              "        buttonElement = findViewById(R.id.first_btn);\n"
              "        buttonSecondElement = findViewById(R.id.second_btn);\n"
              "\n"
              "        initializeTextArray();\n"
              "\n"
              "        View decorView = getWindow().getDecorView();\n"
              "        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION\n"
              "                | View.SYSTEM_UI_FLAG_FULLSCREEN;\n"
              "        decorView.setSystemUiVisibility(uiOptions);\n"
              "\n"
              "        mediaPlayer = MediaPlayer.create(this, R.raw.school);\n"
              "        mediaPlayer.setLooping(true);\n"
              "        mediaPlayer.setVolume(volumeLvl, volumeLvl);\n"
              "        buttonElement.setVisibility(View.INVISIBLE);\n"
              "        buttonSecondElement.setVisibility(View.INVISIBLE);\n"
              "\n"
              "        history.setOnClickListener(new View.OnClickListener() {\n"
              "            @Override\n"
              "            public void onClick(View v) {\n"
              "                if (!historyBlockIsVisible) {\n"
              "                    showHistoryDialog();\n"
              "                } else {\n"
              "                    hideHistoryDialog();\n"
              "                }\n"
              "            }\n"
              "        });\n"
              "\n"
              "        // Start text animation\n"
              "        animateText();\n"
              "\n"
              "        load.setOnClickListener(new View.OnClickListener() {\n"
              "            @Override\n"
              "            public void onClick(View v) {\n"
              "                quickLoad();\n"
              "            }\n"
              "        });\n"
              "        save.setOnClickListener(new View.OnClickListener() {\n"
              "            @Override\n"
              "            public void onClick(View v) {\n"
              "                quickSave();\n"
              "            }\n"
              "        });\n"
              "        buttonElement.setOnClickListener(new View.OnClickListener() {\n"
              "            @Override\n"
              "            public void onClick(View v) {\n"
              "                firstBtn();\n"
              "            }\n"
              "        });\n"
              "        buttonSecondElement.setOnClickListener(new View.OnClickListener() {\n"
              "            @Override\n"
              "            public void onClick(View v) {\n"
              "                secondBtn();\n"
              "            }\n"
              "        });\n"
              "    }\n"
              "\n"
              "    @Override\n"
              "    public void onBackPressed() {\n"
              "        // Ваш код для обробки кнопки Назад\n"
              "    }\n"
              "\n"
              "    private void quickLoad() {\n"
              "        textElement.setText(\"\");\n"
              "        // Ваш код для швидкого завантаження\n"
              "    }\n"
              "\n"
              "    private void quickSave() {\n"
              "        // Ваш код для швидкого збереження\n"
              "    }\n"
              "\n"
              "    private void showHistoryDialog() {\n"
              "        // Ваш код для відображення історії\n"
              "    }\n"
              "\n"
              "    private void hideHistoryDialog() {\n"
              "        // Ваш код для приховування історії\n"
              "    }\n"
              "\n"
              "    private void animateText() {\n"
              "        if (textIndex < textArray.size()) {\n"
              "            Pair pair = textArray.get(textIndex);\n"
              "\n"
              "            // Якщо ключ не містить спеціальне значення, показати ім'я\n"
              "            if (specialIndexes.contains(textIndex)) {\n"
              "                nameElement.setText(pair.name);\n"
              "            } else {\n"
              "                nameElement.setText(\"Кейт\");\n"
              "            }\n"
              "\n"
              "            textElement.setText(\"\");\n"
              "            String textToAnimate = pair.text;\n"
              "            animationInProgress = true;\n"
              "            new Handler().postDelayed(new Runnable() {\n"
              "                int i = 0;\n"
              "\n"
              "                @Override\n"
              "                public void run() {\n"
              "                    if (i < textToAnimate.length()) {\n"
              "                        textElement.append(String.valueOf(textToAnimate.charAt(i)));\n"
              "                        i++;\n"
              "                        new Handler().postDelayed(this, delayBetweenCharacters);\n"
              "                    } else {\n"
              "                        textIndex++;\n"
              "                        animationInProgress = false;\n"
              "                        new Handler().postDelayed(new Runnable() {\n"
              "                            @Override\n"
              "                            public void run() {\n"
              "                                animateText(); // Рекурсивний виклик для наступного тексту\n"
              "                            }\n"
              "                        }, delayBetweenTexts);\n"
              "                    }\n"
              "                }\n"
              "            }, delayBetweenCharacters);\n"
              "        } else {\n"
              "            textElement.setText(\"\");\n"
              "        }\n"
              "    }\n"
              "\n"
              "    private void initializeTextArray() {\n";

    // Додати всі записи з entries
    for (size_t index = 0; index < entries.size(); ++index) {
        const Entry& entry = entries[index];
        output << "        textArray.add(new Pair(" << index << ", \"" << entry.name << "\", \"" << entry.text << "\"));\n";

        // Якщо entry має особливі параметри, можна додати їх до specialIndexes
        if (!entry.image.empty() || !entry.music.empty()) {
            output << "        specialIndexes.add(" << index << ");\n";
        }
    }

    output << "    }\n"
              "\n"
              "    private void firstBtn() {\n"
              "        // Ваш код для першої кнопки\n"
              "    }\n"
              "\n"
              "    private void secondBtn() {\n"
              "        // Ваш код для другої кнопки\n"
              "    }\n"
              "\n"
              "}\n";
}

// Основна функція для читання даних і генерації Java коду
extern "C" JNIEXPORT void JNICALL
Java_com_ccs_gencorelite_compiler_Rewriter_generateScript(JNIEnv* env, jobject /* this */, jstring inputPath, jstring outputPath) {
    // Конвертація jstring у std::string
    const char* inputPathStr = env->GetStringUTFChars(inputPath, nullptr);
    const char* outputPathStr = env->GetStringUTFChars(outputPath, nullptr);

    std::ifstream inputFile(inputPathStr);
    std::ofstream outputFile(outputPathStr);

    // Перевірка відкриття файлів
    if (!inputFile.is_open()) {
        std::cerr << "Не вдалося відкрити вхідний файл для читання.\n";
        return;
    }
    if (!outputFile.is_open()) {
        std::cerr << "Не вдалося відкрити вихідний файл для запису.\n";
        return;
    }

    std::string scriptContent((std::istreambuf_iterator<char>(inputFile)), std::istreambuf_iterator<char>());
    std::vector<Entry> entries;
    parseScript(scriptContent, entries);
    generateJavaCode(entries, outputFile);

    inputFile.close();
    outputFile.close();

    // Очищення пам'яті JNI
    env->ReleaseStringUTFChars(inputPath, inputPathStr);
    env->ReleaseStringUTFChars(outputPath, outputPathStr);

    std::cout << "Код успішно згенеровано і записано у файл.\n";
}