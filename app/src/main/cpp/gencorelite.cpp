#include <jni.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

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

// Функція для генерації Java коду
void generateJavaCode(const Entry& entry, int index, std::ofstream& output) {
    output << "public class Game_First_Activity extends MainActivity {\n"
              "\n"
              "    private WebView webView;\n"
              "    private int katya, choose, textIndex = 0, value, indexArray, delayBetweenCharacters = 40, //затримка між спавном символів\n"
              "            delayBetweenTexts = 2000; // затримка між спавнінгом іншого тексту з масиву;\n"
              "\n"
              "    private static final int dialogContainerId = View.generateViewId(); // Генерируем уникальный идентификатор для контейнера\n"
              "\n"
              "    float volumeLvl;\n"
              "    MediaPlayer mediaPlayer;\n"
              "    boolean type, historyBlockIsVisible = false, animationInProgress;\n"
              "    private Button history, save, load, buttonElement, buttonSecondElement;\n"
              "    private RelativeLayout bg;\n"
              "    private TextView textElement, nameElement;\n"
              "    private ArrayList<Pair> textArray = new ArrayList<>();\n"
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
              "\n"
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
              "        choose = PreferenceConfig.getChoose(this);\n"
              "        history = findViewById(R.id.buttonHistory);\n"
              "        save = findViewById(R.id.fastSave_btn);\n"
              "        load = findViewById(R.id.fastLoad_btn);\n"
              "        bg = findViewById(R.id.bg);\n"
              "        textElement = findViewById(R.id.dialog);\n"
              "        nameElement = findViewById(R.id.name);\n"
              "        type = PreferenceConfig.getAnimSwitchValue(this);\n"
              "        buttonElement = findViewById(R.id.first_btn);\n"
              "        buttonSecondElement = findViewById(R.id.second_btn);\n"
              "        value = PreferenceConfig.getValue(this);\n"
              "\n"
              "        initializeTextArray();\n"
              "\n"
              "        View decorView = getWindow().getDecorView();\n"
              "        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION\n"
              "                | View.SYSTEM_UI_FLAG_FULLSCREEN;\n"
              "        decorView.setSystemUiVisibility(uiOptions);\n"
              "        volumeLvl = PreferenceConfig.getVolumeLevel(this);\n"
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
              "        ExitConfirmationDialog.showExitConfirmationDialog(this);\n"
              "    }\n"
              "\n"
              "    private void quickLoad() {\n"
              "        textElement.setText(\"\");\n"
              "        textIndex = value;\n"
              "    }\n"
              "\n"
              "    private void quickSave() {\n"
              "        // Реализация быстрого сохранения\n"
              "        PreferenceConfig.setValue(getApplicationContext(), textIndex);\n"
              "    }\n"
              "\n"
              "    private void showHistoryDialog() {\n"
              "        historyBlockIsVisible = true;\n"
              "        // Створення діалогового контейнера\n"
              "        LinearLayout dialogContainer = new LinearLayout(this);\n"
              "        dialogContainer.setId(dialogContainerId);\n"
              "        dialogContainer.setLayoutParams(new ViewGroup.LayoutParams(\n"
              "                convertDpToPx(300), // Ширина контейнера\n"
              "                convertDpToPx(200) // Висота контейнера\n"
              "        ));\n"
              "        dialogContainer.setOrientation(LinearLayout.VERTICAL);\n"
              "        dialogContainer.setBackgroundColor(Color.WHITE); // Білий колір фону\n"
              "        dialogContainer.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Відступи всередині контейнера\n"
              "        dialogContainer.setBackgroundResource(R.drawable.pink_bg); // Границя контейнера\n"
              "        dialogContainer.setX(getScreenWidth() / 2f - convertDpToPx(150)); // Положення по горизонталі\n"
              "        dialogContainer.setY(getScreenHeight() / 2f - convertDpToPx(100)); // Положення по вертикалі\n"
              "\n"
              "        // Створення елемента для відображення тексту\n"
              "        LinearLayout textContainer = new LinearLayout(this);\n"
              "        textContainer.setLayoutParams(new LinearLayout.LayoutParams(\n"
              "                ViewGroup.LayoutParams.MATCH_PARENT,\n"
              "                ViewGroup.LayoutParams.MATCH_PARENT\n"
              "        ));\n"
              "        textContainer.setOrientation(LinearLayout.VERTICAL);\n"
              "        textContainer.setScrollbarFadingEnabled(false);\n"
              "        textContainer.setVerticalScrollBarEnabled(true);\n"
              "        textContainer.setHorizontalScrollBarEnabled(false);\n"
              "\n"
              "        // Додавання кожного ключа та значення з HashMap до текстового елемента\n"
              "        for (int i = 0; i < textIndex; i++) {\n"
              "            Pair pair = textArray.get(i);\n"
              "\n"
              "            TextView textView = new TextView(this);\n"
              "            textView.setLayoutParams(new LinearLayout.LayoutParams(\n"
              "                    ViewGroup.LayoutParams.MATCH_PARENT,\n"
              "                    ViewGroup.LayoutParams.WRAP_CONTENT\n"
              "            ));\n"
              "            textView.setText(pair.name + \": \" + pair.text);\n"
              "            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);\n"
              "            textView.setLineSpacing(0, 1.5f);\n"
              "            textContainer.addView(textView);\n"
              "            Log.d(\"TextIndex\", String.valueOf(textIndex));\n"
              "        }\n"
              "\n"
              "        // Додавання текстового елемента до діалогового контейнера\n"
              "        dialogContainer.addView(textContainer);\n"
              "\n"
              "        // Додавання діалогового контейнера до кореневої розмітки активності\n"
              "        ((ViewGroup) getWindow().getDecorView().getRootView()).addView(dialogContainer);\n"
              "    }\n"
              "\n"
              "    // Метод для преобразования dp в px\n"
              "    private int convertDpToPx(int dp) {\n"
              "        float scale = getResources().getDisplayMetrics().density;\n"
              "        return (int) (dp * scale + 0.5f);\n"
              "    }\n"
              "\n"
              "    // Метод для получения ширины экрана\n"
              "    private int getScreenWidth() {\n"
              "        DisplayMetrics displayMetrics = new DisplayMetrics();\n"
              "        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);\n"
              "        return displayMetrics.widthPixels;\n"
              "    }\n"
              "\n"
              "    // Метод для получения высоты экрана\n"
              "    private int getScreenHeight() {\n"
              "        DisplayMetrics displayMetrics = new DisplayMetrics();\n"
              "        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);\n"
              "        return displayMetrics.heightPixels;\n"
              "    }\n"
              "\n"
              "    private void hideHistoryDialog() {\n"
              "        historyBlockIsVisible = false;\n"
              "        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();\n"
              "        View dialogContainer = rootView.findViewById(dialogContainerId);\n"
              "        if (dialogContainer != null) {\n"
              "            rootView.removeView(dialogContainer);\n"
              "        }\n"
              "    }\n"
              "\n"
              "    private void animateText() {\n"
              "        if (textIndex < textArray.size()) {\n"
              "            Pair pair = textArray.get(textIndex);\n"
              "\n"
              "            // Якщо ключ не містить спешл значення, показати ім'я\n"
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
              "                                animateText(); // Call the method recursively to show the next text\n"
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
              "    private void initializeTextArray() {\n"
              "\n"
              "textArray.add(new Pair(" << index << ", \"" << entry.name << "\", \"" << entry.text << "\", \""
           << entry.image << "\", \"" << entry.music << "\"));\n"
              "\n"
              "        \n"
              "\n"
              "        // Додати інші пари за необхідності\n"
              "    }\n"
              "    \n"
              "}";

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

    std::vector<Entry> entries;
    std::string image, name, text, music;
    std::string prevImage, prevName, prevMusic;
    int index = 0;
    std::string line;

    // Читання вхідного файлу построчно
    while (std::getline(inputFile, line)) {
        if (line.find("{") != std::string::npos || line.find("}") != std::string::npos) {
            continue; // Пропуск дужок
        }

        std::istringstream lineStream(line);
        std::string temp;

        // Блок зображення, імені, музики
        if (std::getline(lineStream, image, ':') && std::getline(lineStream, name, ':') && std::getline(lineStream, music)) {
            if (validateFileFormat(image) != 1 || validateFileFormat(music) != 2) {
                std::cerr << "Невірний формат файлу в рядку: " << line << "\n";
                return;
            }
            prevImage = image;
            prevName = name;
            prevMusic = music;
        }
            // Блок імені, музики
        else if (std::getline(lineStream, name, ':') && std::getline(lineStream, music)) {
            prevName = name;
            prevMusic = music;
        }
            // Текстовий блок
        else if (std::getline(lineStream, text)) {
            Entry entry = {prevImage, prevName, text, prevMusic};
            entries.push_back(entry);
        }
        else {
            std::cerr << "Невірний формат рядка: " << line << "\n";
        }
    }

    // Генерація Java коду
    for (const auto& entry : entries) {
        generateJavaCode(entry, index++, outputFile);
    }

    inputFile.close();
    outputFile.close();

    // Очищення пам'яті JNI
    env->ReleaseStringUTFChars(inputPath, inputPathStr);
    env->ReleaseStringUTFChars(outputPath, outputPathStr);

    std::cout << "Код успішно згенеровано і записано у файл.\n";
}
