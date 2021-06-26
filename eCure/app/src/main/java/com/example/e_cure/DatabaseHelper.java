package com.example.e_cure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {


    //constructor
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static final String DATABASE_NAME = "ECURE.db";
    public static final String PATIENT_LOCATION = "patient_location_table";
    public static final String LATITUDE = "latitude"; //Column names
    public static final String LONGITUDE = "longitude";
    public static final String LOCALITY = "locality";
    public static final String USER_ID = "user_id";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + PATIENT_LOCATION
                + "(" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LATITUDE +" DECIMAL," + LONGITUDE + " DECIMAL," + LOCALITY+ " STRING)";

        db.execSQL(createTableStatement);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ PATIENT_LOCATION);
        onCreate(db);
    }

    public boolean insertData(double latitude, double longitude, String locality) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(LATITUDE, latitude);
        cv.put(LONGITUDE, longitude);
        cv.put(LOCALITY, locality);

        long result = db.insert(PATIENT_LOCATION,null,cv);

        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+PATIENT_LOCATION,null);

        return result;
    }

    public boolean updateData(String id, double latitude, double longitude, String locality)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(USER_ID,id);
        cv.put(LATITUDE, latitude);
        cv.put(LONGITUDE, longitude);
        cv.put(LOCALITY, locality);

        db.update(PATIENT_LOCATION, cv, "ID=?", new String[]{id});
        return true;
    }

    public Integer deleteData(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PATIENT_LOCATION,"ID=?",new String[]{id});
    }
}

