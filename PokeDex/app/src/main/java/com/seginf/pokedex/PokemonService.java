package com.seginf.pokedex;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class PokemonService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Parcelable photo = intent.getParcelableExtra("photo");

        if (photo != null) {
            Log.d("PokemonService", "received bitmap!!");
        } else {
            Log.d("PokemonService", "photo was null");
        }

        return Service.START_STICKY;
    }
}
