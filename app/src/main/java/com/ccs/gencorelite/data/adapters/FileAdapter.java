package com.ccs.gencorelite.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.gencorelite.R;
import com.ccs.gencorelite.data.items.FileItem;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private final List<FileItem> fileList;
    private final OnFileClickListener onFileClickListener;

    public interface OnFileClickListener {
        void onFileClick(FileItem fileItem);
        void onFileLongClick(FileItem fileItem);  // Додаємо метод для обробки тривалого натискання
    }

    public FileAdapter(List<FileItem> fileList, OnFileClickListener listener) {
        this.fileList = fileList;
        this.onFileClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);
        holder.fileNameTextView.setText(fileItem.getFileName());

        // Встановлюємо іконку відповідно до типу файлу
        if (fileItem.isImage()) {
            holder.fileIconImageView.setImageResource(R.drawable.ic_image_256); // Іконка для зображення
        } else if (fileItem.isAudio()) {
            holder.fileIconImageView.setImageResource(R.drawable.ic_audio_256); // Іконка для аудіо
        } else {
            holder.fileIconImageView.setImageResource(R.drawable.ic_file_256); // Загальна іконка для файлів
        }

        // Обробка натискання
        holder.itemView.setOnClickListener(v -> onFileClickListener.onFileClick(fileItem));

        // Обробка тривалого натискання
        holder.itemView.setOnLongClickListener(v -> {
            onFileClickListener.onFileLongClick(fileItem);
            return true;  // Повертаємо true, щоб подія була завершена
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView fileIconImageView;
        public final TextView fileNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconImageView = itemView.findViewById(R.id.iv_file_icon);
            fileNameTextView = itemView.findViewById(R.id.tv_file_name);
        }
    }
}
