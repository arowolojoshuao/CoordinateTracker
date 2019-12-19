package com.ecosa.devicemovementtracker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.ecosa.devicemovementtracker.R;
import com.ecosa.devicemovementtracker.Util.NetworkConnectionUtil;
import com.ecosa.devicemovementtracker.database.Coordinate;
import com.ecosa.devicemovementtracker.database.RoomRepository;
import com.ecosa.devicemovementtracker.logics.GpsTracker;
import com.ecosa.devicemovementtracker.logics.LocationTracker;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DeviceTracker";
    private static final int REQUEST_LOCATION = 100;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 13112;
    private static final int REQUEST_CHECK_SETTINGS = 13119;
    LocationTracker tracker;
    double initialLatitude = 0.0d, initiallongitude = 0.0d;
    double destLatitude = 0.0d, destLongitude = 0.0d;
    LatLng latLng;
    SupportMapFragment mapFragment;
    boolean isAnnonymousChecked = false;
    private Double[] PositionA = new Double[2];
    private Double[] PositionB = new Double[2];
    Boolean isInternetPresent = false;
    Geocoder geocoder;
    List<Address> addresses;
    private int buildSDK;
    private Marker marker;
    private String picName;
    private boolean mLocationPermissionGranted;
    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Marker mMarker;
    private boolean mFirstRender = true;
    public static RoomRepository roomRepository;

    @Bind(R.id.llmMapContainer)
    LinearLayout llmMapContainer;

    @Bind(R.id.llWatchContainer)
    LinearLayout llWatchContainer;

    @Bind(R.id.textAddress)
    TextView textAddress;

    @Bind(R.id.btnStart)
    Button btnStart;

    @Bind(R.id.btnStop)
    Button btnStop;


    @Bind(R.id.btnCalDistance)
    Button btnCalDistance;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        System.gc();
//        Window window = getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ButterKnife.bind(this);

        buildSDK = Build.VERSION.SDK_INT;
        isInternetPresent = NetworkConnectionUtil.isConnectedToInternet(this);
        displayLocationSettingsRequest(MainActivity.this);
        roomRepository = new RoomRepository(getApplication());

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }

                if (mMarker != null) {
                    mMarker.remove();
                }

                for (final Location location : locationResult.getLocations()) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // Toast.makeText(MainActivity.this, "Latitude :" + location.getLatitude() + "\t" + "Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    mMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                            .title("Location Tracker"));

                    mGoogleMap.setMyLocationEnabled(true);
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();
                        //
                        //  textAddress.setText(addresses.get(0).getAddressLine(0));
//                        Toast.makeText(Report.this, "address:\t" + address, Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);


                    if (mFirstRender) {
                        mGoogleMap.animateCamera(cameraUpdate);

                        mFirstRender = false;

                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };

        getPermissions();
    }


    @OnClick({R.id.btnStart, R.id.btnStop, R.id.btnCalDistance})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnStart:

                Toast.makeText(getApplicationContext(), "Start Button Clicked", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: start button clicked... attempting to save to DB");
                btnStop.setEnabled(true);
                btnCalDistance.setEnabled(false);
                btnStart.setEnabled(false);
                //  latLng = new LatLng(location.getLatitude(), location.getLongitude());
                tracker = new LocationTracker(MainActivity.this);
                // check if location is available
                if (tracker.isLocationEnabled) {
                    initialLatitude = tracker.getLatitude();
                    initiallongitude = tracker.getLongitude();

                    saveCoordinateToDatabase(1, "A", initialLatitude, initiallongitude);

                    textAddress.setText("Tracking initiated!");
                    Toast.makeText(MainActivity.this, "Your Initial Location Latitude= " + initialLatitude
                            + "\n & Longitude= " + initiallongitude + "\nSuccessfully saved to db", Toast.LENGTH_LONG).show();

                } else {
                    // show dialog box to user to enable location
                    tracker.askToOnLocation();
                    displayLocationSettingsRequest(MainActivity.this);
                }

                return;


            case R.id.btnStop:

                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnCalDistance.setEnabled(true);

                tracker = new LocationTracker(MainActivity.this);
                // check if location is available
                if (tracker.isLocationEnabled) {
                    destLatitude = tracker.getLatitude();
                    destLongitude = tracker.getLongitude();
                    saveCoordinateToDatabase(2, "B", destLatitude, destLongitude);
                    textAddress.setText("Tracking stopped!");
                    Toast.makeText(MainActivity.this, "Your Destination Location Latitude= " + destLatitude + "\n & Longitude= " + destLongitude + "\nSuccessfully saved to db", Toast.LENGTH_LONG).show();

                } else {
                    // show dialog box to user to enable location
                    tracker.askToOnLocation();
                    displayLocationSettingsRequest(MainActivity.this);
                }

                return;


            case R.id.btnCalDistance:

                fetchCoordinatesFromDatabase();
                // origin = new LatLng(startingLatitude, startingLongitude);
                // LatLng destination = new LatLng(stoppingLatitude, stoppingLongitude);

                LatLng origin = new LatLng(PositionA[0], PositionA[1]);
                LatLng destination = new LatLng(PositionB[0], PositionB[1]);

                Location locationA = new Location("point A");
                locationA.setLatitude(origin.latitude);
                locationA.setLongitude(origin.longitude);

                Location locationB = new Location("point B");
                locationB.setLatitude(destination.latitude);
                locationB.setLongitude(destination.longitude);

                GoogleDirection.withServerKey(getString(R.string.map_direction_key))
                        .from(new LatLng(origin.latitude, origin.longitude))
                        .to(new LatLng(destination.latitude, destination.longitude))
                        .avoid(AvoidType.FERRIES)
                        .avoid(AvoidType.HIGHWAYS)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.isOK()) {

                                    mGoogleMap.addMarker(new MarkerOptions().position(origin));
                                    mGoogleMap.addMarker(new MarkerOptions().position(destination));

                                    ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                    mGoogleMap.addPolyline(DirectionConverter.createPolyline(MainActivity.this, directionPositionList, 5, Color.RED));

                                }

                            }


                            @Override
                            public void onDirectionFailure(Throwable t) {

                            }
                        });


                textAddress.setText("The Distance between Point A and Point B is : " + new DecimalFormat("##.##").format(locationA.distanceTo(locationB)) + "m");


                break;
            default:
                return;
        }
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i(TAG, "All location settings are satisfied.");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == 0) {

                return;
            } else {
                Toast.makeText(this, "The app was not allowed to access your location." +
                        " Please consider granting it this permission", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        return;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000);
        checkLocationAvailability();

    }

    private void checkLocationAvailability() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnCompleteListener(task1 -> {
            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                // All location settings are satisfied.
                if (mLocationPermissionGranted) {
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, null);
                }

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user why this permission is required
                    displayDialog();
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION);
                }
            } else {
                // Permission has already been granted
                mLocationPermissionGranted = true;
                mSupportMapFragment.getMapAsync(this);
            }
        } else {
            // Android SDK Version is below Marshmallow.
            // You don't need runtime permission, Do what you want . . .
            mLocationPermissionGranted = true;
            mSupportMapFragment.getMapAsync(this);
        }
    }


    private void displayDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Location Permission")
                .setMessage("Hi there! We can't show your current location without the" +
                        " location permission, could you please grant it?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), ":(", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).show();
    }


//    private void GetAddress(Location location) {
//        new ApiConnector(this, "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=true", this, false, false, 3).Get();
//    }


    private void formatAddress(Object response) {
        try {
            JSONObject object = new JSONObject(response.toString()).getJSONArray("results").getJSONObject(0);
            JSONArray jsonArray1 = object.getJSONArray("address_components");
            int length = jsonArray1.length();
            String fullAddress = object.getString("formatted_address");
            String country = jsonArray1.getJSONObject(length - 1).getString("long_name");
            String city = jsonArray1.getJSONObject(length - 2).getString("long_name");
            if (marker != null) {
                marker.remove();
            }
            marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(city + ", " + country).snippet(fullAddress));

            textAddress.setText(Html.fromHtml(fullAddress + " , <strong>" + city + " , " + country + "</strong>"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void saveCoordinateToDatabase(int id, String position, double latitude, double longitude) {

        Coordinate coordinate = new Coordinate(id, position, longitude, latitude);
        roomRepository.addCoordinate(coordinate);
        Toast.makeText(MainActivity.this, "coordinate successfully saved to db", Toast.LENGTH_SHORT).show();


    }


    private void fetchCoordinatesFromDatabase() {

        List<Coordinate> coordinates = roomRepository.getAllCoordinates();

        for (Coordinate coordinate : coordinates) {

            int id = coordinate.getId();
            String position = coordinate.getPosition();
            double longitude = coordinate.getLongitude();
            double latitude = coordinate.getLatitude();

            if (id == 1) {
                PositionA[0] = latitude;
                PositionA[1] = longitude;

            } else if (id == 2) {
                PositionB[0] = latitude;
                PositionB[1] = longitude;
            }
        }

    }


}

