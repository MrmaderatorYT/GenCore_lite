// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("gencorelite");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("gencorelite")
//      }
//    }

#include <jni.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

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
void generateJavaCode(const std::string& name, const std::string& text, const std::string& image, const std::string& music, int index, std::ofstream& output) {
    output << "textArray.add(new Pair(" << index << ", \"" << name << "\", \"" << text << "\", \"" << image << "\", \"" << music << "\"));\n";
}

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

    std::string image, name, text, music;
    std::string prevImage, prevName, prevMusic;
    int index = 0;
    std::string line;

    // Читання вхідного файлу построчно
    while (std::getline(inputFile, line)) {
        // Пропуск блокових дужок
        if (line.find("{") != std::string::npos || line.find("}") != std::string::npos) {
            continue;
        }

        std::istringstream lineStream(line);
        std::string temp;

        if (std::getline(lineStream, image, ':') && std::getline(lineStream, name, ':') && std::getline(lineStream, music)) {
            // Новий блок із зображенням, іменем і музикою
            if (validateFileFormat(image) != 1 || validateFileFormat(music) != 2) {
                std::cerr << "Невірний формат файлу в рядку: " << line << "\n";
                return;
            }
            prevImage = image;
            prevName = name;
            prevMusic = music;
        } else if (std::getline(lineStream, name, ':') && std::getline(lineStream, music)) {
            // Блок з іменем і музикою
            prevName = name;
            prevMusic = music;
        } else if (std::getline(lineStream, text)) {
            // Текстовий блок
            generateJavaCode(prevName, text, prevImage, prevMusic, index++, outputFile);
        } else {
            std::cerr << "Невірний формат рядка: " << line << "\n";
        }
    }

    inputFile.close();
    outputFile.close();

    // Очищення пам'яті JNI
    env->ReleaseStringUTFChars(inputPath, inputPathStr);
    env->ReleaseStringUTFChars(outputPath, outputPathStr);

    std::cout << "Код успішно згенеровано і записано у файл.\n";
}
