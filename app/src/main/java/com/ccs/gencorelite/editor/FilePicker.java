package com.ccs.gencorelite.editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.gencorelite.R;
import com.ccs.gencorelite.data.PreferenceConfig;
import com.ccs.gencorelite.data.adapters.FileAdapter;
import com.ccs.gencorelite.data.items.FileItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FilePicker extends AppCompatActivity implements FileAdapter.OnFileClickListener {
    private final int PICK_FILE_REQUEST_CODE = 456;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileItem> fileItems = new ArrayList<>();
    private ImageView imageView;
    private TextView fileSizeText;
    private TextView metadataText;
    private String projectFolderName;  // Ім'я проектної папки
    private ImageView import_btn;

    private ImageButton musicToggleButton;
    private SeekBar musicSeekBar;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;
    private Handler handler = new Handler();
    private Runnable updateProgressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        // Ініціалізація RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectFolderName = PreferenceConfig.getTitle(this);
        fileAdapter = new FileAdapter(fileItems, this);
        recyclerView.setAdapter(fileAdapter);
        import_btn = findViewById(R.id.import_file);

        musicToggleButton = findViewById(R.id.music_toggle_button);
        musicSeekBar = findViewById(R.id.music_seek_bar);


        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Нічого не робимо
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Нічого не робимо
            }
        });



        import_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });




        // Ініціалізація елементів для відображення файлу
        imageView = findViewById(R.id.image_view);
        fileSizeText = findViewById(R.id.file_size_text);
        metadataText = findViewById(R.id.metadata_text);

        loadProjectImages();
    }
    @Override
    public void onFileLongClick(FileItem fileItem) {
        // Створюємо AlertDialog з опціями
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Виберіть дію")
                .setItems(new String[]{"Перейменувати", "Видалити"}, (dialog, which) -> {
                    if (which == 0) {
                        // Перейменування файлу
                        showRenameDialog(fileItem);
                    } else if (which == 1) {
                        // Видалення файлу
                        deleteFile(fileItem);
                    }
                })
                .show();
    }

    @Override
    public void onFileClick(FileItem fileItem) {
        Uri fileUri = fileItem.getFileUri();
        String fileName = fileItem.getFileName();
        if (fileItem.getFileUri() == null) {
            Toast.makeText(this, "File URI is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fileItem.isAudio()) {
            setupMediaPlayer(fileUri);
        }
        // Визначаємо розмір файлу
        String fileSize = getFileSize(fileUri);
        fileSizeText.setText("File size: " + fileSize);

        // Відображення зображення (якщо це картинка)
        if (fileItem.isImage()) {
            imageView.setImageURI(fileUri);
            imageView.setVisibility(View.VISIBLE);
            musicToggleButton.setVisibility(View.GONE);
            musicSeekBar.setVisibility(View.GONE);
        } else if (fileItem.isAudio()) {
            imageView.setImageResource(android.R.color.transparent);  // Очистити зображення
            imageView.setVisibility(View.GONE);
            musicToggleButton.setVisibility(View.VISIBLE);
            musicSeekBar.setVisibility(View.VISIBLE);

            // Завантаження і налаштування медіаплеєра для відображення прогресу
            setupMediaPlayer(fileUri);
        }

        // Додаткові метадані
        metadataText.setText("File name: " + fileName);

        // Дублювання файлу у проектну папку
        duplicateFileToProjectFolder(fileUri, fileName);
    }
    private void setupMediaPlayer(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.setOnPreparedListener(mp -> {
            musicSeekBar.setMax(mediaPlayer.getDuration());  // Встановлюємо максимальне значення SeekBar як тривалість музики
            handler.post(updateProgressTask);
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.reset();
            try {
                mp.setDataSource(uri.toString());
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        updateProgressTask = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());  // Оновлюємо SeekBar
                    handler.postDelayed(this, 1000);  // Оновлюємо прогрес кожну секунду
                }
            }
        };

        musicToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMusicPlaying) {
                    stopMusic();
                } else {
                    startMusic();
                }
            }
        });
    }
    private void startMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isMusicPlaying = true;
            musicToggleButton.setImageResource(R.drawable.ic_pause_24);  // Зображення для увімкненої музики
            handler.post(updateProgressTask);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isMusicPlaying = false;
            musicToggleButton.setImageResource(R.drawable.ic_play_24);  // Зображення для вимкненої музики
            handler.removeCallbacks(updateProgressTask);

        }
    }


    private String getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            long sizeInBytes = cursor.getLong(sizeIndex);
            cursor.close();

            // Конвертуємо розмір у мегабайти або гігабайти
            if (sizeInBytes < 1024 * 1024) {
                return sizeInBytes + " Bytes"; // Якщо розмір менше 1 МБ, повертаємо в байтах
            } else if (sizeInBytes < 1024 * 1024 * 1024) {
                double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
                return String.format("%.2f MB", sizeInMB); // Повертаємо в мегабайтах
            } else {
                double sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0);
                return String.format("%.2f GB", sizeInGB); // Повертаємо в гігабайтах
            }
        }
        return "0 Б"; // Якщо не вдалося отримати розмір
    }

    private void duplicateFileToProjectFolder(Uri uri, String fileName) {
        // Шлях до проектної папки
        File projectFolder = new File(getFilesDir(), "Projects/" + projectFolderName + "/res");

        // Перевірка чи існує папка
        if (!projectFolder.exists()) {
            boolean folderCreated = projectFolder.mkdirs();
            if (!folderCreated) {
                Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to create project folder.");
                Toast.makeText(this, "Failed to create project folder.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Перевірка чи файл вже існує в проектній папці
        File destinationFile = new File(projectFolder, fileName);
        if (destinationFile.exists()) {
            Log.d("FILEPICKER / DublicateFileToProjectFolder", "File already exists in the project folder.");
            Toast.makeText(this, "File already exists in the project folder.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Копіювання файлу в проектну папку
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to open input stream.");
                Toast.makeText(this, "Failed to open input stream.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            Toast.makeText(this, "File copied to " + destinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            Log.d("FILEPICKER / DublicateFileToProjectFolder", "File copied to " + destinationFile.getAbsolutePath());

            // Перевірка типу файлу
            if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                // Шлях до папки /storage/emulated/0/Documents/GenCoreLite/scripts/res/drawable
                File externalFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GenCoreLite/scripts/res/drawable");

                // Перевірка чи існує папка
                if (!externalFolder.exists()) {
                    boolean folderCreated = externalFolder.mkdirs();
                    if (!folderCreated) {
                        Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to create external folder.");
                        Toast.makeText(this, "Failed to create external folder.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Перевірка чи файл вже існує в зовнішній папці
                File externalDestinationFile = new File(externalFolder, fileName);
                if (externalDestinationFile.exists()) {
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "File already exists in the external folder.");
                    Toast.makeText(this, "File already exists in the external folder.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Копіювання файлу в зовнішню папку
                try (InputStream externalInputStream = getContentResolver().openInputStream(uri); // Новий InputStream
                     OutputStream externalOutputStream = new FileOutputStream(externalDestinationFile)) {

                    if (externalInputStream == null) {
                        Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to open external input stream.");
                        Toast.makeText(this, "Failed to open external input stream.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    while ((length = externalInputStream.read(buffer)) > 0) {
                        externalOutputStream.write(buffer, 0, length);
                    }

                    Toast.makeText(this, "File copied to " + externalDestinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "File copied to " + externalDestinationFile.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error copying file to external folder", Toast.LENGTH_SHORT).show();
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "Error copying file to external folder: " + e);
                }

            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")) {
                // Шлях до папки /storage/emulated/0/Documents/GenCoreLite/scripts/res/raw
                File externalFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GenCoreLite/scripts/res/raw");

                // Перевірка чи існує папка
                if (!externalFolder.exists()) {
                    boolean folderCreated = externalFolder.mkdirs();
                    if (!folderCreated) {
                        Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to create external folder.");
                        Toast.makeText(this, "Failed to create external folder.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Перевірка чи файл вже існує в зовнішній папці
                File externalDestinationFile = new File(externalFolder, fileName);
                if (externalDestinationFile.exists()) {
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "File already exists in the external folder.");
                    Toast.makeText(this, "File already exists in the external folder.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Копіювання файлу в зовнішню папку
                try (InputStream externalInputStream = getContentResolver().openInputStream(uri); // Новий InputStream
                     OutputStream externalOutputStream = new FileOutputStream(externalDestinationFile)) {

                    if (externalInputStream == null) {
                        Log.d("FILEPICKER / DublicateFileToProjectFolder", "Failed to open external input stream.");
                        Toast.makeText(this, "Failed to open external input stream.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    while ((length = externalInputStream.read(buffer)) > 0) {
                        externalOutputStream.write(buffer, 0, length);
                    }

                    Toast.makeText(this, "File copied to " + externalDestinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "File copied to " + externalDestinationFile.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error copying file to external folder", Toast.LENGTH_SHORT).show();
                    Log.d("FILEPICKER / DublicateFileToProjectFolder", "Error copying file to external folder: " + e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error copying file", Toast.LENGTH_SHORT).show();
            Log.d("FILEPICKER / DublicateFileToProjectFolder", "Error copying file: " + e);
        }
    }
    private void copyFileToAssets(Uri uri, String fileName) {
        String assetPath = "project/res/";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            assetPath += "drawable/";
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".wav")) {
            assetPath += "raw/";
        } else {
            Log.d("FILEPICKER / copyFileToAssets", "Unsupported file type: " + fileName);
            return;
        }

        // Повний шлях до папки assets
        String fullAssetPath = assetPath + fileName;

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = openAssetOutputStream(fullAssetPath)) {

            if (inputStream == null || outputStream == null) {
                Log.d("FILEPICKER / copyFileToAssets", "Failed to open input or output stream.");
                Toast.makeText(this, "Failed to copy file to assets.", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            Toast.makeText(this, "File copied to assets: " + fullAssetPath, Toast.LENGTH_SHORT).show();
            Log.d("FILEPICKER / copyFileToAssets", "File copied to assets: " + fullAssetPath);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error copying file to assets", Toast.LENGTH_SHORT).show();
            Log.d("FILEPICKER / copyFileToAssets", "Error copying file to assets: " + e);
        }
    }

    private OutputStream openAssetOutputStream(String assetPath) throws IOException {
        File assetsDir = new File(getApplicationInfo().dataDir, "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }

        File assetFile = new File(assetsDir, assetPath);
        if (!assetFile.getParentFile().exists()) {
            assetFile.getParentFile().mkdirs();
        }

        return new FileOutputStream(assetFile);
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "audio/*",
                "image/*"
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = getFileName(uri);
                boolean isImage = fileName.endsWith(".jpg") || fileName.endsWith(".png");
                boolean isAudio = fileName.endsWith(".wav") || fileName.endsWith(".ogg") || fileName.endsWith(".mp3");

                FileItem fileItem = new FileItem(fileName, uri, isImage, isAudio);
                fileItems.add(fileItem);
                fileAdapter.notifyDataSetChanged();
            }
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
    private void loadProjectImages() {
        // Шлях до проектної папки
        File projectFolder = new File(getFilesDir(), "Projects/" + projectFolderName + "/res");

        // Перевірка чи існує папка
        if (!projectFolder.exists() || !projectFolder.isDirectory()) {
            Toast.makeText(this, "Project folder does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отримуємо всі файли з проектної папки
        File[] files = projectFolder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".wav"));

        if (files != null && files.length > 0) {
            for (File file : files) {
                Uri fileUri = Uri.fromFile(file);
                String fileName = file.getName();

                // Визначаємо чи це зображення чи аудіо на основі розширення файлу
                boolean isImage = fileName.endsWith(".jpg") || fileName.endsWith(".png");
                boolean isAudio = fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".wav");

                // Створюємо об'єкт FileItem з коректними значеннями для зображень та аудіо
                FileItem fileItem = new FileItem(fileName, fileUri, isImage, isAudio);
                fileItems.add(fileItem);
            }

            fileAdapter.notifyDataSetChanged(); // Оновлення списку в RecyclerView
        } else {
            Toast.makeText(this, "No images or audio found in the project folder.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateProgressTask);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateProgressTask);
    }
    // Метод для показу AlertDialog з опціями
    private void showFileOptionsDialog(int position) {
        FileItem fileItem = fileItems.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Options");
        builder.setItems(new String[]{"Rename", "Delete"}, (dialog, which) -> {
            switch (which) {
                case 0:  // Перейменування файлу
                    showRenameDialog(fileItem);
                    break;
                case 1:  // Видалення файлу
                    deleteFile(fileItem);
                    break;
            }
        });
        builder.show();
    }

    // Метод для перейменування файлу
    private void showRenameDialog(FileItem fileItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(fileItem.getFileName());

        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newFileName = input.getText().toString();
            renameFile(fileItem, newFileName);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void renameFile(FileItem fileItem, String newFileName) {
        File projectFolder = new File(getFilesDir(), "Projects/" + projectFolderName + "/res");
        File oldFile = new File(projectFolder, fileItem.getFileName());
        File newFile = new File(projectFolder, newFileName);

        if (oldFile.exists()) {
            boolean renamed = oldFile.renameTo(newFile);
            if (renamed) {
                fileItem.setFileName(newFileName);
                updateFileList();
                Toast.makeText(this, "File renamed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to rename file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Метод для видалення файлу
    private void deleteFile(FileItem fileItem) {
        File projectFolder = new File(getFilesDir(), "Projects/" + projectFolderName + "/res");
        File fileToDelete = new File(projectFolder, fileItem.getFileName());

        if (fileToDelete.exists()) {
            boolean deleted = fileToDelete.delete();
            if (deleted) {
                fileItems.remove(fileItem);
                updateFileList();
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Метод для оновлення списку файлів
    private void updateFileList() {
        fileAdapter.notifyDataSetChanged();  // Оновлює адаптер після змін
    }
}