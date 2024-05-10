package com.ccs.gencorelite.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectCompiler {

    public static boolean compileToDex(String inputDir, String outputDirPath) {
        try {
            File inputDirFile = new File(inputDir);
            if (!inputDirFile.exists() || !inputDirFile.isDirectory()) {
                System.err.println("There is no folder with java files");
                return false;
            }

            // Шлях до вихідної директорії для збереження скомпільованого коду
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs(); // Створення директорії, якщо вона не існує
            }

            // Компіляція Java файлів

            // Створюємо файл APK як ZIP архів
            String apkFilePath = outputDirPath + File.separator + "app.apk";
            try (FileOutputStream fos = new FileOutputStream(apkFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Рекурсивно додаємо файли з вихідної директорії до ZIP архіву
                addFilesToZip(inputDirFile, inputDirFile.getPath(), zos);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addFilesToZip(File sourceDir, String rootPath, ZipOutputStream zos) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFilesToZip(file, rootPath, zos);
                } else {
                    String relativePath = file.getPath().substring(rootPath.length() + 1);
                    zos.putNextEntry(new ZipEntry(relativePath));
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }
    }

}
