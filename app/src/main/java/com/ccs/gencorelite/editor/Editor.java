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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ccs.gencorelite.R;
import com.ccs.gencorelite.compiler.Rewriter;
import com.ccs.gencorelite.compiler.RewriterConstClasses;
import com.ccs.gencorelite.compiler.RewriterMain;
import com.ccs.gencorelite.compiler.RewriterSettings;
import com.ccs.gencorelite.data.PreferenceConfig;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Editor extends AppCompatActivity {

    private static final int REQUEST_CODE = 123;
    private static final int PICK_FILE_REQUEST_CODE = 456;
    public static final String SCRIPT_NAME = "build_script.sh";
    public static final String SCRIPTS_FOLDER_NAME = "scripts";
    public static final String MAIN_FOLDER_NAME = "GenCoreLite";
    
    // UI компоненти
    private ListView fileList;
    private EditText editor;
    private MaterialToolbar toolbar;
    private MaterialButton btnUndo, btnRedo;
    private MaterialTextView tvFileName;
    private FloatingActionButton fabCompile, fabAddFile, fabDocs;
    
    // Змінні для undo/redo функціональності
    private List<String> undoStack = new ArrayList<>();
    private List<String> redoStack = new ArrayList<>();
    private boolean isUndoRedoOperation = false;
    private static final int MAX_UNDO_STACK_SIZE = 50;
    
    private boolean isRunning = true;
    private String title, package_project;
    private String previousText = ""; // зберігаємо попередній текст
    private String currentFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        title = PreferenceConfig.getTitle(this);
        package_project = PreferenceConfig.getPackage(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Приховати панель навігації та зробити режим повноекранним
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        initViews();
        setupTextWatcher();
        setupClickListeners();
        checkPermissions();
        setupAutoSave();
        setupFileList();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        fileList = findViewById(R.id.fileList);
        editor = findViewById(R.id.editor);
        btnUndo = findViewById(R.id.btn_undo);
        btnRedo = findViewById(R.id.btn_redo);
        tvFileName = findViewById(R.id.tv_file_name);
        fabCompile = findViewById(R.id.fab_compile);
        fabAddFile = findViewById(R.id.fab_add_file);
        fabDocs = findViewById(R.id.fab_docs);
        
        SyntaxHighlighter.applySyntaxHighlighting(editor);
        updateUndoRedoButtons();
    }

    private void setupTextWatcher() {
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Зберігаємо поточний стан для undo, якщо це не undo/redo операція
                if (!isUndoRedoOperation && s.length() > 0) {
                    addToUndoStack(s.toString());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Очищаємо redo stack при новому введенні тексту
                if (!isUndoRedoOperation) {
                    redoStack.clear();
                    updateUndoRedoButtons();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateUndoRedoButtons();
            }
        });
    }

    private void setupClickListeners() {
        btnUndo.setOnClickListener(v -> performUndo());
        btnRedo.setOnClickListener(v -> performRedo());
        
        fabDocs.setOnClickListener(v -> {
            Intent intent = new Intent(Editor.this, DocsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        
        fabCompile.setOnClickListener(v -> performCompile());
        
        fabAddFile.setOnClickListener(v -> {
            Intent intent = new Intent(Editor.this, FilePicker.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void addToUndoStack(String text) {
        undoStack.add(text);
        // Обмежуємо розмір стеку
        if (undoStack.size() > MAX_UNDO_STACK_SIZE) {
            undoStack.remove(0);
        }
        updateUndoRedoButtons();
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            String currentText = editor.getText().toString();
            redoStack.add(currentText);
            
            String previousText = undoStack.remove(undoStack.size() - 1);
            
            isUndoRedoOperation = true;
            editor.setText(previousText);
            editor.setSelection(previousText.length());
            isUndoRedoOperation = false;
            
            updateUndoRedoButtons();
            Toast.makeText(this, "Скасовано", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            String currentText = editor.getText().toString();
            undoStack.add(currentText);
            
            String nextText = redoStack.remove(redoStack.size() - 1);
            
            isUndoRedoOperation = true;
            editor.setText(nextText);
            editor.setSelection(nextText.length());
            isUndoRedoOperation = false;
            
            updateUndoRedoButtons();
            Toast.makeText(this, "Повернуто", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUndoRedoButtons() {
        btnUndo.setEnabled(!undoStack.isEmpty());
        btnRedo.setEnabled(!redoStack.isEmpty());
        
        // Оновлюємо прозорість кнопок
        btnUndo.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        btnRedo.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void setupAutoSave() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!currentFileName.isEmpty()) {
                    saveData(title, currentFileName, editor.getText().toString());
                }
            }
        };
        timer.schedule(task, 1, 10000); // Автозбереження кожні 10 секунд
    }

    private void setupFileList() {
        String[] files = {"messages.gc_l", "colors.gc_l", "main_screen.gc_l", "settings_screen.gc_l"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        fileList.setAdapter(adapter);
        registerForContextMenu(fileList);

        fileList.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = fileList.getItemAtPosition(position).toString();
            loadFile(fileName);
        });
    }

    private void loadFile(String fileName) {
        // Очищаємо стеки при завантаженні нового файлу
        undoStack.clear();
        redoStack.clear();
        
        currentFileName = fileName;
        tvFileName.setText(fileName);
        
        if (fileName.contains("messages.gc_l")) {
            readFile(title, "messages.gc_l");
        } else if (fileName.contains("colors.gc_l")) {
            readFile(title, "colors.gc_l");
        } else if (fileName.contains("main_screen.gc_l")) {
            readFile(title, "main_screen.gc_l");
        } else if (fileName.contains("settings_screen.gc_l")) {
            readFile(title, "settings_screen.gc_l");
        }
        
        updateUndoRedoButtons();
    }

    private void performCompile() {
        // ... existing code ...
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        File documentsDirectory = new File("/storage/emulated/0/Documents/GenCoreLite/scripts");
        if (!documentsDirectory.exists()) {
            documentsDirectory.mkdirs();
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

        try {
            inputStream = assetManager.open("build_script.sh");
            copyFileFromAssets(Editor.this, "build_script.sh", destinationFile);
            copyFileFromAssets(Editor.this, "test.sh", destinationFile1);
            copyFileFromAssets(Editor.this, "android.jar", androidJar);
            copyFileFromAssets(Editor.this, "zipalign", zipalign);
            copyFileFromAssets(Editor.this, "apksigner", apksigner);
            copyFileFromAssets(Editor.this, "apksigner.jar", apksigner_jar);
            
            copyAssets(Editor.this, "project/res", "/storage/emulated/0/Documents/GenCoreLite/scripts/res");
            copyAssets(Editor.this, "test", "/storage/emulated/0/Documents/GenCoreLite/scripts");
            launchTermuxScript();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        rewriteData(getFilesDir() + "/Projects/" + title + "/" + title + "/messages.gc_l");
        rewriteDataInMain(getFilesDir() + "/Projects/" + title + "/" + title + "/main_screen.gc_l");
        rewriteDataInSettings(getFilesDir() + "/Projects/" + title + "/" + title + "/settings_screen.gc_l");
        rewriteConstFiles();

        PackageManager pm = Editor.this.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("com.termux");

        if (intent != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();

                String fileContent = stringBuilder.toString();
                Log.d("FileContent", fileContent);

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
            Toast.makeText(Editor.this, "Termux не знайдено на пристрої", Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(this, "Rename file: " + fileName, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String readFile(String folderName, String fileName) {
        StringBuilder sb = new StringBuilder();
        File file = new File(getFilesDir(), "Projects/" + folderName + "/" + folderName + "/" + fileName);
        Log.d("EDITOR / ReadFile", file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            Log.i("EDITOR / ReadFile", "Text of file [" + fileName + "]: " + sb);
            
            // Очищаємо стеки при завантаженні файлу
            undoStack.clear();
            redoStack.clear();
            
            isUndoRedoOperation = true;
            editor.setText(sb.toString());
            isUndoRedoOperation = false;
            
            updateUndoRedoButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void saveData(String title, String fileName, String data) {
        if (!data.equals(previousText)) {
            File folder = new File(getFilesDir(), "Projects/" + title + "/" + title);
            if (!folder.exists()) {
                folder.mkdirs(); // Створення папки, якщо вона не існує
            }

            File file = new File(folder, fileName);
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                osw.write(data);

                runOnUiThread(() -> {
                    Toast.makeText(Editor.this, "Дані збережено успішно", Toast.LENGTH_SHORT).show();
                    Log.d("EDITOR / SaveData", "Data was saved successfully");
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(Editor.this, "Помилка збереження даних", Toast.LENGTH_SHORT).show();
                    Log.d("EDITOR / SaveData", "Trouble with saving data");
                });
            }
            previousText = data; // Зберігаємо новий текст як попередній
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("EDITOR / OnRequestPermissionsResult", "Permission granted");
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                Log.d("EDITOR / OnRequestPermissionsResult", "Permission denied");
            }
        }
    }

    public static void copyFileFromAssets(Context context, String assetFilePath, File destinationFile) {
        try (InputStream inputStream = context.getAssets().open(assetFilePath);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
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
        String packagePath = package_project.replace('.', '/');
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/Game_First_Activity.java";
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            new Rewriter.FileOperationTask(inputFilePath, outputPath, getApplicationContext()).execute();
        });
    }
    
    private void rewriteDataInMain(String inputFilePath) {
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + package_project.replace('.', '/') + "/"+"MainActivity.java";
        Log.d("EDITOR / RewriteDataInMain", "Output path: ["+outputPath+"]");
        Log.d("EDITOR / RewriteDataInMain", "Input path: ["+inputFilePath+"]");
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + package_project.replace('.', '/') + "/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        new RewriterMain.FileOperationTask(inputFilePath, outputPath, getApplicationContext()).execute();
    }

    private void rewriteDataInSettings(String inputFilePath) {
        String packagePath = package_project.replace('.', '/');
        String outputPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/Settings.java";
        Log.d("EDITOR / RewriteDataInSettings", "Input path: ["+inputFilePath+"]");
        File outputDir = new File("/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/");
        Log.d("EDITOR / RewriteDataInSettings", "Output path: ["+outputPath+"]");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        new RewriterSettings.FileOperationTask(inputFilePath, outputPath, getApplicationContext()).execute();
    }
    
    private void rewriteConstFiles() {
        String packagePath = package_project.replace('.', '/');
        String manifestPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/AndroidManifest.xml";
        String dataPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/data";
        String systemPath = "/storage/emulated/0/Documents/GenCoreLite/scripts/java/" + packagePath + "/system";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            createDirectoryIfNotExists(dataPath);
            createDirectoryIfNotExists(systemPath);

            File manifestFile = new File(manifestPath);
            if (manifestFile.exists() && !manifestFile.delete()) {
                Log.e("EDITOR / RewriteDataInManifest", "Failed to delete old AndroidManifest.xml.");
            }

            new RewriterConstClasses.FileOperationTask(manifestPath, getApplicationContext()).execute();

            RewriterConstClasses.rewriteExitConfirmationDialog(systemPath + "/ExitConfirmationDialog.java", package_project);
            RewriterConstClasses.rewriteWebAppInterface(dataPath + "/WebAppInterface.java", package_project);
            RewriterConstClasses.rewriteFileManager(dataPath + "/FileManager.java", package_project);
            RewriterConstClasses.rewritePreferenceConfig(dataPath + "/PreferenceConfig.java", package_project);

            Log.d("EDITOR / RewriteDataInManifest", "All files rewritten successfully.");
        });
    }

    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("EDITOR / CreateDirectory", "Failed to create directory: " + path);
        }
    }
}