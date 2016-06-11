package com.google.sample.cloudvision;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StorageUtils{

    private SQLiteDatabase db;
    private StorageUtilsOpenHelper helper;
    private String[] keys;

    public StorageUtils(Context context){
        helper = new StorageUtilsOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    public String[] getAllKeys(){
        if (keys == null) {
            Cursor c = db.rawQuery(getNameQuery(), null);
            c.moveToFirst();
            keys = new String[c.getCount()];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = c.getString(1);
                c.moveToNext();
            }
            this.keys = keys;
        }
        return keys;
    }

    public Object getObject(Object o){
        Cursor c = db.rawQuery(getObjectQuery(o.getName()), null);
        if(c.moveToFirst()) {
            Object result = new Object(c.getString(1), convertStateFromInt(c.getInt(2)), c.getInt(3));
            c.close();
            return result;
        } else {
            c.close();
            return null;
        }
    }

    public boolean insertOrUpdateObject(Object o){
        ContentValues cv = new ContentValues();
        cv.put(StorageConstants.OBJECT_COLUMN_NAME, o.getName());
        cv.put(StorageConstants.OBJECT_COLUMN_ATTEMPTS, o.getAttempts());
        cv.put(StorageConstants.OBJECT_COLUMN_STATE, convertStateFromObject(o));
        int success = -1;
        //No current object exists
        if (getObject(o) == null){
            //No current object exists, create one
            success = (int) db.insert(StorageConstants.OBJECT_TABLE_NAME, StorageConstants.OBJECT_COLUMN_NAME, cv);
        } else {
            //Update the current value
            success = db.update(StorageConstants.OBJECT_TABLE_NAME,
                    cv,
                    updateObjectQuery(o),
                    null);
        }
        return success > 0;
    }

    public static String getObjectQuery(String name){
        return "SELECT * FROM " + StorageConstants.OBJECT_TABLE_NAME + " WHERE " +
                StorageConstants.OBJECT_COLUMN_NAME + " = '" + name + "'";
    }

    public static String getNameQuery(){
        return "SELECT * FROM "+ StorageConstants.OBJECT_TABLE_NAME;
    }

    public static String updateObjectQuery(Object o){
        return StorageConstants.OBJECT_COLUMN_NAME + "='" + o.getName() + "'";
    }

    protected static int convertStateFromObject(Object o){
        if (o.getState() == Object.State.CORRECT){
            return StorageConstants.OBJECT_STATE_CORRECT;
        } else if (o.getState() == Object.State.SKIPPED){
            return StorageConstants.OBJECT_STATE_SKIPPED;
        } else {
            return StorageConstants.OBJECT_STATE_NOT_ATTEMPTED;
        }
    }

    protected static Object.State convertStateFromInt(int s){
        if (s == StorageConstants.OBJECT_STATE_CORRECT){
            return Object.State.CORRECT;
        } else if(s == StorageConstants.OBJECT_STATE_SKIPPED){
            return Object.State.SKIPPED;
        } else {
            return Object.State.NOT_TESTED;
        }
    }



}
