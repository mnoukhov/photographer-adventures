package com.google.sample.cloudvision;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class ObjectManager {

    private StorageUtils storage;

    public ObjectManager(){

    }

    public Object getNextObject(){
        return new Object("fork", Object.State.NOT_TESTED, 0);
    }

    public boolean updateObject(Object o){
        return true;
    }


}
