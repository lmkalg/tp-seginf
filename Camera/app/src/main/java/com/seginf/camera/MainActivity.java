package com.seginf.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


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
            Bitmap bitmap = data.getExtras().getParcelable("data");
            setLastImage(bitmap);
            if (isPokemonAppInstalled()) {
                Log.d("CAMERA", "Pokemon app is installed, sending bitmap");
                sendBitmap(bitmap);
            } else {
                Log.d("CAMERA", "Pokemon app isn't installed =(");
            }
        }
    }

    private void setLastImage(Bitmap bitmap) {
        ((ImageView) findViewById(R.id.lastImage)).setImageBitmap(invertBitmap(bitmap));
    }

    private Bitmap invertBitmap(Bitmap src) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int A, R, G, B;
        int pixelColor;
        int height = src.getHeight();
        int width = src.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = src.getPixel(x, y);
                A = Color.alpha(pixelColor);

                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);

                output.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return output;
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
