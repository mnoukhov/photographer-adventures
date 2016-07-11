package com.google.sample.cloudvision;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Jeric Pauig on 7/11/2016.
 */
public class PlayerManager {

    private static final String LOG_TAG = "PlayerManager";

    private static final String USER_SHARED_PREFERENCES = "user_shared_preferences";
    private static final String USER_EXPERIENCE = "user_experience";
    private static final int[] LEVEL_CURVE = {0, 2, 8, 18, 32, 50, 72, 98, 128, 162, 200};

    private Context context;

    public PlayerManager(Context context){
        this.context = context;
    }

    public int getExperience() {
        SharedPreferences pref = context.getSharedPreferences(USER_SHARED_PREFERENCES, 0);
        return pref.getInt(USER_EXPERIENCE, 0);
    }

    public int getLevel(){
        int currentExperience = getExperience();
        int previousExperience = 0;
        for (int i = 0 ; i < LEVEL_CURVE.length; i++){
            int levelExperience = LEVEL_CURVE[i];
            if (currentExperience <= levelExperience && currentExperience > previousExperience){
                return i;
            }
            previousExperience = levelExperience;
        }
        //If not, player is still level 1
        return 1;
    }

    public boolean addExperience(int additional){
        try {
            //Increment experience
            SharedPreferences pref = context.getSharedPreferences(USER_SHARED_PREFERENCES, 0);
            int currentExperience = pref.getInt(USER_EXPERIENCE, 0);
            SharedPreferences.Editor editor = pref.edit();
            int newExperience = currentExperience + additional;
            editor.putInt(USER_EXPERIENCE, newExperience);
            editor.commit();
            return true;
        } catch (Exception e){
            Log.e(LOG_TAG, "addExperience: failed to add experience to player", e);
            return false;
        }
    }

    public int experienceUntilNextLevel(){
        int currentExperience = getExperience();
        int levelExperience = LEVEL_CURVE[getLevel()];
        return levelExperience - currentExperience;
    }

    public int percentageOfLevelComplete(){
        int currentExperience = getExperience();
        int level = getLevel();
        int levelExperience = LEVEL_CURVE[level];
        int previousLevelExperience = LEVEL_CURVE[level - 1];
        int differenceInLevel = levelExperience - previousLevelExperience;
        int differenceInCurrent = currentExperience - previousLevelExperience;
        return (int)Math.floor((double)differenceInCurrent / (double)differenceInLevel * 100d);
    }

    //Please don't use unless testing
    protected boolean setExperience(int experience){
        try {
            Log.d(LOG_TAG, "setExperience: Called. Setting experience to " + experience);
            //Increment experience
            SharedPreferences pref = context.getSharedPreferences(USER_SHARED_PREFERENCES, 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(USER_EXPERIENCE, experience);
            editor.commit();
            return true;
        } catch (Exception e){
            Log.e(LOG_TAG, "addExperience: failed to add experience to player", e);
            return false;
        }
    }
}
