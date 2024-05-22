package com.ccs.gencorelite.compiler;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectCompiler {

    public static boolean compileToDex(String inputDir, String outputDirPath, String manifestPath, String resFolderPath) {
        try {
            File inputDirFile = new File(inputDir);
            if (!inputDirFile.exists() || !inputDirFile.isDirectory()) {
                System.err.println("There is no folder with java files");
                return false;
            }

            // Compile resources
            compileResources(resFolderPath, outputDirPath);

            // Compile manifest
            compileManifest(manifestPath, outputDirPath);

            // Create APK
            createApk(inputDirFile, outputDirPath);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void compileResources(String resFolderPath, String outputDirPath) throws IOException {
        File resFolder = new File(resFolderPath);
        File outputDir = new File(outputDirPath);

        if (!resFolder.exists() || !resFolder.isDirectory()) {
            System.err.println("There is no resources folder");
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = resFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    File outputFile = new File(outputDirPath + File.separator + "res" + File.separator + file.getName());
                    copyFile(file, outputFile);
                } else if (file.isDirectory()) {
                    File newDir = new File(outputDirPath + File.separator + "res" + File.separator + file.getName());
                    newDir.mkdirs();
                    compileResources(file.getPath(), newDir.getPath());
                }
            }
        }
    }

    private static void compileManifest(String manifestPath, String outputDirPath) throws IOException {
        File manifestFile = new File(manifestPath);
        File outputDir = new File(outputDirPath);

        if (!manifestFile.exists() || manifestFile.isDirectory()) {
            System.err.println("Manifest file does not exist or is a directory");
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDirPath + File.separator + "AndroidManifest.xml");
        copyFile(manifestFile, outputFile);
    }

    private static void createApk(File inputDir, String outputDirPath) throws IOException {
        String dexOutputDir = outputDirPath + File.separator + "dex";
        compileToDex(inputDir.getAbsolutePath(), dexOutputDir);

        String apkFilePath = outputDirPath + File.separator + "app.apk";
        List<File> dexFiles = getDexFiles(new File(dexOutputDir));
        packIntoApk(apkFilePath, dexFiles);
    }

    private static void compileToDex(String inputDir, String outputDirPath) {
        // Implement logic to compile Java files into DEX format
        // This method should compile all Java files in the input directory and output the DEX files to the specified output directory
    }

    private static List<File> getDexFiles(File directory) {
        // Get all DEX files from the specified directory
        // For simplicity, this method is left blank
        return new ArrayList<>();
    }

    private static void packIntoApk(String apkFilePath, List<File> dexFiles) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(apkFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File dexFile : dexFiles) {
                String entryName = "classes.dex";
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(dexFile.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        try (InputStream inputStream = new FileInputStream(source);
             OutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}
