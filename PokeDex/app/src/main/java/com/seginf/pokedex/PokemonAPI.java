package com.seginf.pokedex;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface PokemonAPI {

    @GET("/api/v1/pokemon/{pokemonNumber}")
    void getPokemon(@Path("pokemonNumber") int pokemonNumber, Callback<Pokemon> callback);

}
