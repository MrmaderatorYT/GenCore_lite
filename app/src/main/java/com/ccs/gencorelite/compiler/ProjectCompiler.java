package com.ccs.gencorelite.compiler;

import java.io.IOException;

public class ProjectCompiler {

    public static void compileProject(String projectPath, String outputPath) {
        try {
            // Побудова команди компіляції за допомогою AAPT (Android Asset Packaging Tool)
            ProcessBuilder builder = new ProcessBuilder(
                    "aapt", "package", "-f",
                    "-M", projectPath + "/AndroidManifest.xml", // Шлях до AndroidManifest.xml
                    "-S", projectPath + "/res", // Шлях до папки ресурсів
                    "-I", "file:///resources/android.jar", // Шлях до android.jar, де X - версія Android SDK
                    "-F", outputPath + "/app.apk" // Шлях та ім'я для збереження APK
            );

            // Перенаправлення виводу компілятора в консоль
            builder.redirectErrorStream(true);

            // Запуск процесу компіляції
            Process process = builder.start();

            // Очікування завершення процесу
            int exitCode = process.waitFor();

            // Перевірка статусу завершення
            if (exitCode == 0) {
                System.out.println("Проєкт успішно скомпільовано.");
            } else {
                System.out.println("Помилка під час компіляції проєкту.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

