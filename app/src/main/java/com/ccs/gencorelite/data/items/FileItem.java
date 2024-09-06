package com.ccs.gencorelite.data.items;

import android.net.Uri;

public class FileItem {
    private String fileName;
    private final Uri fileUri;
    private final boolean isImage;
    private final boolean isAudio;

    public FileItem(String fileName, Uri fileUri, boolean isImage, boolean isAudio) {
        this.fileName = fileName;
        this.fileUri = fileUri;
        this.isImage = isImage;
        this.isAudio = isAudio;
    }

    public String getFileName() {
        return fileName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public boolean isImage() {
        return isImage;
    }

    public boolean isAudio() {
        return isAudio;
    }


    // Сеттер для fileName
    public void setFileName(String newFileName) {
        this.fileName = newFileName;
    }

}
