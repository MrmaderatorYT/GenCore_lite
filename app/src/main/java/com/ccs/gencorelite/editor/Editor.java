package com.ccs.gencorelite.editor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ccs.gencorelite.R;
import com.ccs.gencorelite.compiler.Rewriter;
import com.ccs.gencorelite.data.PreferenceConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class Editor extends AppCompatActivity {

    private static final int REQUEST_CODE = 123;
    private static final int PICK_FILE_REQUEST_CODE = 456;
    public static final String SCRIPT_NAME = "build_script.sh";
    public static final String SCRIPTS_FOLDER_NAME = "scripts";
    public static final String MAIN_FOLDER_NAME = "GenCoreLite";
    private ListView fileList;

    private EditText editor;
    private ImageView compile, add_file;
    private boolean isRunning = true;
    private String title;
    private String previousText = ""; // зберігаємо попередній текст

    //private final Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        title = PreferenceConfig.getTitle(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Приховати панель навігації та зробити режим повноекранним
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        fileList = findViewById(R.id.fileList);
        editor = findViewById(R.id.editor);
        compile = findViewById(R.id.compile);
        add_file = findViewById(R.id.add_new_file);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Викликати ваш метод кожні 10 секунд
                saveData(title, "messages.gc_l", editor.getText().toString());
                saveData(title, "colors.gc_l", editor.getText().toString());
                saveData(title, "main_screen.gc_l", editor.getText().toString());
                saveData(title, "settings_screen.gc_l", editor.getText().toString());
                Log.i("EDITOR / SaveData (calling in onCreate", "Text of saved file: [messages.gc_l]: "+editor.getText().toString());
                Log.i("EDITOR / SaveData (calling in onCreate", "Text of saved file: [colors.gc_l]: "+editor.getText().toString());
            }
        };

        // Запускати завдання кожні 10 секунд (10000 мілісекунд)
        timer.schedule(task, 1, 10000);

        compile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AssetManager assetManager = getAssets();
                InputStream inputStream = null;
                File documentsDirectory = new File("/storage/emulated/0/Documents/GenCoreLite/scripts");
                if (!documentsDirectory.exists()) {
                    documentsDirectory.mkdirs(); // Створюємо директорію, якщо вона не існує
                }
                File destinationFile = new File(documentsDirectory, "build_script.sh");
                File build = new File(documentsDirectory, "/test/build.sh");
                File apksigner = new File(documentsDirectory, "apksigner");
                File apksigner_jar = new File(documentsDirectory, "apksigner.jar");
                File zipalign = new File(documentsDirectory, "zipalign");
                File destinationFile1 = new File(documentsDirectory, "test.sh");
                File androidJar = new File(documentsDirectory, "android.jar");


                final boolean b = destinationFile.setExecutable(true);
                final boolean a = destinationFile1.setExecutable(true);
                final boolean c = zipalign.setExecutable(true);
                final boolean d = apksigner.setExecutable(true);
                final boolean f = apksigner_jar.setExecutable(true);
                final boolean buildScript = apksigner.setExecutable(true);

                // Launch Termux with the script
                try {
                    inputStream = assetManager.open("build_script.sh");
                    copyFileFromAssets(Editor.this, "build_script.sh", destinationFile);
                    copyFileFromAssets(Editor.this, "test.sh", destinationFile1);
                    copyFileFromAssets(Editor.this, "android.jar", androidJar);
                    copyFileFromAssets(Editor.this, "zipalign", zipalign);
                    copyFileFromAssets(Editor.this, "apksigner", apksigner);
                    copyFileFromAssets(Editor.this, "apksigner.jar", apksigner_jar);
                    File destinationFolder = new File(getFilesDir(), "/storage/emulated/0/Documents/GenCoreLite/scripts");
                    copyAssets(Editor.this, "project", "/storage/emulated/0/Documents/GenCoreLite/scripts");
                    copyAssets(Editor.this, "test", "/storage/emulated/0/Documents/GenCoreLite/scripts");
                    launchTermuxScript();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String sourceFilePath = "file:///android_asset/build_script.sh";
                rewriteData(getFilesDir() + "/Projects/" + title + "/" + title + "/messages.gc_l");
                rewriteDataInMain(getFilesDir() + "/Projects/" + title + "/" + title + "/main_screen.gc_l");
                rewriteDataInSettings(getFilesDir() + "/Projects/" + title + "/" + title + "/settings_screen.gc_l");

                PackageManager pm = Editor.this.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage("com.termux");

                if (intent != null) {

                    try {
                        // Відкриття файлу з папки assets

                        // Читання даних з файлу
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        reader.close();

                        // Отримання зчитаного вмісту файлу у вигляді рядка
                        String fileContent = stringBuilder.toString();

                        // Тепер ви можете використати fileContent за потребою

                        // Наприклад, вивести його в лог
                        Log.d("FileContent", fileContent);

                        // Відправка команди у термінал Termux
                        try {
                            Process process = Runtime.getRuntime().exec("sh");
                            OutputStream outputStream = process.getOutputStream();
                            outputStream.write(fileContent.getBytes());
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Якщо Termux не знайдено на пристрої
                    Toast.makeText(Editor.this, "Termux не знайдено на пристрої", Toast.LENGTH_SHORT).show();
                }
//                try {
//                    copyScriptToStorage();
//                    runTermuxCommand();
//                } catch (Exception e) {
//                    Log.e("TermuxCommand", "Error copying or running Termux command", e);
//                }
            }
        });

        // Масив з назвами файлів (припустимо, що це ваш список файлів)
        String[] files = {"messages.gc_l", "colors.gc_l", "main_screen.gc_l", "settings_screen.gc_l"};

        // Адаптер для списку файлів
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, files);
        fileList.setAdapter(adapter);

        // Реєстрація ListView для контекстного меню
        registerForContextMenu(fileList);

        // Обробник подій для вибору файлу зі списку
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Отримуємо назву вибраного файлу
                String fileName = fileList.getItemAtPosition(position).toString();
                // Можна використовувати це ім'я файлу для завантаження тексту файлу та відображення його в редакторі
                // З цієї точки ви можете реалізувати завантаження тексту файлу та відображення його в полі редактора
                if (fileName.contains("messages.gc_l")) {
                    readFile(title, "messages.gc_l");
                } else if (fileName.contains("colors.gc_l")) {
                    readFile(title, "colors.gc_l");
                }else if(fileName.contains("main_screen.gc_l")){
                    readFile(title, "main_screen.gc_l");
                }else if(fileName.contains("settings_screen.gc_l")){
                    readFile(title, "settings_screen");
                }
            }
        });

        add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openFilePicker();
                Intent intent = new Intent(Editor.this, FilePicker.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }


    // Метод для створення контекстного меню
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.fileList) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }

    // Метод для обробки вибору елементів контекстного меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String fileName = fileList.getItemAtPosition(info.position).toString();
        switch (item.getItemId()) {
            case R.id.rename_option:
                // Додайте код для перейменування файлу тут
                Toast.makeText(this, "Rename file: " + fileName, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String readFile(String folderName, String fileName) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(getFilesDir(), "Projects/" + folderName + "/" + folderName + "/" + fileName);
            Log.d("EDITOR / ReadFile", file.getAbsolutePath());
            fis = new FileInputStream(file);
            // Вказуємо кодування UTF-8 при читанні файлу
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            System.out.println("Text of file: " + sb);
            Log.i("EDITOR / ReadFile", "Text of file ["+fileName+"]" + sb);
            editor.setText(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private void saveData(String title, String fileName, String data) {
        Log.d("EDITOR / SaveData", "title: [ "+title + " ]");
        // перевіряємо, чи змінився текст


        if (!data.equals(previousText)) {
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            try {
                // Отримання шляху до папки проєкту
                File folder = new File(getFilesDir(), "Projects/" + title + "/" + title);
                if (!folder.exists()) {
                    folder.mkdirs(); // Створення папки, якщо вона не існує
                    Log.d("EDITOR / SaveData", "Folder [Projects] was created");
                }

                File file = new File(folder, fileName);
                Log.d("EDITOR / SaveData", file.getAbsolutePath());
                fos = new FileOutputStream(file);
                // Вказуємо кодування UTF-8 при записі у файл
                osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                osw.write(data);

                // Показати Toast на головному потоці
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Editor.this, "Data was saved successfully", Toast.LENGTH_SHORT).show();
                        Log.d("EDITOR / SaveData", "Data was saved successfully");

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();

                // Показати Toast на головному потоці
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Editor.this, "Trouble with saving data", Toast.LENGTH_SHORT).show();
                        Log.d("EDITOR / SaveData", "Trouble with saving data");

                    }
                });
            } finally {
                if (osw != null) {
                    try {
                        osw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            previousText = data; // зберігаємо новий текст як попередній
        }
    }


    private void executeTermuxCommand() {
        try {
            Intent intent = new Intent("com.termux.RUN_COMMAND");
            intent.setPackage("com.termux");
            intent.putExtra("com.termux.RUN_COMMAND_PATH", "/storage/emulated/0/Documents/GenCoreLite/scripts/build_script.sh");
            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TermuxCommand", "Error running Termux command", e);
            Toast.makeText(this, "Error running Termux command: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void launchTermuxScript() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent = pm.getLaunchIntentForPackage("com.termux");
        startActivity(intent);
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "bash /storage/emulated/0/Documents/GenCoreLite/scripts/build_script.sh");
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-n", "5"});
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false);
        intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0");
        startService(intent);


//        if (intent != null) {
//            // Add the command to execute in Termux
//            intent.putExtra("com.termux.RUN_COMMAND", "bash " + scriptPath);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            // Launch Termux
//            startActivity(intent);
//        } else {
//            // Handle the error
//            // Termux is not installed
//        }
    }


    private void runTermuxCommand() {
        try {
            // Шлях до скопійованого скрипта
            File scriptFile = new File(getExternalFilesDir(null), SCRIPT_NAME);

            // Додати права на виконання скрипта
            scriptFile.setExecutable(true);

            // Параметри для скрипта
            String appName = "MyApp";
            String packageName = "com.example.myapp";
            String mainActivity = "package org.example.myapp;\n" +
                    "public class MainActivity extended AppCompatActivity{\n" +
                    "@Override\n" +
                    "protected void onCreate(Bundle savedInstanceState) {\n" +
                    "super.onCreate(savedInstanceState);\n" +
                    "setContentView(R.layout.activity_editor);\n" +
                    "}\n" +
                    "}";
            String sourceDir = "src";
            String buildDir = "build";
            String outputDir = "/storage/emulated/0/Download/GenCoreLite";


            // Команда для виконання скрипта з параметрами
            String command = String.format(
                    "sh %s %s %s %s %s %s %s",
                    scriptFile.getAbsolutePath(), appName, packageName, mainActivity, sourceDir, buildDir, outputDir
            );

            // Виконати команду за допомогою ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Команда виконана успішно
                String successMessage = "Command executed successfully:\n" + output.toString();
                Log.d("CommandExecution", successMessage);
                editor.setText(successMessage);
            } else {
                // Команда завершилася з помилкою
                String errorMessage = "Command execution failed with exit code " + exitCode + ":\n" + output.toString();
                Log.e("CommandExecution", errorMessage);
                editor.setText(errorMessage);
            }

        } catch (Exception e) {
            Log.e("CommandExecution", "Error running command", e);
            Toast.makeText(this, "Error running command: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("EDITOR / OnRequestPermissionsResult", "Permission granted");
                runTermuxCommand();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                Log.d("EDITOR / OnRequestPermissionsResult", "Permission denied");
            }
        }
    }

    //копіювання файлів з папки assets в інше місце (в нашому коді - в папку Documents)
    public static void copyFileFromAssets(Context context, String assetFilePath, File destinationFile) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(assetFilePath);
            OutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            Log.d("EDITOR / CopyFileFromAssets", "File copied from assets to: " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("EDITOR / CopyFileFromAssets", "Error copying file from assets: " + assetFilePath, e);
        }
    }

    public static void copyAssets(Context context, String assetDir, String targetDir) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list(assetDir);
        if (files == null) return;

        File targetDirectory = new File(targetDir);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();  // Створити директорію, якщо вона ще не існує
        }

        for (String file : files) {
            InputStream in;
            OutputStream out;

            // Перевіряємо, чи це директорія, чи файл
            if (assetManager.list(assetDir + "/" + file).length == 0) {
                // Це файл, копіюємо його
                in = assetManager.open(assetDir + "/" + file);
                File outFile = new File(targetDir, file);
                out = new FileOutputStream(outFile);

                // Копіюємо байти файлу
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                in.close();
                out.flush();
                out.close();
            } else {
                // Це директорія, рекурсивно копіюємо файли
                copyAssets(context, assetDir + "/" + file, targetDir + "/" + file);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
// Зберігаємо файл у відповідну папку
                saveFileToFolder(uri);
            }
        }
    }

    private void saveFileToFolder(Uri uri) {
        String fileName = getFileName(uri);
        String destinationFolderPath = getDestinationFolderPath(uri);

        File destinationFolder = new File(destinationFolderPath);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs(); // Створюємо папку, якщо вона не існує
        }

        File destinationFile = new File(destinationFolder, fileName);
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                Toast.makeText(this, "Failed to open file", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            Toast.makeText(this, "File saved to " + destinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String fileName = "unknown";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }

    private String getDestinationFolderPath(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "/storage/emulated/0/Documents/GenCoreLite/" + title + "/images";
            } else if (mimeType.startsWith("audio/")) {
                return "/storage/emulated/0/Documents/GenCoreLite/" + title + "/raw";
            }
        }
        return "/storage/emulated/0/Download/GenCoreLite"; // Папка за замовчуванням
    }

    private void rewriteData(String inputFilePath) {
        // Шляхи до вхідного та вихідного файлів
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/Game_First_Activity.java"; // Шлях до вихідного файлу
        Log.d("EDITOR / RewriteData", "Output path: ["+outputPath+"]");
        Log.d("EDITOR / RewriteData", "Input path: ["+inputFilePath+"]");
        // Переконайтеся, що директорія вихідного файлу існує
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Виклик AsyncTask для виконання операцій у фоновому потоці
        new Rewriter.FileOperationTask(inputFilePath, outputPath).execute();
    }private void rewriteDataInMain(String inputFilePath) {
        // Шляхи до вхідного та вихідного файлів
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/MainActivity.java"; // Шлях до вихідного файлу
        Log.d("EDITOR / RewriteData", "Output path: ["+outputPath+"]");
        Log.d("EDITOR / RewriteData", "Input path: ["+inputFilePath+"]");
        // Переконайтеся, що директорія вихідного файлу існує
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Виклик AsyncTask для виконання операцій у фоновому потоці
        new Rewriter.FileOperationTask(inputFilePath, outputPath).execute();
    }private void rewriteDataInSettings(String inputFilePath) {
        // Шляхи до вхідного та вихідного файлів
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/Settings.java"; // Шлях до вихідного файлу
        Log.d("EDITOR / RewriteData", "Output path: ["+outputPath+"]");
        Log.d("EDITOR / RewriteData", "Input path: ["+inputFilePath+"]");
        // Переконайтеся, що директорія вихідного файлу існує
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/com/ccs/romanticadventure/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Виклик AsyncTask для виконання операцій у фоновому потоці
        new Rewriter.FileOperationTask(inputFilePath, outputPath).execute();
    }
}