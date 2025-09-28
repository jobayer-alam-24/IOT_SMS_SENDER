package com.example.iotsmssender;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "iot_sms.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_LOG = "gas_detection_log";
    public static final String COL_ID = "id";
    public static final String COL_ALARM_TYPE = "alarm_type";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_YEAR = "year";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_LOG + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ALARM_TYPE + " TEXT, "
                + COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + COL_YEAR + " INTEGER"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
        onCreate(db);
    }

    // Insert detection with year
    public void insertDetection(String alarmType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ALARM_TYPE, alarmType);

        // Get current year
        int year = Calendar.getInstance().get(Calendar.YEAR);
        values.put(COL_YEAR, year);

        db.insert(TABLE_LOG, null, values);
        db.execSQL("DELETE FROM " + TABLE_LOG + " WHERE " + COL_TIMESTAMP + " <= datetime('now','-7 days')");
        db.close();
    }


    public List<Map<String, String>> fetchDetections() {
        List<Map<String, String>> detectionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COL_ALARM_TYPE + ", " + COL_TIMESTAMP + ", " + COL_YEAR +
                " FROM " + TABLE_LOG +
                " ORDER BY " + COL_TIMESTAMP + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        String lastType = "";
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALARM_TYPE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(COL_YEAR));

                if (!type.equals(lastType)) {
                    Map<String, String> row = new HashMap<>();

                    // Split date and time
                    String date = timestamp.split(" ")[0]; // yyyy-MM-dd
                    String time24 = timestamp.split(" ")[1]; // HH:mm:ss

                    // Convert to 12-hour format
                    String time12 = time24;
                    try {
                        java.text.SimpleDateFormat _24HourFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                        java.text.SimpleDateFormat _12HourFormat = new java.text.SimpleDateFormat("hh:mm a");
                        java.util.Date parsedTime = _24HourFormat.parse(time24);
                        time12 = _12HourFormat.format(parsedTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    row.put("type", type);
                    row.put("date", date);
                    row.put("time", time12);
                    row.put("year", String.valueOf(year));
                    detectionList.add(row);

                    lastType = type;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return detectionList;
    }
}