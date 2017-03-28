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
import android.widget.Toast;

import java.util.Map;

import de.tobbexiv.pruefungsleistung.MainActivity;
import de.tobbexiv.pruefungsleistung.database.LocationDbHelper;
import de.tobbexiv.pruefungsleistung.widget.WidgetProvider;

public class CellStateReceiver extends BroadcastReceiver {
    public static final String BROADCAST_START_CELL_CHANGE_LISTNER = "de.tobbexiv.pruefungsleistung.StartCellChangeListener";
    public static final String BROADCAST_STOP_CELL_CHANGE_LISTNER = "de.tobbexiv.pruefungsleistung.StopCellChangeListener";

    private static boolean running = false;

    private static TelephonyManager manager;
    private static Listener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(manager == null || listener == null) {
            manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            listener = new Listener(context);
        }

        String action = intent.getAction();

        if(!running && action.equals(BROADCAST_START_CELL_CHANGE_LISTNER)) {
            manager.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION);
            running = true;
        } else if(running && action.equals(BROADCAST_STOP_CELL_CHANGE_LISTNER)) {
            manager.listen(listener, PhoneStateListener.LISTEN_NONE);
            running = false;
        }
    }

    public class Listener extends PhoneStateListener {
        Context context;
        LocationDbHelper dbHelper;

        public Listener(Context context) {
            this.context = context;
            this.dbHelper = new LocationDbHelper(context);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);

            if(location instanceof GsmCellLocation) {
                GsmCellLocation cell = (GsmCellLocation) location;
                String cellId = cell.getCid() + "";
                String lac = cell.getLac() + "";

                Map<String, String> stored = dbHelper.getLocationByCellInfo(cellId, lac);
                WidgetProvider.setLocationInformation(context, stored);
                if(stored == null) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    if(preferences.getBoolean("startAutomatically", false)) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("NEW_CELL", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        context.startActivity(intent);
                    }
                }
            } else if(location == null) {
                WidgetProvider.setLocationInformation(context, null);
            }
        }
    }
}
