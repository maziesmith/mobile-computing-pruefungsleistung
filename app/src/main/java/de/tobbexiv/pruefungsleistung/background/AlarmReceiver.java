package de.tobbexiv.pruefungsleistung.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static int waitTime;

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public static void setUp(Context context) {
        Intent intent = new Intent(context, AlarmManager.class);
        PendingIntent operation = PendingIntent.getBroadcast(context, 29845, intent, 0);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        waitTime = preferences.getInt("refreshInterval", 5000);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), waitTime, operation);
    }
}
