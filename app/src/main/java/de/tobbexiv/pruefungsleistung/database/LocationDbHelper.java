package de.tobbexiv.pruefungsleistung.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tobbexiv.pruefungsleistung.database.LocationContract.*;

public class LocationDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Location.db";

    private SQLiteDatabase db;

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Location.TABLE_NAME + " (" +
                Location._ID + " INTEGER PRIMARY KEY," +
                Location.COLUMN_NAME_CELL_ID + " TEXT," +
                Location.COLUMN_NAME_LAC + " TEXT," +
                Location.COLUMN_NAME_DESCRIPTION + " TEXT," +
                Location.COLUMN_NAME_IMAGE_SRC + " TEXT," +
                Location.COLUMN_NAME_GPS_DATA + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Location.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public String insertLocation(String cellId, String lac, String locationDescription, String imageSrc, String gpsData) {
        ContentValues values = new ContentValues();
        values.put(Location.COLUMN_NAME_CELL_ID, cellId);
        values.put(Location.COLUMN_NAME_LAC, lac);
        values.put(Location.COLUMN_NAME_DESCRIPTION, locationDescription);
        values.put(Location.COLUMN_NAME_IMAGE_SRC, imageSrc);
        values.put(Location.COLUMN_NAME_GPS_DATA, gpsData);

        return db.insert(Location.TABLE_NAME, null, values) + "";
    }

    public void updateLocation(String id, String locationDescription) {
        ContentValues values = new ContentValues();
        values.put(Location.COLUMN_NAME_DESCRIPTION, locationDescription);

        String selection = Location._ID + " = ?";
        String[] selectionArguments = { id };

        db.update(Location.TABLE_NAME, values, selection, selectionArguments);
    }

    public void deleteLocation(String id) {
        String selection = Location._ID + " = ?";
        String[] selectionArguments = { id };

        db.delete(Location.TABLE_NAME, selection, selectionArguments);
    }

    private Map[] getLocationBy(String selection, String[] selectionArguments) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map;

        String[] projection = {
                Location._ID,
                Location.COLUMN_NAME_CELL_ID,
                Location.COLUMN_NAME_LAC,
                Location.COLUMN_NAME_DESCRIPTION,
                Location.COLUMN_NAME_IMAGE_SRC,
                Location.COLUMN_NAME_GPS_DATA
        };

        String sortOrder = Location.COLUMN_NAME_CELL_ID + ", " + Location.COLUMN_NAME_LAC + " DESC";

        Cursor cursor = db.query(Location.TABLE_NAME, projection, selection, selectionArguments, null, null, sortOrder);
        cursor.getCount();
        while(cursor.moveToNext()) {
            map = new HashMap<>();

            map.put(Location._ID, cursor.getLong(cursor.getColumnIndexOrThrow(Location._ID)) + "");
            map.put(Location.COLUMN_NAME_CELL_ID, cursor.getString(cursor.getColumnIndexOrThrow(Location.COLUMN_NAME_CELL_ID)));
            map.put(Location.COLUMN_NAME_LAC, cursor.getString(cursor.getColumnIndexOrThrow(Location.COLUMN_NAME_LAC)));
            map.put(Location.COLUMN_NAME_DESCRIPTION, cursor.getString(cursor.getColumnIndexOrThrow(Location.COLUMN_NAME_DESCRIPTION)));
            map.put(Location.COLUMN_NAME_IMAGE_SRC, cursor.getString(cursor.getColumnIndexOrThrow(Location.COLUMN_NAME_IMAGE_SRC)));
            map.put(Location.COLUMN_NAME_GPS_DATA, cursor.getString(cursor.getColumnIndexOrThrow(Location.COLUMN_NAME_GPS_DATA)));

            data.add(map);
        }
        cursor.close();

        return data.toArray(new Map[data.size()]);
    }

    public Map getLocationByCellInfo(String cellId, String lac) {
        String[] selectionArguments = { cellId, lac };
        Map[] locations = getLocationBy(Location.COLUMN_NAME_CELL_ID + " = ? AND " + Location.COLUMN_NAME_LAC + " = ?", selectionArguments);

        return locations.length > 0 ? locations[0] : null;
    }

    public Map[] getAllLocations() {
        return getLocationBy(null, null);
    }
}