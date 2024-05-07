package com.ccs.gencorelite;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
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

import com.ccs.gencorelite.compiler.ProjectCompiler;
import com.ccs.gencorelite.createProject.CreateFileAndFolder;
import com.ccs.gencorelite.data.PreferenceConfig;
import com.ccs.gencorelite.editor.Editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView newProject, loadProject, title, create, return_text;
    private EditText edit_name_new_pr, edit_package_new_pr, edit_version_new_pr;
    private ImageView logo, schematic_logo;

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
        edit_package_new_pr = findViewById(R.id.edit_package_pr);
        edit_version_new_pr = findViewById(R.id.edit_version_pr);
        logo = findViewById(R.id.logo);
        schematic_logo = findViewById(R.id.logo_not_main);
        title = findViewById(R.id.title);
        loadProject = findViewById(R.id.load_project);
        create = findViewById(R.id.create_text);
        return_text = findViewById(R.id.return_text);

        newProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_name_new_pr.setVisibility(View.VISIBLE);
                edit_version_new_pr.setVisibility(View.VISIBLE);
                edit_package_new_pr.setVisibility(View.VISIBLE);
                schematic_logo.setVisibility(View.VISIBLE);
                return_text.setVisibility(View.VISIBLE);
                create.setVisibility(View.VISIBLE);

                logo.setVisibility(View.INVISIBLE);
                title.setVisibility(View.INVISIBLE);
                loadProject.setVisibility(View.INVISIBLE);
                newProject.setVisibility(View.INVISIBLE);

            }
        });
        return_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                showFolderDialog();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name_pr = edit_name_new_pr.getText().toString();
                String package_pr = edit_package_new_pr.getText().toString();
                String version_pr = edit_version_new_pr.getText().toString();

                PreferenceConfig.setPackage(getApplicationContext(), package_pr);
                PreferenceConfig.setTitle(getApplicationContext(), name_pr);
                PreferenceConfig.setVersion(getApplicationContext(), version_pr);

                CreateFileAndFolder.createFolder(MainActivity.this, name_pr);

                Intent intent = new Intent(MainActivity.this, Editor.class);
                startActivity(intent);
                overridePendingTransition(0, 0);


            }
        });

    }



    private void createApp() {

    }

    private void fadeInAnimation() {
        // Сховати елемент перед початком анімації
        logo.setAlpha(0f);

        // Створюємо анімацію появи для myView
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        // Встановлюємо тривалість анімації (в мілісекундах)
        fadeIn.setDuration(2500);
        // Встановлюємо інтерполятор (швидкість зміни анімації)
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        // Запускаємо анімацію
        fadeIn.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fadeInAnimation();
        edit_name_new_pr.setVisibility(View.INVISIBLE);
        edit_version_new_pr.setVisibility(View.INVISIBLE);
        edit_package_new_pr.setVisibility(View.INVISIBLE);
        schematic_logo.setVisibility(View.INVISIBLE);
        return_text.setVisibility(View.INVISIBLE);
        return_text.setVisibility(View.INVISIBLE);
        create.setVisibility(View.INVISIBLE);
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
        builder.setTitle("Choose project");
        builder.setItems(new CharSequence[]{"Open", "Rename", "Delete"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Опція "Перейменувати"
                                renameFolder(folderName);
                                break;
                            case 1:
                                // Опція "Видалити"
                                deleteFolder(folderName);
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void renameFolder(String folderName) {
        // Ваш код для перейменування папки
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
}
