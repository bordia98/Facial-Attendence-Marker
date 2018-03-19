package com.example.dhp.codefundo;

/**
 * Created by Suyash on 19-03-2018.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Database helper for Pets app. Manages database creation and version management.
 */
public class AttendanceDbHelper extends SQLiteOpenHelper {

    Context context;
    static String SQL_CREATE_PETS_TABLE;

    /** Name of the database file */
    private static final String DATABASE_NAME = "AttendenceMarker.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link AttendanceDbHelper}.
     *
     * @param context of the app
     */
    public AttendanceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
//        Log.v("Batch table name", BatchEntry.TABLE_NAME);
        // Create a String that contains the SQL statement to create the pets table
        SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + BatchEntry.TABLE_NAME + " ("
                + BatchEntry.rollNumber + " TEXT PRIMARY KEY, "
                + BatchEntry.studentName + " TEXT NOT NULL, "
                + BatchEntry.markedAttendence + " INTEGER, "
                + BatchEntry.totalAttendence + " INTEGER);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
//        Log.v("AttendanceDbHelper", "Table created");
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}