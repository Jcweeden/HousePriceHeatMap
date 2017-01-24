package com.sussex.ase1.gpstry3;


import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class maintains the behavior of the map
 * Created by User on 13/10/2016.
 */

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Consts
    private final int ROWS_PER_TRANSACTION=4;
    private final int ROWS_MAX_LOCAL = 8;

    // Map's variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView mTextViewLatitude;
    private TextView mTextViewLongitude;

    // Cloud AWS variables
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient dynamoClient;

    // Aux variables
    private final int REQUEST_RESULT = 666;
    private float lat;
    private float lon;
    private boolean auxInit;
    private int countSavingCloud;
    private List<Location> arrayLocationsCloud = new ArrayList<>();



    //private LocationManager locationManager;
    //private android.location.LocationListener listener;
    //private TextView t;
    //private Button b;
    //private static final int REQUEST_LOCATION_CODE = 2;


    // Initial method
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Set global references for the latitude and longitude values
        mTextViewLatitude = (TextView) findViewById(R.id.lblLatitude);
        mTextViewLongitude = (TextView) findViewById(R.id.lblLongitude);

        // Checks for permission granted: ACCESS_FINE_LOCATION
        // It allows use of phone location feature
        if (!checkPermission("android.permission.ACCESS_FINE_LOCATION"))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_FINE_LOCATION"))
                showExplanation("Permission Needed please!", "Rationale", "android.permission.ACCESS_FINE_LOCATION",REQUEST_RESULT);
            else
                requestPermission("android.permission.ACCESS_FINE_LOCATION",REQUEST_RESULT);
        }
        else
        {
            //Toast.makeText(this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
            requestPermission("android.permission.ACCESS_FINE_LOCATION",REQUEST_RESULT);
        }

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-west-2:f7e2ae4e-296a-4c7e-b1cb-c6fb371db3d8", // Identity Pool ID - UsersPoolWinter - GJJP
                Regions.US_WEST_2 // Region
        );
        dynamoClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        mapper = new DynamoDBMapper(dynamoClient);


//        t = (TextView) findViewById(R.id.textView); //textView to display coordinates
//        b = (Button) findViewById(R.id.cmdLocation);     //button to update coordinates
//        checkPermission();
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        configure_button();
    }

    // Trigger for complete map built
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    // Checks permission for the selected permission string
    private boolean checkPermission(String permission)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    // Shows the permission request explanation
    private void showExplanation(String title, String message, final String permission,  final int permissionRequestCode)
    {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    // Request the selected permission to the OS
    private void requestPermission(String permissionName, int permissionRequestCode)
    {
        ActivityCompat.requestPermissions(this, new String[]{permissionName}, permissionRequestCode);
    }

    // Overridden to get the permission request result for the selected permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case REQUEST_RESULT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    setupLocationRequest();
                else
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Set-up the location request properties
    public synchronized void setupLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);                                         // location update interval
        mLocationRequest.setFastestInterval(5000);                                  // location update interval in ideal conditions
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);       //is more likely to use GPS than WiFi
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // Auxiliary GoogleApiClient class for update location listeners
    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks()
    {
        @Override
        public void onConnected(Bundle bundle)
        {
            Log.i("onConnected()", "start");
            try
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,mLocationListener);
            }
            catch (SecurityException e)
            {
                Log.i("onConnected()","SecurityException:"+e.getMessage());
            }
        }
        @Override
        public void onConnectionSuspended(int i)
        {
            Log.i("onSuspended","connection suspended");
        }
    };

    // Auxiliary GoogleApiClient class for  failed connections
    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient. OnConnectionFailedListener()
    {
        @Override
        public void onConnectionFailed( ConnectionResult connectionResult)
        {
            Toast.makeText(LocationActivity.this, connectionResult.toString(), Toast.LENGTH_LONG).show();
            Log.i("onConnected()", "SecurityException: " + connectionResult.toString());
        }
    };

    // Listener for location updates
    com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener()
    {
        // This code is executed each time the location changes
        @Override
        public void onLocationChanged(android.location.Location location)
        {
            if (location != null)
            {
                lat = (float)location.getLatitude();
                lon = (float)location.getLongitude();
                mTextViewLatitude.setText(String.format("%1$.4f",location.getLatitude()));
                mTextViewLongitude.setText(String.format("%1$.4f",location.getLongitude()));

                //insert the coordinates in the array to later upload it to the cloud
                Location oLocation = new Location();
                oLocation.setId_location(UUID.randomUUID().toString());
                oLocation.setId_user(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                oLocation.setLatitude(location.getLatitude());
                oLocation.setLongitude(location.getLongitude());
                arrayLocationsCloud.add(oLocation);
                countSavingCloud++;

                // removes firsts location to liberate memory this code is just useful when
                // was not possible upload the default number of items to the cloud then they start
                // to accumulate
                List<Location> deletedItems = new ArrayList<>();
                if(arrayLocationsCloud.size() == ROWS_MAX_LOCAL)
                {
                    for(int i = 0; i < ROWS_MAX_LOCAL - ROWS_PER_TRANSACTION; i++)
                        deletedItems.add(arrayLocationsCloud.get(i));
                    for (Location item:deletedItems)
                        arrayLocationsCloud.remove(item);
                }

                // executes the cloud saving
                if(countSavingCloud >= ROWS_PER_TRANSACTION && CheckInternetAccessStatus())
                {
                    new DynamoDBManagerTask().execute(arrayLocationsCloud);
                    countSavingCloud = 0;
                    // clear all locations array
                    arrayLocationsCloud.clear();
                }

                // Set marker on map
                if(!auxInit)
                {
                    CurrentLocation();
                    auxInit = true;
                }
                else
                    drawMarker();
            }
        }
    };

    // Checks if internet access is enabled
    private boolean CheckInternetAccessStatus()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        else
        {
            Toast.makeText(this, "Internet access is not enabled in your device, go to settings and enable it first.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // inner class to execute the database update
    private class DynamoDBManagerTask extends AsyncTask<List<Location>, Void, String>
    {
        @Override
        protected String doInBackground(List<Location>... params)
        {
            if(params.length > 0)
                mapper.batchSave(params[0]);
            return null;
        }
    }

    // Display the coordinates on the map
    private void CurrentLocation()
    {
        if(lat!= 0 && lon !=0)
        {
            LatLng currentPosition = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(currentPosition).title("You're here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    // Display the markers
    private void drawMarker()
    {
        //mMap.clear();
        if(lat!= 0 && lon !=0)
        {
            LatLng currentPosition = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(currentPosition).title("You're here!"));
        }
    }

    // Display the coordinates and show the marker on the map
    public void onClickButtonGetPosition(View view)
    {
        //validate gps turned on, else clear marker and coordinates
        if(!CheckGpsStatus())
        {
            mTextViewLatitude.setText("");
            mTextViewLongitude.setText("");
            lat = 0; lon = 0;
            mMap.clear();
        }

        //validate internet access on, else send message notifying it, keep coordinates
        if(!CheckInternetAccessStatus())
        {
            lat = 0; lon = 0;
            mMap.clear();
        }

        // marks the current location
        CurrentLocation();
    }

    // Checks if GPS is enable, returns false otherwise
    private boolean CheckGpsStatus()
    {
        LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is not enabled in your device, go to settings and enable it first.", Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }
}

