package com.seginf.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private int CAMERA = 123;

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

    /**
     * Called when list images button is pressed.
     */
    public void listImages(View v) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {
            if (data.getExtras() == null) return;
            String photo = MediaStore.Images.Media.insertImage(getContentResolver(),
                    (Bitmap) data.getExtras().getParcelable("data"), "foto", "foto");
            if (photo == null) {
                Toast.makeText(this, "la imagen actual es null", Toast.LENGTH_LONG).show();
            } else {
                Uri photoUri = Uri.parse(photo);
                Intent i = new Intent();
                i.putExtra("photo", photoUri);
                i.setData(Uri.parse("seginf://pokedex"));
                i.setPackage("com.seginf.pokedex");
                startActivity(i);
            }
        }
    }
}
