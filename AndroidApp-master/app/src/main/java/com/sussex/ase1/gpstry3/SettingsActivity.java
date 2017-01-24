package com.sussex.ase1.gpstry3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by User on 14/10/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    private TextView gpsUpdateSec, cloudUpdateSec;
    private Button settingsButton;

    private double lat;
    private double lon;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.i("SETTINGSSJD", "Start of Settings");

        gpsUpdateSec = (TextView) findViewById(R.id.gpsUpdateSeconds);     //Seconds between GPS Location Updates
        cloudUpdateSec = (TextView) findViewById(R.id.cloudUpdateSeconds); // Seconds between Cloud Location Updates
        settingsButton  = (Button) findViewById(R.id.settingsButton);      //button to update settings

        Log.i("SETTINGSSJD", "End of Settings");



    } //end onCreate

    public void updateSettings(View view)
    {

        final GlobalVariables globalVariable = (GlobalVariables) getApplicationContext();
        String aa = gpsUpdateSec.getText().toString();
        globalVariable.setGPSUpdateSec(Integer.valueOf(gpsUpdateSec.getText().toString()).intValue());
        globalVariable.setCloudUpdateSec(Integer.valueOf(cloudUpdateSec.getText().toString()).intValue());
     }
}