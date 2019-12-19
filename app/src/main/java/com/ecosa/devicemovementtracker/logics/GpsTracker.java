package com.ecosa.devicemovementtracker.logics;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ecosa.devicemovementtracker.interfaces.LocationChanged;


public class GpsTracker extends Service implements LocationListener {
    private static final String TAG = "GpsTracker";
//    Context context;
    LocationChanged delegate = null;
    boolean hasGps;
    boolean isGpsAvailable;
    //    private Location location;
    LocationManager locationManager;
    String provider;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    public boolean canGetLocation = false;

    Location location; // location
    double latitude = 49.27675;
    double longitude = -123.114193;
    private final Context mContext;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1 / 1000; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1; // 1 minute

    public GpsTracker(Context context) {
//        this.delegate = delegate;
        this.mContext = context;
        checkForGPS();
//        getLocation();
    }

    private void checkForGPS() {
        this.hasGps = mContext.getPackageManager().hasSystemFeature("android.hardware.location.gps");
        if (this.hasGps) {
            getLocation();
        }
    }


    public void reQuestLocationUpdates() {
        if (this.locationManager != null) {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0.0f, this);
        }
    }


    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }



    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onLocationChanged(Location location) {
        delegate.locationChanged(location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }


}
