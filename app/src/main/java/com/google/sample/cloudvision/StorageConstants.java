package com.google.sample.cloudvision;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class StorageConstants {

    public static final String DB_NAME = "objectDB";

    public static final String OBJECT_TABLE_NAME = "objects";
    public static final String OBJECT_COLUMN_NAME = "name";
    public static final String OBJECT_COLUMN_STATE = "state";
    public static final String OBJECT_COLUMN_ATTEMPTS = "attempts";
    public static final String OBJECT_COLUMN_ID = "id";

    public static final int OBJECT_STATE_CORRECT = 0;
    public static final int OBJECT_STATE_SKIPPED = 1;
    public static final int OBJECT_STATE_NOT_ATTEMPTED = 2;

    public static final int DB_VERSION = 1;

}
