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
    }
