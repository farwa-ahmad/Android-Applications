package com.example.mylist;

import android.content.Context;
import android.content.SharedPreferences;

//Class to manage launching activities (to make the slider appear only on first launch)

public class LaunchManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static String PREF_NAME = "LaunchManger";
    private static String IS_FIRST_TIME = "isFirst";

    public LaunchManager(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME,0);
        editor = sharedPreferences.edit();
    }

    public void setFirstLaunch(boolean isFirst){
        editor.putBoolean(IS_FIRST_TIME,isFirst);
        editor.commit();
    }

    public boolean isFirstTime(){
        return sharedPreferences.getBoolean(IS_FIRST_TIME,true);
    }
}
