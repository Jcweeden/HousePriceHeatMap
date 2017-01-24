package com.sussex.ase1.gpstry3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Steve Dixon on 12/10/2016.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "locationDB.db";

    private static final String TABLE_LOCATIONS = "locations";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PHONE_ID = "phoneid";
    private static final String COLUMN_LOCKEY = "lockey";
    private static final String COLUMN_LATTITUDE = "lattitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP  = "timestamp";

    private static final String TABLE_LOG = "auditlog";
    private static final String COLUMN_IDL = "_id";
    private static final String COLUMN_TIME = "logtime";
    private static final String COLUMN_TYPE = "logtype";
    private static final String COLUMN_MESSAGE = "logmessage";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "(" +
                                        COLUMN_ID + " INTEGER PRIMARY KEY," +
                                        COLUMN_LOCKEY + " STRING," +
                                        COLUMN_PHONE_ID + " STRING," +
                                        COLUMN_LATTITUDE + " DOUBLE," +
                                        COLUMN_LONGITUDE + " DOUBLE," +
                                        COLUMN_TIMESTAMP + " DATETIME" + ")";

        String CREATE_LOG_TABLE       = "CREATE TABLE " + TABLE_LOG + "(" +
                                        COLUMN_IDL + " INTEGER PRIMARY KEY," +
                                        COLUMN_TIME + " DATETIME," +
                                        COLUMN_TYPE + " INT," +
                                        COLUMN_MESSAGE + " STRING" + ")";

        db.execSQL(CREATE_LOCATIONS_TABLE);
        db.execSQL(CREATE_LOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
        onCreate(db);
    }

    public boolean addLocation(Location location, String phoneID) {
        long inserted = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PHONE_ID, phoneID);
            values.put(COLUMN_LOCKEY, UUID.randomUUID().toString());
            values.put(COLUMN_LATTITUDE, location.getLatitude());
            values.put(COLUMN_LONGITUDE, location.getLongitude());
            values.put(COLUMN_TIMESTAMP, dateFormat.format(date));
            SQLiteDatabase db = this.getWritableDatabase();
            inserted = db.insert(TABLE_LOCATIONS, null, values);
            db.close();
        } catch (Exception e) {
            Log.w("TAG_NAME", e.getMessage());
            return false;
        }
        if (inserted == 1)
            return true;
        else
            return false;
    }

    public int getLocalCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("TAG_NAME",db.toString());
//        String query = "SELECT count(*) FROM " + TABLE_LOCATIONS;
//        Cursor cursor = db.rawQuery(query, null);
//        int cnt = cursor.getCount();
        int count = db.rawQuery("SELECT * FROM " + TABLE_LOCATIONS, null).getCount();

        db.close();

        return count;
   //     for( int i=0 ; i< totalColumn ; i++ )
     //   {
   //         if( cursor.getColumnName(i) != null ) {
   //             try {
//                    if (cursor.getString(i) != null) {
//                        Log.w("TAG_NAME", cursor.getString(i));
//                    } else {
//                        Log.w("TAG_NAME", "NOTHING");
//                    }
//                } catch (Exception e) {
//                    Log.w("TAG_NAME", e.getMessage());
//                }
//            }
//        }
//        cursor.close();
//        return cnt;
    }

    public JSONArray getJSONArray() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATIONS;
        Cursor cursor = db.rawQuery(query, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ; i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            Log.w("TAG_NAME", cursor.getString(i) );
                            rowObject.put(cursor.getColumnName(i) , cursor.getString(i) );
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) , "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        Log.w("TAG_NAME", resultSet.toString() );
        boolean allOk = deleteLocations(cursor, db);
        cursor.close();
        return resultSet;
    }

    public List<Location> getListArray()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATIONS;
        Cursor cursor = db.rawQuery(query, null);
        List<Location> list = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            int totalColumn = cursor.getColumnCount();
            //JSONObject rowObject = new JSONObject();
            Location rowLocation = new Location();

            for( int i=0 ; i< totalColumn ; i++ )
            {
                if(cursor.getColumnName(i).equals("phoneid"))
                {
                    try
                    {
                        rowLocation.setId_user(cursor.getString(i));
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
                if(cursor.getColumnName(i).equals("lockey"))
                {
                    try
                    {
                        rowLocation.setId_location(cursor.getString(i));
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
                if(cursor.getColumnName(i).equals("lattitude"))
                {
                    try
                    {
                        rowLocation.setLatitude(Double.parseDouble(cursor.getString(i)));
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
                if(cursor.getColumnName(i).equals("longitude"))
                {
                    try
                    {
                        rowLocation.setLongitude(Double.parseDouble(cursor.getString(i)));
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
                if(cursor.getColumnName(i).equals("timestamp"))
                {
                    try
                    {
                        rowLocation.setTimeStamp(cursor.getString(i));
                    }
                    catch( Exception e )
                    {
                        Log.w("TAG_NAME", e.getMessage() );
                    }
                }
            }

            list.add(rowLocation);
            cursor.moveToNext();
        }

        boolean allOk = deleteLocations(cursor, db);
   //     cursor.close();
        return list;
    }

    public boolean deleteLocations(Cursor cursor, SQLiteDatabase db) {
        int count = cursor.getCount();
        int deleted = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int delflag = db.delete(TABLE_LOCATIONS, COLUMN_ID + " = ?", new String[] { String.valueOf(cursor.getInt(0))});
      //      deleted = deleted + delflag;
            cursor.moveToNext();
        }
        if (count == deleted)
            return true;
        else
            return false;
    }

    public boolean addLog(int type, String message) {
        long inserted = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TIME, dateFormat.format(date));
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_MESSAGE, message);

            SQLiteDatabase db = this.getWritableDatabase();
            inserted = db.insert(TABLE_LOG, null, values);
            Log.e("SQL", "Log record inserted" + inserted);
            db.close();

        } catch (Exception e) {
            Log.w("TAG_NAME", e.getMessage());
            return false;
        }

        if (inserted == 1)
            return true;
        else
            return false;
    }
}
