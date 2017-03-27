package de.tobbexiv.pruefungsleistung.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.RemoteViews;

import java.io.File;
import java.util.Map;

import de.tobbexiv.pruefungsleistung.R;
import de.tobbexiv.pruefungsleistung.database.LocationContract.Location;
import de.tobbexiv.pruefungsleistung.database.LocationDbHelper;

public class WidgetProvider extends AppWidgetProvider {
    private static LocationDbHelper dbHelper;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if(dbHelper == null) {
            dbHelper = new LocationDbHelper(context);
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        String cellId = cellLocation.getCid() + "";
        String lac = cellLocation.getLac() + "";

        Map<String, String> stored = dbHelper.getLocationByCellInfo(cellId, lac);
        setLocationInformation(context, stored);
    }

    public static void setLocationInformation(Context context, Map<String, String> location) {
        String description = context.getString(R.string.widget_unknown_location);
        String cellId = "";
        Bitmap myBitmap = null;

        if(location != null) {
            description = location.get(Location.COLUMN_NAME_DESCRIPTION);
            cellId = context.getString(R.string.display_location_view_cell_id, location.get(Location.COLUMN_NAME_CELL_ID), location.get(Location.COLUMN_NAME_LAC));
            File imgFile = new File(location.get(Location.COLUMN_NAME_IMAGE_SRC));
            if (imgFile.exists()) {
                myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_widget);
        remoteViews.setTextViewText(R.id.widget_description, description);
        remoteViews.setTextViewText(R.id.widget_cellid, cellId);
        remoteViews.setImageViewBitmap(R.id.widget_image, myBitmap);

        pushWidgetUpdate(context, remoteViews);
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }
}
