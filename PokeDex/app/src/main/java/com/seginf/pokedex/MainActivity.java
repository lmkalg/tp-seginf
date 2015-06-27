package com.seginf.pokedex;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() == null) {
            Toast.makeText(this, "extras are null", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getIntent().getExtras().toString(), Toast.LENGTH_LONG).show();
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.parse(getIntent().getStringExtra("photo")));
                ((ImageView) findViewById(R.id.image)).setImageBitmap(b);
            } catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
