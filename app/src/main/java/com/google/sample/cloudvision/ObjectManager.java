package com.google.sample.cloudvision;

import android.content.Context;

import java.util.Properties;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ObjectManager {

    private StorageUtils mStorage;
    private PropertiesHelper mProperties;
    private String[] mAllItems;
    private int mCurrentItemIndex = 0;
    private static ObjectManager mManager;


    public static ObjectManager getInstance(Context context){
        if (mManager != null){
            return mManager;
        } else {
            mManager = new ObjectManager(context);
            return mManager;
        }
    }

    public ObjectManager(Context context){
        mStorage = new StorageUtils(context);
        mProperties = new PropertiesHelper(context);
        mAllItems = mStorage.getAllKeys();
        mCurrentItemIndex = 0;
    }

    //Return list of all item names in the database
    public String[] allItemNames(){
        return mAllItems;
    }

    public Object[] getAllItems(){ return mStorage.getAllObjects(); }

    //Return specified object with name
    public Object getObject(String name){
        return mStorage.getObject(name);
    }

    public int getExperience(Object o){
        Properties items = mProperties.getProperties("items.properties");
        int experience = Integer.valueOf(items.getProperty(o.getName()));
        return experience;
    }

    /*
    Return the next object that is NOT_TESTED and not the same as the current object
    */
    public Object getNextObject(Object current) {
        for (int i = mCurrentItemIndex + 1; i < mAllItems.length; i++) {
            Object o = getObject(mAllItems[i]);
            if (o.getState() == Object.State.NOT_TESTED || o.getState() == Object.State.SKIPPED
                    && (current == null || !o.getName().equalsIgnoreCase(current.getName()))) {
                mCurrentItemIndex = i;
                return o;
            }
        }

        for (int i = 0; i < mCurrentItemIndex; i++) {
            Object o = getObject(mAllItems[i]);
            if (o.getState() == Object.State.NOT_TESTED || o.getState() == Object.State.SKIPPED
                    && (current == null || !o.getName().equalsIgnoreCase(current.getName()))) {
                mCurrentItemIndex = i;
                return o;
            }
        }
        return null;
        //TODO: Handle when nothing is left to test
    }

    public boolean updateObject(Object o){
        return mStorage.insertOrUpdateObject(o);
    }

    public void resetObjects() {
        Object[] objects = getAllItems();
        for (Object obj : objects) {
            obj.setState(Object.State.NOT_TESTED);
            obj.setAttempts(0);
            updateObject(obj);
        }
    }
}
