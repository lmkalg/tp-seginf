package com.seginf.pokedex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;










public class MainActivity extends AppCompatActivity {

    private EditText inputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        inputEditText = (EditText) findViewById(R.id.inputEditText);

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

    public void search(View v) {
        int pokemonNumber = Integer.parseInt(inputEditText.getText().toString());
        if (pokemonNumber < 0 || pokemonNumber > 150) {
            Toast.makeText(this, "Please enter a number between 1 and 150", Toast.LENGTH_LONG).show();
            return;
        }
        Intent pokemonDisplayIntent = new Intent(this, DisplayPokemonActivity.class);
        pokemonDisplayIntent.putExtra(DisplayPokemonActivity.POKEMON_NUMBER_KEY, pokemonNumber);
        startActivity(pokemonDisplayIntent);
    }

}
