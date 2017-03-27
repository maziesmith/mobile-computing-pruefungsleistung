package de.tobbexiv.pruefungsleistung.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class CellStateService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CellStateReceiver.BROADCAST_START_CELL_CHANGE_LISTNER);
        sendBroadcast(broadcastIntent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CellStateReceiver.BROADCAST_STOP_CELL_CHANGE_LISTNER);
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
