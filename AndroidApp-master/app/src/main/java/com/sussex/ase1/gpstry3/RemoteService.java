package com.sussex.ase1.gpstry3;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Steve Dixon on 13/10/2016.
 */

public class RemoteService extends Service {

    final Messenger myMessenger = new Messenger(new com.sussex.ase1.gpstry3.RemoteService.IncomingHandler());

    private Context mContext;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    android.location.Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    Activity activity;

    private static final int REQUEST_LOCATION_CODE = 2;
    private android.location.LocationListener listener;

  //  public RemoteService(Context remoteService) {
    public RemoteService() {
        Log.i("AAAAAA","RemoteService Constructor Start");
        mContext = this;
        activity = new Activity();
        android.location.Location getLocation  = getLocation();
        Log.i("AAAAAA","RemoteService Constructor End");
    }

  //  public RemoteService(Context context, Activity activity) {
    //    this.mContext = context;
      //  this.activity = activity;
        //getLocation();
   // }

    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            int gpsUpdateSec = data.getInt("gpsUpdateSec");
            int cloudUpdateSec = data.getInt("cloudUpdateSec");


            Log.i("AAAAAA", "RemoteService gpsUpdateSec = " + gpsUpdateSec);
            Log.i("AAAAAA", "RemoteService cloudUpdateSec = " + cloudUpdateSec);

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        getLocation();
        Log.i("AAAAAA", "RemoteService IBinder sent");
        return myMessenger.getBinder();
    }

    public android.location.Location getLocation()
    {
        Log.i("AAAAAA","getLocation start");
        try {

            checkPermission();

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            listener = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    Log.i("AAAAAA","Coordinates:" + "lat: " + lat + " lon:" + lon);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                }
            };








    /*        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            Log.i("AAAAAA","1");
            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.i("AAAAAA","2");
                // No network provider is enabled
            } else {
                this.canGetLocation = true;
                Log.i("AAAAAA","3");
                if (isNetworkEnabled) {
                    while(true) {
                        int requestPermissionsCode = 50;

                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);

                        Log.d("AAAAAA", "4");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            Log.i("AAAAAA", "5");
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.i("AAAAAA", "Latitude = " + latitude);
                                Log.i("AAAAAA", "Longitude = " + longitude);
                                locationManager.requestLocationUpdates("gps", 1000, 0, mLocationListener);
                            }
                        }

                    }
                }
            }
            Log.i("AAAAAA","6");
            // If GPS enabled, get latitude/longitude using GPS Services
            if (isGPSEnabled) {
                Log.i("AAAAAA","7");
                if (location == null) {
                    Log.i("AAAAAA","8");
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 50);
                        Log.i("AAAAAA","9");
                    } else {
                        Log.i("AAAAAA","10");
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                        Log.d("AAAAAA", "GPS Enabled");
                        if (locationManager != null) {
                            Log.i("AAAAAA","11");
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                            Log.i("AAAAAA","12");
                        }
                    }
                }

            }  */
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     */
    public void stopUsingGPS() {

    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final android.location.Location location) {

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.i("AAAAAA","latitude = " + latitude);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
    };

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }


    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void checkPermission()
    {
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION ))
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage("Permission to access the Location is required for this app to run.").setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        makeRequest();
                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                makeRequest();
            }
        }
    }

    protected void makeRequest()
    {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
    }


}


