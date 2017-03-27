package de.tobbexiv.pruefungsleistung.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.util.Map;

import de.tobbexiv.pruefungsleistung.MainActivity;
import de.tobbexiv.pruefungsleistung.database.LocationDbHelper;
import de.tobbexiv.pruefungsleistung.widget.WidgetProvider;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static boolean running = false;

    private TelephonyManager manager;
    private Listener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new Listener(context);

        if(!running) {
            manager.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION);
        }
    }

    public class Listener extends PhoneStateListener {
        Context context;
        LocationDbHelper dbHelper;

        public Listener(Context context) {
            this.context = context;
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);

            if(location instanceof GsmCellLocation) {
                GsmCellLocation cell = (GsmCellLocation) location;
                String cellId = cell.getCid() + "";
                String lac = cell.getLac() + "";

                Map<String, String> stored = dbHelper.getLocationByCellInfo(cellId, lac);
                if(stored == null) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    if(preferences.getBoolean("startAutomatically", true)) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("NEW_CELL", true);
                        context.startActivity(intent);
                    }
                } else {
                    WidgetProvider.setLocationInformation(context, stored);
                }
            }
        }
    }
}
