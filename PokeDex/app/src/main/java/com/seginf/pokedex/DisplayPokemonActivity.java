package com.seginf.pokedex;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;



import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DisplayPokemonActivity extends AppCompatActivity {

    public static final String POKEMON_NUMBER_KEY = "pokemon number";
    private static final String API_HOST = "http://pokeapi.co";
    private static final String ATTACK = "Attack: ";
    private static final String CATCH_RATE = "Catch rate: ";
    private static final String DEFENSE = "Defense: ";
    private static final String GROWTH_RATE = "Growth rate: ";
    private static final String HP = "HP: ";
    private static final String SPECIES = "Species: ";

    private TextView nameTextView;
    private TextView attackTextView;
    private TextView catchRateTextView;
    private TextView defenseTextView;
    private TextView growthRateTextView;
    private TextView hpTextView;
    private TextView speciesTextView;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pokemon);

        setUpView();

        int pokemonNumber = getIntent().getIntExtra(POKEMON_NUMBER_KEY, 0);
        try {
            executePokemonRequest(pokemonNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executePokemonRequest(int pokemonNumber) throws IOException {


        FTPClient ftpClient = new FTPClient();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        // try {
        ftpClient.connect(InetAddress.getByName("ftp.deanastasie.com.ar"));
        ftpClient.login("deanastasie", "3rLJskyhsF");
        ftpClient.changeWorkingDirectory("test/fotos/");

        if (ftpClient.getReplyString().contains("250")) {
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            BufferedInputStream buffIn = null;
            buffIn = new BufferedInputStream(new FileInputStream("/sdcard/Pictures/Twitter/prueba.jpg"));
            ftpClient.enterLocalPassiveMode();
            // ProgressInputStream progressInput = new ProgressInputStream(buffIn, progressHandler);
            boolean result = ftpClient.storeFile("DIEGOOOOO.jpg", buffIn);
            buffIn.close();
            ftpClient.logout();
            ftpClient.disconnect();
        }
/*
        } catch (SocketException e) {
            Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
        } catch (UnknownHostException e) {
            Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
        } catch (IOException e) {
            Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
        }
*/


        PokemonAPI pokemonAPI = new RestAdapter.Builder()
                .setEndpoint(API_HOST)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(PokemonAPI.class);

        pokemonAPI.getPokemon(pokemonNumber, new Callback<Pokemon>() {
            @Override
            public void success(Pokemon pokemon, Response response) {
                onRequestSuccess(pokemon);
            }

            @Override
            public void failure(RetrofitError error) {
                onRequestFailure(error);
            }
        });
    }

    private void setUpView() {
        hpTextView = (TextView) findViewById(R.id.pokemonHp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        attackTextView = (TextView) findViewById(R.id.pokemonAttack);
        speciesTextView = (TextView) findViewById(R.id.pokemonSpecies);
        defenseTextView = (TextView) findViewById(R.id.pokemonDefense);
        catchRateTextView = (TextView) findViewById(R.id.pokemonCatchRate);
        growthRateTextView = (TextView) findViewById(R.id.pokemonGrowthRate);
    }

    private void onRequestSuccess(Pokemon pokemon) {
        dismissProgressBar();
        getSupportActionBar().setTitle(pokemon.getName());
        hpTextView.setText(HP + pokemon.getHp());
        attackTextView.setText(ATTACK + pokemon.getAttack());
        speciesTextView.setText(SPECIES + pokemon.getSpecies());
        defenseTextView.setText(DEFENSE + pokemon.getDefense());
        catchRateTextView.setText(CATCH_RATE + pokemon.getCatchRate());
        growthRateTextView.setText(GROWTH_RATE + pokemon.getGrowthRate());
    }

    private void onRequestFailure(RetrofitError error) {
        dismissProgressBar();
        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
        finish();
    }

    private void dismissProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
