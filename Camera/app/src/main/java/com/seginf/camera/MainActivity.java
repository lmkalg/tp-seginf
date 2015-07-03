package com.seginf.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private int CAMERA = 123;
    private String POKEDEX_PACKAGE = "com.seginf.pokedex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openCamera(View v) {
        Intent i = new Intent();
        i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {
            if (data.getExtras() == null) return;
            if (isPokemonAppInstalled()) {
                Log.d("CAMERA", "Pokemon app is installed, sending bitmap");
                sendBitmap(data.getExtras().getParcelable("data"));
            } else {
                Log.d("CAMERA", "Pokemon app isn't installed =(");
            }
        }
    }

    private void sendBitmap(Parcelable bitmap) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(POKEDEX_PACKAGE, "com.seginf.pokedex.PokemonService"));
        intent.putExtra("photo", bitmap);
        startService(intent);
    }

    private boolean isPokemonAppInstalled() {
        try {
            getPackageManager().getPackageInfo(POKEDEX_PACKAGE, PackageManager.GET_SERVICES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
