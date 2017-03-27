package de.tobbexiv.pruefungsleistung.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public final class LocationContract {
    private LocationContract() {}

    public static class Location implements BaseColumns {
        public static final String TABLE_NAME = "location";

        public static final String COLUMN_NAME_CELL_ID = "cell";
        public static final String COLUMN_NAME_LAC = "lac";
        public static final String COLUMN_NAME_IMAGE_SRC = "image";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_GPS_DATA = "gps";
    }
}
