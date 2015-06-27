package com.seginf.parentalcontrol;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

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

        try {
            startRecording();
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

        if (location == null) {
            Toast.makeText(this, "location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "location: " + location.getLongitude() + " " + location.getLatitude()
                    , Toast.LENGTH_SHORT).show();
        }
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
