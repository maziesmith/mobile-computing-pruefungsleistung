package de.tobbexiv.pruefungsleistung;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import de.tobbexiv.pruefungsleistung.background.CellStateReceiver;
import de.tobbexiv.pruefungsleistung.background.CellStateService;
import de.tobbexiv.pruefungsleistung.camera.CameraActivity;
import de.tobbexiv.pruefungsleistung.database.LocationDbHelper;
import de.tobbexiv.pruefungsleistung.location.GPSLocation;
import de.tobbexiv.pruefungsleistung.settings.SettingsActivity;
import de.tobbexiv.pruefungsleistung.view.DisplayLocationView;

import de.tobbexiv.pruefungsleistung.database.LocationContract.Location;
import de.tobbexiv.pruefungsleistung.widget.WidgetProvider;

public class MainActivity extends AppCompatActivity {
    public static int CAMERA_ACTIVITY = 4711;

    private static final int REQUEST_APP_PERMISSIONS = 100;
    private static final int REQUEST_BROADCAST_PERMISSIONS = 101;

    private TelephonyManager telephonyManager;
    private LocationDbHelper dbHelper;
    private GPSLocation gpsLocation;

    private Map<String, String> newPosition;
    private DisplayLocationView selectedView;

    LinearLayout content_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addNew = (FloatingActionButton) findViewById(R.id.add);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddNewPosition();
            }
        });

        content_main = (LinearLayout) findViewById(R.id.content_main);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        gpsLocation = new GPSLocation(this, (LocationManager) getSystemService(LOCATION_SERVICE));

        dbHelper = new LocationDbHelper(this);
        for (Map<String, String> location : dbHelper.getAllLocations()) {
            addLocationToView(location);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE }, REQUEST_BROADCAST_PERMISSIONS);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateService(preferences);

        preferences.registerOnSharedPreferenceChangeListener(
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                updateService(preferences);
            }
        });
    }

    private void updateService(SharedPreferences preferences) {
        if(preferences.getBoolean("backgroundJob", false)) {
            startService();
        } else {
            stopService();
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, CellStateService.class);
        startService(serviceIntent);
    }

    private void stopService() {
        Intent stopIntent = new Intent(MainActivity.this, CellStateService.class);
        stopService(stopIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(selectedView != null) {
            selectedView.redrawImage();
            updateWidget();
            selectedView = null;
        }

        if (newPosition == null) {
            return;
        }

        if(requestCode == CAMERA_ACTIVITY) {
            if(resultCode == RESULT_OK) {
                newPosition.put(Location.COLUMN_NAME_IMAGE_SRC, data.getStringExtra("FILE_PATH"));
                persistNewPosition();
                addLocationToView(newPosition);
                updateWidget();
                newPosition = null;
            } else {
                Toast.makeText(this, R.string.toast_need_image, Toast.LENGTH_LONG).show();
                newPosition = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_APP_PERMISSIONS:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, R.string.toast_need_all_permissions, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                startAddNewPosition();
                return;
            case REQUEST_BROADCAST_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.toast_need_permission_for_background, Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(getIntent().getBooleanExtra("NEW_CELL", false)) {
            startAddNewPosition();
        }
    }



    @Override
    protected void onDestroy() {
        gpsLocation.stopUpdating();
        dbHelper.close();

        super.onDestroy();
    }

    private void persistNewPosition() {
        String id = dbHelper.insertLocation(
                newPosition.get(Location.COLUMN_NAME_CELL_ID),
                newPosition.get(Location.COLUMN_NAME_LAC),
                newPosition.get(Location.COLUMN_NAME_DESCRIPTION),
                newPosition.get(Location.COLUMN_NAME_IMAGE_SRC),
                newPosition.get(Location.COLUMN_NAME_GPS_DATA)
        );
        newPosition.put(Location._ID, id);
    }

    private void addLocationToView(Map<String, String> location) {
        final DisplayLocationView dls = new DisplayLocationView(
                this,
                location.get(Location._ID),
                location.get(Location.COLUMN_NAME_CELL_ID),
                location.get(Location.COLUMN_NAME_LAC),
                location.get(Location.COLUMN_NAME_DESCRIPTION),
                location.get(Location.COLUMN_NAME_IMAGE_SRC),
                location.get(Location.COLUMN_NAME_GPS_DATA)
        );
        dls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedView = dls;
                showViewOption();
            }
        });
        content_main.addView(dls);
    }

    private void startAddNewPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_APP_PERMISSIONS);
            return;
        }

        newPosition = new HashMap<>();
        if(!getCellInformation()) {
            newPosition = null;
            updateWidget();
            return;
        }
        if(dbHelper.getLocationByCellInfo(newPosition.get(Location.COLUMN_NAME_CELL_ID), newPosition.get(Location.COLUMN_NAME_LAC)) != null) {
            Toast.makeText(this, R.string.toast_position_exists, Toast.LENGTH_LONG).show();
            newPosition = null;
            updateWidget();
            return;
        }
        if(!getGpsInformation()) {
            newPosition = null;
            updateWidget();
            return;
        }
        getDescription();
    }

    private boolean getCellInformation() {
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        if(cellLocation == null) {
            Toast.makeText(this, R.string.toast_cell_info_not_available, Toast.LENGTH_LONG).show();
            return false;
        }

        newPosition.put(Location.COLUMN_NAME_CELL_ID, cellLocation.getCid() + "");
        newPosition.put(Location.COLUMN_NAME_LAC, cellLocation.getLac() + "");

        return true;
    }

    private boolean getGpsInformation() {
        if(gpsLocation.gpsEnabled()) {
            String gpsPosition = gpsLocation.getLocationAsString();

            if(gpsPosition == "") {
                Toast.makeText(this, R.string.toast_gps_no_data, Toast.LENGTH_LONG).show();
                return false;
            }

            newPosition.put(Location.COLUMN_NAME_GPS_DATA, gpsPosition);
            return true;
        } else {
            Toast.makeText(this, R.string.toast_gps_not_started, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void getDescription() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_description_title)
                .setMessage(R.string.dialog_description_message)
                .setView(input)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newPosition.put(Location.COLUMN_NAME_DESCRIPTION, input.getText().toString());
                        startCamera(newPosition.get(Location.COLUMN_NAME_CELL_ID), newPosition.get(Location.COLUMN_NAME_LAC));
                    }
                })
                .show();
    }

    private void startCamera(String cellId, String lac) {
        String fileName = cellId + "_" + lac;

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("FILE_NAME", fileName);
        startActivityForResult(intent, CAMERA_ACTIVITY);
    }

    private void showViewOption() {
        CharSequence options[] = new CharSequence[] {
                getText(R.string.dialog_options_selection_update_description),
                getText(R.string.dialog_options_selection_update_image),
                getText(R.string.dialog_options_selection_delete),
                getText(R.string.dialog_options_selection_cancel)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_options_title)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Update description
                                updateDescription();
                                return;
                            case 1: // Update image
                                updateImage();
                                return;
                            case 2: // Delete
                                deleteLocation();
                                return;
                            case 3: // Cancel
                                selectedView = null;
                                return;
                        }
                    }
                })
                .show();
    }

    private void updateDescription() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_description_title)
                .setMessage(R.string.dialog_description_message)
                .setView(input)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.updateLocation(selectedView.getLocationId(), input.getText().toString());
                        selectedView.updateLocationDescription(input.getText().toString());
                        updateWidget();
                        selectedView = null;
                    }
                })
                .show();
    }

    private void updateImage() {
        startCamera(selectedView.getCellId(), selectedView.getLac());
    }

    private void deleteLocation() {
        dbHelper.deleteLocation(selectedView.getLocationId());
        content_main.removeView(selectedView);
        updateWidget();
        selectedView = null;
    }

    private void updateWidget() {
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        Map<String, String> location = null;

        if(cellLocation != null) {
            String cellId = cellLocation.getCid() + "";
            String lac = cellLocation.getLac() + "";

            location = dbHelper.getLocationByCellInfo(cellId, lac);
        }
        WidgetProvider.setLocationInformation(this, location);
    }
}
