package de.tobbexiv.pruefungsleistung.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

public class GPSLocation extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;

    private boolean enabled = false;
    private boolean running = false;

    private Context context;
    private LocationManager locationManager;
    private Location location;

    public GPSLocation(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;

        startUpdates();
    }

    private void startUpdates() {
        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(enabled
                && !running
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this);
            running = true;
        } else {
            location = null;
        }
    }

    private void updateLocation() {
        startUpdates();

        if(enabled
                && running
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            location = null;
        }
    }

    public void stopUpdating(){
        locationManager.removeUpdates(GPSLocation.this);
        location = null;
        running = false;
    }

    public String getLocationAsString(){
        updateLocation();

        if(location != null){
            return location.getLatitude() + ", " + location.getLongitude();
        }

        return "";
    }

    public boolean gpsEnabled() {
        startUpdates();

        return enabled;
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
