package com.seginf.parentalcontrol;

import retrofit.Callback;
import retrofit.http.GET;

public interface CommandApi {

    @GET("/seginf/pc/command.txt")
    void getCommand(Callback<String> callback);

}
