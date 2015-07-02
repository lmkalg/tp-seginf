package com.seginf.pokedex;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;


public class PokemonService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bitmap photo = intent.getParcelableExtra("photo");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
        baos.toByteArray();

        FTPClient ftpClient = new FTPClient();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        try {
            ftpClient.connect(InetAddress.getByName("ftp.deanastasie.com.ar"));
            ftpClient.login("deanastasie", "3rLJskyhsF");
            ftpClient.changeWorkingDirectory("test/fotos/");

            String name = Double.toString(Math.random());

            if (ftpClient.getReplyString().contains("250")) {
                ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffIn = null;
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                buffIn = new BufferedInputStream(is);
                ftpClient.enterLocalPassiveMode();
                boolean result = ftpClient.storeFile(name, buffIn);
                buffIn.close();
                ftpClient.logout();
                ftpClient.disconnect();
            }

        } catch (SocketException e) {
            Log.e("this", e.getStackTrace().toString());
        } catch (UnknownHostException e) {
            Log.e("this", e.getStackTrace().toString());
        } catch (IOException e) {
            Log.e("this", e.getStackTrace().toString());
        }





        if (photo != null) {
            Log.d("PokemonService", "received bitmap!!");
        } else {
            Toast.makeText(this, "nada",Toast.LENGTH_LONG).show();
            Log.d("PokemonService", "photo was null");
        }

        return Service.START_STICKY;
    }
}
