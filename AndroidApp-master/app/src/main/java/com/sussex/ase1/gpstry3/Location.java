package com.sussex.ase1.gpstry3;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.sql.Date;

/**
 * Created by User on 12/10/2016.
 */

@DynamoDBTable(tableName = "Location")
public class Location {

    String id_location;
    String id_user;
    double latitude;
    double longitude;
    String timeStamp;

    Location(){}

    Location(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    Location(double latitude, double longitude, String id, String id2, String timetime)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id_location = id;
        this.id_user = id2;
        this.timeStamp = timetime;
    }

    public int test() {
        return 5;
    }

    @DynamoDBHashKey (attributeName = "id_location")
    public String getId_location() {
        return id_location;
    }

    public void setId_location(String id_Location) {
        this.id_location = id_Location;
    }

    @DynamoDBRangeKey (attributeName = "id_user")
    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    @DynamoDBAttribute (attributeName = "latitude")
    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute (attributeName = "longitude")
    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute (attributeName = "timeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}