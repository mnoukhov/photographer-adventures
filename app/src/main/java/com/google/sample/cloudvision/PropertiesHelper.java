package com.google.sample.cloudvision;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class PropertiesHelper {
    private Context context;
    private Properties properties;

    public PropertiesHelper(Context context){
        this.context=context;
    }

    public Properties getProperties(String file){
        try{
            properties = new Properties();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        }catch (Exception e){
            System.out.print(e.getMessage());
        }

        return properties;
    }
}
