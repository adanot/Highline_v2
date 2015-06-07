package com.example.anottingham.highline.util;

import org.apache.commons.net.ftp.FTPClient;

public class Result {
    private boolean isUpdateAvailable;
    private FTPClient mFtpClient;
    private long fileSize;

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean isUpdateAvailable) {
        this.isUpdateAvailable = isUpdateAvailable;
    }

    public FTPClient getFtpClient() {
        return mFtpClient;
    }

    public void setFtpClient(FTPClient mFtpClient) {
        this.mFtpClient = mFtpClient;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

