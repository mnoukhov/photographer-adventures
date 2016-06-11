package com.google.sample.cloudvision;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Properties;
import java.util.Set;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class StorageUtilsOpenHelper extends SQLiteOpenHelper{

    private PropertiesHelper properties;

    public static String buildObjectTableQuery(){
        String tableQuery = "CREATE TABLE " + StorageConstants.OBJECT_TABLE_NAME + " (" +
                StorageConstants.OBJECT_COLUMN_ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
                StorageConstants.OBJECT_COLUMN_NAME + " VARCHAR(255)," +
                StorageConstants.OBJECT_COLUMN_STATE + " INTEGER," +
                StorageConstants.OBJECT_COLUMN_ATTEMPTS + " INTEGER" +
                ");";
        return tableQuery;
    }

    public static String buildDropObjectTableQuery(){
        String tableDropQuery = "DROP TABLE IF EXISTS   " + StorageConstants.OBJECT_TABLE_NAME + ";";
        return tableDropQuery;
    }

    public StorageUtilsOpenHelper(Context context){
        super(context, StorageConstants.DB_NAME, null, StorageConstants.DB_VERSION);
        properties = new PropertiesHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Delete the table during testing
        Properties testing = properties.getProperties("testing.properties");
        if(testing.containsKey("drop_tables") && testing.getProperty("drop_tables").equalsIgnoreCase("true")){
            db.execSQL(buildDropObjectTableQuery());
        }

        //Instantiate the table
        db.execSQL(buildObjectTableQuery());

        //Fill the database with data from items.properties
        Properties items = properties.getProperties("items.properties");
        Set<java.lang.Object> defaultItemSet = items.keySet();
        String[] defaultItems = defaultItemSet.toArray(new String[defaultItemSet.size()]);
        for(int i = 0; i < defaultItems.length; i++){
            Object o = createObjectFromName(defaultItems[i]);
            ContentValues cv = new ContentValues();
            cv.put(StorageConstants.OBJECT_COLUMN_NAME, o.getName());
            cv.put(StorageConstants.OBJECT_COLUMN_ATTEMPTS, o.getAttempts());
            cv.put(StorageConstants.OBJECT_COLUMN_STATE, StorageUtils.convertStateFromObject(o));
            //No current object exists
            db.insert(StorageConstants.OBJECT_TABLE_NAME, StorageConstants.OBJECT_COLUMN_NAME, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: no idea if we want to implement this;
        db.execSQL(buildDropObjectTableQuery());
        onCreate(db);
    }

    private Object createObjectFromName(String name){
        return new Object(name, Object.State.NOT_TESTED, 0);
    }
}
