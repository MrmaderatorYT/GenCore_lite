package com.ccs.gencorelite.editor;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Editor extends AppCompatActivity {
    private ListView fileList;
    private EditText editor;
    private ImageView compile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

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


        compile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compileApp();
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
                    printFileContentToLog(Editor.this, "app/resources/" + fileName);
//                } else if (fileName.equals("AndroidManifest.xml")) {
//                    printFileContentToLog(Editor.this, "file:///android_assets/manifest/" + fileName);
                } else if (fileName.contains(".xml")&&fileName.contains("activity")) {
                    printFileContentToLog(Editor.this, "app/layout/" + fileName);
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
    private void saveData(String fileName, String data) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            Toast.makeText(this, "Data was saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Trouble with saving data", Toast.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void printFileContentToLog(Context context, String fileName) {
        try {
            // Отримання AssetManager для доступу до активів
            AssetManager assetManager = context.getAssets();
            // Відкриття файлу у вигляді InputStream
            InputStream inputStream = assetManager.open(fileName);

            // Читання файлу з InputStream
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // Виведення змісту файлу в логи
            editor.setText(sb.toString());
            // Закриття потоків
            inputStream.close();
            isr.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileContent", "Error reading file: " + fileName);
        }
    }
    private void compileApp() {
        String projectPath = "Projects/"; // Шлях до завантаженого проєкту
        String outputPath = "/storage/emulated/0/Download/app.apk"; // Шлях для збереження APK

        // Виклик функції компіляції з іншого класу
        ProjectCompiler.compileProject(projectPath, outputPath);
    }

}
