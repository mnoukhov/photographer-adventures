package com.google.sample.cloudvision;

import android.content.Context;

import java.util.Properties;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ObjectManager {

    private static StorageUtils mStorage;
    private static PropertiesHelper mProperties;
    private static String[] mAllItems;
    private static int mCurrentItemIndex = 0;
    private static ObjectManager mManager;


    public static ObjectManager getInstance(){
        if (mManager == null){
            mManager = new ObjectManager();
        }
        return mManager;
    }

    private ObjectManager(){
    }

    private static StorageUtils getMStorage(Context context){
        if(mStorage == null){
            mStorage = new StorageUtils(context);
        }
        mAllItems = mStorage.getAllKeys();
        return mStorage;
    }

    private static PropertiesHelper getMProperties(Context context){
        if(mProperties == null){
            mProperties = new PropertiesHelper(context);
        }
        return mProperties;
    }

    //Return list of all item names in the database
    public static String[] allItemNames(){
        return mAllItems;
    }

    public static Object[] getAllItems(Context context){ return getMStorage(context).getAllObjects(); }

    //Return specified object with name
    public static Object getObject(String name, Context context){
        return getMStorage(context).getObject(name);
    }

    public static int getExperience(Object o, Context context){
        Properties items = getMProperties(context).getProperties("items.properties");
        int experience = Integer.valueOf(items.getProperty(o.getName()));
        return experience;
    }

    /*
    Return the next object that is NOT_TESTED and not the same as the current object
    */
    public static Object getNextObject(Object current, Context context) {
        //No guarantee these things are initialized, initialize them.
        getMStorage(context);
        getMProperties(context);
        for (int i = mCurrentItemIndex + 1; i < mAllItems.length; i++) {
            Object o = getObject(mAllItems[i], context);
            if (o.getState() == Object.State.NOT_TESTED || o.getState() == Object.State.SKIPPED
                    && (current == null || !o.getName().equalsIgnoreCase(current.getName()))) {
                mCurrentItemIndex = i;
                return o;
            }
        }

        for (int i = 0; i < mCurrentItemIndex; i++) {
            Object o = getObject(mAllItems[i], context);
            if (o.getState() == Object.State.NOT_TESTED || o.getState() == Object.State.SKIPPED
                    && (current == null || !o.getName().equalsIgnoreCase(current.getName()))) {
                mCurrentItemIndex = i;
                return o;
            }
        }
        return null;
        //TODO: Handle when nothing is left to test
    }

    public static boolean updateObject(Object o, Context context){
        return getMStorage(context).insertOrUpdateObject(o);
    }

    public static void resetObjects(Context context) {
        Object[] objects = getAllItems(context);
        for (Object obj : objects) {
            obj.setState(Object.State.NOT_TESTED);
            obj.setAttempts(0);
            updateObject(obj, context);
        }
    }

    public static Object setCurrentObject(String name, Context context) {
        for (int i = 0; i < mAllItems.length; i++) {
            Object o = getObject(mAllItems[i], context);
            if (o.getName().equals(name)) {
                mCurrentItemIndex = i;
                return o;
            }
        }
        return null;
    }

    public Object getCurrentObject(Context context) {
        return getObject(mAllItems[mCurrentItemIndex], context);
    }
}
