package com.seginf.parentalcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CommandBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MainApplication.startCommandService(context);
    }
}
