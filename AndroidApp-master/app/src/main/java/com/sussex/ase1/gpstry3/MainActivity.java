package com.sussex.ase1.gpstry3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputFilter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView postcode;
    Button mapButton;
    private DBHandler db;


    // Aux variables
    private final int REQUEST_RESULT = 666;
    public LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private double lat;
    private double lon;




    public Context context;
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_main);
//        final GlobalVariables globalVariable = (GlobalVariables) getApplicationContext();
        postcode = (TextView) findViewById(R.id.postcode);      //Seconds between GPS Location Updates
        mapButton  = (Button) findViewById(R.id.mapButton);          //button to update settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        Intent serviceIntent = new Intent(getApplicationContext(), RemoteService.class);
//        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
     //   remoteService = new RemoteService(MainActivity.this);

        ((EditText)findViewById(R.id.postcode)).setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        db = new DBHandler(this, null, null, 1);
        db.addLog(1, "Running Task 4");

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
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i("SETTINGSSJD", "Starting Settings Intent");
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            Log.i("SETTINGSSJD", "After Settings Intent Created");
            startActivity(settingsIntent);
            Log.i("SETTINGSSJD", "After Starting Settings Intent");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.location) {
            Intent mapIntent = new Intent(this, LocationActivity.class);
            db = new DBHandler(this, null, null, 1);
            db.addLog(1, "Running Task 2");
            startActivity(mapIntent);

        } else if (id == R.id.task3) {
            db = new DBHandler(this, null, null, 1);
            db.addLog(1, "Running Task 3");
            Intent task3Intent = new Intent(this, Task3Activity.class);
            startActivity(task3Intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onClick(View arg0) {
        String pcode = postcode.getText().toString();
        if (!validPostcode(pcode)) {
            Toast.makeText(this, "Postcode format invalid. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent webIntent = new Intent(context, WebViewActivity.class);
        webIntent.putExtra("typeFind", "P");
        webIntent.putExtra("postcode", pcode);
        startActivity(webIntent);
    }

    public void onClick2(View arg0) {

        //validate gps turned on, else clear marker and coordinates
        if(!CheckGpsStatus()) {
            lat = 0;
            lon = 0;
        }

        //validate internet access on, else send message notifying it, keep coordinates
        if(!CheckInternetAccessStatus()) {
            lat = 0;
            lon = 0;
        }

        Intent webIntent = new Intent(context, WebViewActivity.class);
        webIntent.putExtra("typeFind", "L");
        webIntent.putExtra("latitude", String.valueOf(lat));
        webIntent.putExtra("longitude", String.valueOf(lon));
        mLocationRequest = null;
        mGoogleApiClient.disconnect();
        startActivity(webIntent);
    }

    public boolean testMock() {
        return true;
    }

    public boolean validPostcode(String postcode){
    //              valid postcode formats    AA9A 9AA  |  A9A 9AA   |  A9 9AA  |  A99 9AA   |  AA9 9AA   |  AA99 9AA
    //  ("(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKPSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})")

        if (postcode == null){
            Toast.makeText(this, "Postcode is empty. Try again", Toast.LENGTH_SHORT).show();
            return false;
        }
//      ("(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKPSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})"))

        String[] pArray = postcode.toUpperCase().trim().split(" ");
        String areaDistrict = pArray[0];

        String sectorUnit = "";
        if (pArray.length == 2)
             sectorUnit = pArray[1];

        String pMatch = "";

        switch (areaDistrict.length()) {
            case 1:
                pMatch = "[A-Z&&[^QVX]]";
                break;
            case 2:
                pMatch = "([A-Z&&[^QVX]][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]])";
                break;
            case 3:
                pMatch = "(([A-Z&&[^QVX]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9])|([A-Z&&[^QVX]][0-9][A-HJKPSTUW]))";
                break;
            case 4:
                pMatch = "(([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][ABEHMNPRVWXY]))";
                break;
            default: ;
        }

        if (!areaDistrict.matches(pMatch)) {
           return false;
        }

        switch (sectorUnit.length()) {
            case 1:
                pMatch = "[0-9]";
                break;
            case 2:
                pMatch = "([0-9][A-Z&&[^CIKMOV]])";
                break;
            case 3:
                pMatch = "([0-9][A-Z&&[^CIKMOV]]{2})";
                break;
            default: ;
        }

        if (sectorUnit != "" && !sectorUnit.matches(pMatch)) {
            return false;
        }

        return true;
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
    // The current location is updated each 5 minutes = 300000ms
    public synchronized void setupLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000);                                         // location update interval
        mLocationRequest.setFastestInterval(300000);                                  // location update interval in ideal conditions
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
            Toast.makeText(MainActivity.this, connectionResult.toString(), Toast.LENGTH_LONG).show();
            Log.i("onConnected()", "SecurityException: " + connectionResult.toString());
        }
    };

    boolean initial = false;

    // Listener for location updates
    com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener()
    {
        // This code is executed each time the location changes
        @Override
        public void onLocationChanged(android.location.Location location)
        {
            if (location != null)
            {
                lat = location.getLatitude();
                lon = location.getLongitude();

                location.getLatitude();
                location.getLongitude();

                if(initial) {
                    //current location update
                    Intent webIntent = new Intent(context, WebViewActivity.class);
                    webIntent.putExtra("typeFind", "L");
                    webIntent.putExtra("latitude", String.valueOf(lat));
                    webIntent.putExtra("longitude", String.valueOf(lon));
                    startActivity(webIntent);
                }
                initial = true;
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