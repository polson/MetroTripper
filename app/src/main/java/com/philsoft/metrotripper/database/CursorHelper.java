package com.philsoft.metrotripper.database;

import android.database.Cursor;

import java.util.Date;


public class CursorHelper {

    Cursor cursor;


    public CursorHelper(Cursor cursor) {
        this.cursor = cursor;
    }

    public String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    public long getLong(String columnName) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
    }

    public int getInteger(String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    public boolean getBoolean(String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName)) > 0;
    }

    public Double getDouble(String columnName) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(columnName));
    }

    public Date getDate(String columnName) {
        long time = cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
        if (time > 0) {
            return new Date(time);
        }
        return null;
    }
}
