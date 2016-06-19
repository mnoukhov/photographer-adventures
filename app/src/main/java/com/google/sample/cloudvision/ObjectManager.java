package com.google.sample.cloudvision;

import android.content.Context;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ObjectManager {

    private StorageUtils storage;
    private PropertiesHelper properties;
    private String[] allItems;
    private static ObjectManager manager;

    public static ObjectManager getInstance(Context context){
        if (manager != null){
            return manager;
        } else {
            manager = new ObjectManager(context);
            return manager;
        }
    }

    public ObjectManager(Context context){
        storage = new StorageUtils(context);
        properties = new PropertiesHelper(context);
        allItems = storage.getAllKeys();
    }

    //Return list of all item names in the database
    public String[] allItemNames(){
        return allItems;
    }

    public Object[] getAllItems(){ return storage.getAllObjects(); }

    //Return specified object with name
    public Object getObject(String name){
        return storage.getObject(name);
    }

    //Return the next object that is NOT_TESTED
    public Object getNextObject(){
        return new Object("fork", Object.State.NOT_TESTED, 0);
    }

    public boolean updateObject(Object o){
        return storage.insertOrUpdateObject(o);
    }
}
