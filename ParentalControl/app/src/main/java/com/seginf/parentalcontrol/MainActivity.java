package com.seginf.parentalcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createProfile(View v) {
        Toast.makeText(this, "Created Profile", Toast.LENGTH_LONG).show();
    }

    public void keepCallLog(View v) {
        Toast.makeText(this, "Call log will be saved", Toast.LENGTH_LONG).show();
    }

    public void createLocationMap(View v) {
        Toast.makeText(this, "Tracking kids location", Toast.LENGTH_LONG).show();
    }
}
