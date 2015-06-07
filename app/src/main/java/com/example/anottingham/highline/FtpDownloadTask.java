package com.example.anottingham.highline;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.anottingham.highline.util.Result;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class FtpDownloadTask extends AsyncTask<Result, Integer, Boolean> {
    private static final String APP_PREFERENCES = "timeSettings";
    private static final String PREFERENCES_TIME = "time";

    private ProgressDialog progressDialog;
    private Context context;

    public FtpDownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Download");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Result... params) {
        FTPClient mFtpClient = params[0].getFtpClient();
        long fileSize = params[0].getFileSize();
        int count = 0;
        try {
            InputStream inputStream = mFtpClient.retrieveFileStream(
                    context.getResources().getString(R.string.ftp_file_name));
            if (inputStream != null) {
                Log.d("FtpDownloadTask", "Input stream open successfully");
                Log.d("FtpDownloadTask", Environment.getExternalStorageDirectory().getParent() +
                        "/Download");
                File resultFile = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "HighlineUpdate.apk");
                OutputStream outputStream = new FileOutputStream(resultFile);

                long total = 0;
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;

                Log.d("FtpDownloadTask", String.valueOf(fileSize));
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    total += bytesRead;

                    Log.d("FtpDownloadTask", String.valueOf(total));
                    publishProgress((int) ((total + 0.0) / fileSize * 100));
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                outputStream.close();
                inputStream.close();

            }


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();

        if (aVoid) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "HighlineUpdate.apk")),
                    "application/vnd.android.package-archive");

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            SharedPreferences prefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(PREFERENCES_TIME, new Date().getTime());
            editor.apply();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }
}
