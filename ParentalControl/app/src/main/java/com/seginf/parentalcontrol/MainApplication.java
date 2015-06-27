package com.seginf.parentalcontrol;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startCommandService(this);
    }

    public static void startCommandService(Context context) {
        Intent serviceIntent = new Intent(context, CommandService.class);

        if (!CommandService.isRunning()) {
            context.startService(serviceIntent);
            final PendingIntent pIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 15);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 500000, pIntent);
        }
    }
}
