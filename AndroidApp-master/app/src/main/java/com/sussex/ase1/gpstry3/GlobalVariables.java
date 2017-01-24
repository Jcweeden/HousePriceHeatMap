package com.sussex.ase1.gpstry3;

import android.app.Application;

/**
 * Created by Steve Dixon on 14/10/2016.
 */

public class GlobalVariables extends Application
{

    private int gpsUpdateSec;
    private int cloudUpdateSec;


    public int getGPSUpdateSec() {

        return gpsUpdateSec;
    }

    public void setGPSUpdateSec(int gpsUpdateSec) {

        this.gpsUpdateSec = gpsUpdateSec;

    }

    public int getCloudUpdateSec() {

        return cloudUpdateSec;
    }

    public void setCloudUpdateSec(int cloudUpdateSec) {

        this.cloudUpdateSec = cloudUpdateSec;
    }

}


