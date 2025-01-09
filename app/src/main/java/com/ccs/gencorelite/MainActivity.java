package com.ccs.gencorelite;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ccs.gencorelite.data.PreferenceConfig;
import com.ccs.gencorelite.editor.Editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView newProject, loadProject, title, create, return_text, version;
    private EditText edit_name_new_pr, edit_package_new_pr, edit_version_new_pr;
    private ImageView logo, schematic_logo, brand_logo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Приховати панель навігації та зробити режим повноекранним
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        newProject = findViewById(R.id.new_project);
        edit_name_new_pr = findViewById(R.id.edit_name_pr);
        version = findViewById(R.id.version);
        edit_package_new_pr = findViewById(R.id.edit_package_pr);
        edit_version_new_pr = findViewById(R.id.edit_version_pr);
        logo = findViewById(R.id.logo);
        schematic_logo = findViewById(R.id.logo_not_main);
        title = findViewById(R.id.title);
        loadProject = findViewById(R.id.load_project);
        create = findViewById(R.id.create_text);
        return_text = findViewById(R.id.return_text);
        brand_logo = findViewById(R.id.brand_logo);

        hideAllElements();

        // Запускаємо анімацію повороту на 360 градусів для brand_logo
        startRotationAnimation();

        newProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MAINACTIVITY / onCreate", "New project btn pressed");
                edit_name_new_pr.setVisibility(View.VISIBLE);
                edit_version_new_pr.setVisibility(View.VISIBLE);
                edit_package_new_pr.setVisibility(View.VISIBLE);
                schematic_logo.setVisibility(View.VISIBLE);
                return_text.setVisibility(View.VISIBLE);
                create.setVisibility(View.VISIBLE);
                version.setVisibility(View.INVISIBLE);

                logo.setVisibility(View.INVISIBLE);
                title.setVisibility(View.INVISIBLE);
                loadProject.setVisibility(View.INVISIBLE);
                newProject.setVisibility(View.INVISIBLE);

            }
        });
        return_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MAINACTIVITY / onCreate", "return text pressed");

                edit_name_new_pr.setVisibility(View.INVISIBLE);
                edit_version_new_pr.setVisibility(View.INVISIBLE);
                edit_package_new_pr.setVisibility(View.INVISIBLE);
                schematic_logo.setVisibility(View.INVISIBLE);
                return_text.setVisibility(View.INVISIBLE);
                create.setVisibility(View.INVISIBLE);

                logo.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                loadProject.setVisibility(View.VISIBLE);
                newProject.setVisibility(View.VISIBLE);
            }
        });
        loadProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MAINACTIVITY / onCreate", "load project btn pressed");

                showFolderDialog();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = PreferenceConfig.getTitle(MainActivity.this);
                if (!name.isEmpty()) {
                    // Створення діалогового вікна
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Sorry"); // Заголовок вікна
                    builder.setMessage("In version 0.1 OEAP, creating multiple projects is not available.\nPlease wait for new updates.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Дія при натисканні кнопки "OK"
                            dialog.dismiss(); // Закрити діалогове вікно
                        }
                    });
                    builder.create().show(); // Показати діалогове вікно
                }else {
                    String name_pr = edit_name_new_pr.getText().toString();
                    String package_pr = edit_package_new_pr.getText().toString();
                    String version_pr = edit_version_new_pr.getText().toString();

                    PreferenceConfig.setPackage(getApplicationContext(), package_pr);
                    PreferenceConfig.setTitle(getApplicationContext(), name_pr);
                    PreferenceConfig.setVersion(getApplicationContext(), version_pr);

                    createFileInProjectFolder(name_pr, "messages.gc_l", "" +
                            "SCENE 1\n" +
                            "BACKGROUND hall.png\n" +
                            "MUSIC intro.mp3\n" +
                            "DIALOG John: Привет, как дела?\n" +
                            "CHOICE Выберите действие: | Пойти направо | Пойти налево\n" +
                            "GOTO 2\n" +
                            "END\n" +
                            "SCENE 2\n" +
                            "BACKGROUND hall.png\n" +
                            "DIALOG Alice: Ты пришел!\n" +
                            "SOUND intro.wav\n" +
                            "GOTO 3\n" +
                            "END\n" +
                            "SCENE 3\n" +
                            "BACKGROUND hall.png\n" +
                            "DIALOG John: Что будем делать дальше?\n" +
                            "CHOICE Выберите действие: | Пойти спать | Остаться\n" +
                            "END\n" +
                            "SCENE 4\n" +
                            "BACKGROUND hall.png\n" +
                            "DIALOG John: Что будем делать дальше?\n" +
                            "END");
                    createFileInProjectFolder(name_pr, "main_screen.gc_l",
                            "SCENE MAIN\n" +
                                    "BACKGROUND hall.png\n" +
                                    "MUSIC intro.mp3\n" +
                                    "END");
                    createFileInProjectFolder(name_pr, "settings_screen.gc_l",
                            "SCENE SETTINGS\n" +
                                    "BACKGROUND hall.png\n" +
                                    "MUSIC intro.mp3\n" +
                                    "END");
                    createFileInProjectFolder(name_pr, "colors.gc_l",
                            "#in 0.1 OEAP is not available to use this file\n"+
                            "COLOR : WHITE = #FFFFFF\n" +
                            "COLOR : BLACK = #000000\n" +
                            "COLOR : RED = #FF0000\n" +
                            "COLOR : GREEN = #00FF00\n" +
                            "COLOR : BLUE = #0000FF\n" +
                            "COLOR : YELLOW = #FFFF00\n" +
                            "COLOR : CYAN = #00FFFF\n" +
                            "COLOR : MAGENTA = #FF00FF\n" +
                            "COLOR : ORANGE = #FFA500\n" +
                            "COLOR : PURPLE = #800080\n" +
                            "COLOR : PINK = #FFC0CB\n" +
                            "COLOR : GRAY = #808080\n" +
                            "COLOR : SILVER = #C0C0C0\n" +
                            "COLOR : MAROON = #800000\n" +
                            "COLOR : OLIVE = #808000\n" +
                            "COLOR : LIME = #00FF00\n" +
                            "COLOR : TEAL = #008080\n" +
                            "COLOR : NAVY = #000080\n" +
                            "COLOR : AQUA = #00FFFF\n" +
                            "COLOR : BEIGE = #F5F5DC");

                    Intent intent = new Intent(MainActivity.this, Editor.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });

    }






    private void startRotationAnimation() {
        // Анімація повороту на 360 градусів для brand_logo
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(brand_logo, "rotationY", 0f, 360f);
        rotationAnimator.setDuration(2000); // Тривалість анімації
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // Після завершення анімації повороту, приховуємо brand_logo і запускаємо fadeInAnimation
        rotationAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Анімація починається
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Анімація завершена
                brand_logo.setVisibility(View.INVISIBLE); // Приховуємо brand_logo
                fadeInAnimation(); // Запускаємо fadeInAnimation для інших елементів
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Анімація скасована
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Анімація повторюється
            }
        });

        rotationAnimator.start();
    }

    private void fadeInAnimation() {
        // Показуємо всі елементи після завершення анімації
        newProject.setVisibility(View.VISIBLE);
        loadProject.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        version.setVisibility(View.VISIBLE);
        logo.setVisibility(View.VISIBLE);

        // Анімація появи для елементів
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.start();

        ObjectAnimator fadeIn2 = ObjectAnimator.ofFloat(newProject, "alpha", 0f, 1f);
        fadeIn2.setDuration(1000);
        fadeIn2.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn2.start();

        ObjectAnimator fadeIn3 = ObjectAnimator.ofFloat(loadProject, "alpha", 0f, 1f);
        fadeIn3.setDuration(1000);
        fadeIn3.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn3.start();

        ObjectAnimator fadeIn4 = ObjectAnimator.ofFloat(version, "alpha", 0f, 1f);
        fadeIn4.setDuration(1000);
        fadeIn4.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn4.start();
    }
    private void hideAllElements() {
        newProject.setVisibility(View.INVISIBLE);
        loadProject.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        create.setVisibility(View.INVISIBLE);
        return_text.setVisibility(View.INVISIBLE);
        version.setVisibility(View.INVISIBLE);
        edit_name_new_pr.setVisibility(View.INVISIBLE);
        logo.setVisibility(View.INVISIBLE);
        edit_package_new_pr.setVisibility(View.INVISIBLE);
        edit_version_new_pr.setVisibility(View.INVISIBLE);
        schematic_logo.setVisibility(View.INVISIBLE);
        brand_logo.setVisibility(View.VISIBLE); // Показуємо brand_logo для анімації
    }


    @Override
    protected void onStart() {
        super.onStart();
    }
    private void createFileInProjectFolder(String folderName, String fileName, String data) {
        FileOutputStream fos = null;
        try {
            // Отримання шляху до папки Projects
            File projectsFolder = new File(getFilesDir()+"/Projects/"+folderName);
            // Перевірка чи папка Projects існує, якщо ні - створення

            // Отримання шляху до папки проєкту в папці Projects
            File projectFolder = new File(projectsFolder, folderName);
            // Перевірка чи папка проєкту існує, якщо ні - створення
            if (!projectFolder.exists()) {
                projectFolder.mkdirs();
            }

            // Створення файлового виводу
            File file = new File(projectFolder, fileName);
            Log.d("MAINACTIVITY / createFileInProjectFolder", "Project folder: "+ projectFolder);
            fos = new FileOutputStream(file);
            // Запис даних у файл
            fos.write(data.getBytes());
            Toast.makeText(this, "File created successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
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




    private void showFolderDialog() {
        ArrayList<String> folderNames = getFolderNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, folderNames);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose project");
        builder.setView(listView);
        builder.setCancelable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFolder = folderNames.get(position);
                showFolderOptionsDialog(selectedFolder);
            }
        });

        builder.show();
    }

    private void showFolderOptionsDialog(String folderName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(folderName);
        builder.setItems(new CharSequence[]{getResources().getString(R.string.open), getResources().getString(R.string.rename), getResources().getString(R.string.delete)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                PreferenceConfig.setTitle(getApplicationContext(), folderName);
                                openFolder(folderName);
                                break;
                            case 1:
                                // Опція "Перейменувати"
                                renameFolder(folderName);
                                break;
                            case 2:
                                // Опція "Видалити"
                                deleteFolder(folderName);
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void renameFolder(String oldFolderName) {
        // Показати діалог для введення нової назви папки
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new folder name");

        // Введення нової назви папки
        final EditText input = new EditText(this);
        builder.setView(input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFolderName = input.getText().toString().trim();

                if (newFolderName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Перевірка наявності папки з новим ім'ям
                File oldFolder = new File(getFilesDir(), "Projects" + File.separator + oldFolderName);
                File newFolder = new File(getFilesDir(), "Projects" + File.separator + newFolderName);

                if (newFolder.exists()) {
                    Toast.makeText(getApplicationContext(), "Folder with this name already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Перейменування папки
                boolean renamed = oldFolder.renameTo(newFolder);
                if (renamed) {
                    Toast.makeText(getApplicationContext(), "Folder renamed successfully", Toast.LENGTH_SHORT).show();
                    // Оновити список папок
                    showFolderDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to rename folder", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void deleteFolder(String folderName) {
        // Отримання шляху до папки
        File folder = new File(getFilesDir(), "Projects" + File.separator + folderName);
        // Видалення папки
        boolean deleted = deleteRecursive(folder);
        if (deleted) {
            Toast.makeText(this, "Folder was successfully removed", Toast.LENGTH_SHORT).show();
            // Оновити список папок
            showFolderDialog();
        } else {
            Toast.makeText(this, "Unknown trouble with removing folder", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    private ArrayList<String> getFolderNames() {
        ArrayList<String> folderNames = new ArrayList<>();
        File appDirectory = new File(getFilesDir(), "Projects");
        if (appDirectory.exists() && appDirectory.isDirectory()) {
            File[] projectFolders = appDirectory.listFiles(File::isDirectory);
            if (projectFolders != null) {
                for (File folder : projectFolders) {
                    folderNames.add(folder.getName());
                }
            }
        }
        return folderNames;
    }
    private void openFolder(String folderName) {
        // Отримуємо шлях до папки
        File folder = new File(getFilesDir(), "Projects" + File.separator + folderName);
        String folderStr = folder.toString();
        Log.d("MAINACTIVITY / OpenFolder", "Dir: "+folderStr);



        Intent intent = new Intent(MainActivity.this, Editor.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}
