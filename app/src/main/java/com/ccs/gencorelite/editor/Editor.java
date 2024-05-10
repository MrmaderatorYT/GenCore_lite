package com.ccs.gencorelite.editor;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;

import com.ccs.gencorelite.R;
import com.ccs.gencorelite.compiler.ProjectCompiler;
import com.ccs.gencorelite.data.PreferenceConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class Editor extends AppCompatActivity {
    private ListView fileList;
    private EditText editor;
    private ImageView compile;
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


        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Викликати ваш метод кожні 10 секунд
                saveData(title,"array.txt", editor.getText().toString());
                System.out.println("Text of saved file: "+ editor.getText().toString());
            }
        };

        // Запускати завдання кожні 10 секунд (10000 мілісекунд)
        timer.schedule(task, 1, 10000);


        compile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    compileApp();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Масив з назвами файлів (припустимо, що це ваш список файлів)
        String[] files = {"array.txt"};

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
                if (fileName.contains("array.txt")) {
                    readFile(title, "array.txt");
//                } else if (fileName.equals("AndroidManifest.xml")) {
//                    printFileContentToLog(Editor.this, "file:///android_assets/manifest/" + fileName);
//                } else if (fileName.contains(".xml")&&fileName.contains("activity")) {
//                    readFile("app/layout/" + fileName);
                }
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

    // Метод для збереження даних в реальному часі


    private void compileApp() throws IOException {
        Context context = Editor.this;
        // Шлях до папки зі скомпільованими Java файлами
        String compiledFilesDir = getFilesDir()+"schema";

        // Шлях до вихідної директорії для збереження скомпільованого коду
        String outputDirPath = "/storage/emulated/0/Download/Schema";
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Створення директорії, якщо вона не існує
        }

// Шлях до вихідного APK файлу
        String apkFilePath = outputDirPath + "/app.apk";
        File outputFile = new File(apkFilePath);


        // Компілюємо Java файли в Dalvik bytecode
        boolean success = ProjectCompiler.compileToDex(compiledFilesDir, outputFile.getAbsolutePath());

        if (success) {
            System.out.println("APK файл успішно створено: " + apkFilePath);
        } else {
            System.out.println("Помилка при створенні APK файлу.");
        }
    }

        // Виклик функції компіляції з іншого класу

    private String readFile(String folderName, String fileName) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(getFilesDir(), "Projects/" + folderName + "/" + folderName + "/" + fileName);
            System.out.println(file.getAbsolutePath());
            fis = new FileInputStream(file);
            // Вказуємо кодування UTF-8 при читанні файлу
            isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            System.out.println("Text of file: " + sb.toString());
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
        // перевіряємо, чи змінився текст
        if (!data.equals(previousText)) {
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            try {
                // Отримання шляху до папки проєкту
                File folder = new File(getFilesDir(), "Projects/" + title + "/" + title);
                if (!folder.exists()) {
                    folder.mkdirs(); // Створення папки, якщо вона не існує
                    Log.d("App", "Folder was created");
                }
                File file = new File(folder, fileName);
                System.out.println(file.getAbsolutePath());

                fos = new FileOutputStream(file);
                // Вказуємо кодування UTF-8 при записі у файл
                osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                osw.write(data);

                // Показати Toast на головному потоці
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Editor.this, "Data was saved successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();

                // Показати Toast на головному потоці
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Editor.this, "Trouble with saving data", Toast.LENGTH_SHORT).show();
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

}
