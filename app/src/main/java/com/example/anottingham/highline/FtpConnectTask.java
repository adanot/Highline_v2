package com.example.anottingham.highline;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.anottingham.highline.util.Result;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FtpConnectTask extends AsyncTask<Void, Void, Result> {

    private static final String APP_PREFERENCES = "timeSettings";
    private static final String PREFERENCES_TIME = "time";

    private Context context;
    private SplashScreenActivity activity;
    private SweetAlertDialog pDialog;

    public FtpConnectTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Checking...");
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return connectToFTP(context.getResources().getString(R.string.ftp_url),
                context.getResources().getString(R.string.ftp_login),
                context.getResources().getString(R.string.ftp_password));
    }

    @Override
    protected void onPostExecute(final Result result) {
        super.onPostExecute(result);
        pDialog.dismiss();

        if (result.isUpdateAvailable()) {
            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Update is available")
                    .setContentText("Do you want to install it?")
                    .setConfirmText("Update")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            new FtpDownloadTask(context).execute(result);
                            sDialog.dismiss();
                        }
                    })
                    .setCancelText("Cancel")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    })
                    .show();
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Sorry!")
                    .setContentText("No updates available")
                    .setConfirmText("OK")
                    .show();
        }
    }

    Result connectToFTP(String URL, String userName, String password) {

        boolean status = false;

        Result result = new Result();

        try {
            FTPClient mFtpClient = new FTPClient();
            mFtpClient.setConnectTimeout(10 * 1000);
            mFtpClient.connect(URL);
            status = mFtpClient.login(userName, password);
            if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                mFtpClient.setFileType(FTP.ASCII_FILE_TYPE);
                mFtpClient.enterLocalPassiveMode();
                FTPFile[] files = mFtpClient.listFiles();
                for (FTPFile file : files) {

                    SharedPreferences prefs = context.getSharedPreferences(APP_PREFERENCES,
                            Context.MODE_PRIVATE);

                    long time = prefs.getLong(PREFERENCES_TIME, 0);
                    if (file.getName().equals(context.getResources().getString(R.string.ftp_file_name))) {
                        if (new Date(time).before(file.getTimestamp().getTime())) {
                            Log.d("CompareDates", "Before");

                            result.setFtpClient(mFtpClient);
                            result.setUpdateAvailable(true);
                            result.setFileSize(file.getSize());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SplashScreenActivity", String.valueOf(status));
        return result;
    }
}
