package com.farwaahmad.mylist;

import android.content.Context;
import android.content.SharedPreferences;

public class LaunchManager {

    private static final String PREF_NAME = "LaunchManager";
    private static final String IS_FIRST_TIME = "isFirst";
    private static final String CONTEXTUAL_SWIPE_HINT_SHOWN = "contextualSwipeHintShown";

    private final SharedPreferences sharedPreferences;

    public LaunchManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setFirstLaunch(boolean isFirst) {
        sharedPreferences.edit().putBoolean(IS_FIRST_TIME, isFirst).apply();
    }

    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true);
    }

    public boolean shouldShowSwipeHint() {
        return !sharedPreferences.getBoolean(CONTEXTUAL_SWIPE_HINT_SHOWN, false);
    }

    public void markSwipeHintShown() {
        sharedPreferences.edit().putBoolean(CONTEXTUAL_SWIPE_HINT_SHOWN, true).apply();
    }
}
