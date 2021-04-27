package com.logictreeit.android.fracturedphoto.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 20-11-2015.
 */
public class FP_PrefsManager {

    private Context context;
    private SharedPreferences prefsManager;

    public FP_PrefsManager(Context applicationContext) {
        this.context = applicationContext;
        this.prefsManager = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = prefsManager.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public void save(String key, boolean value) {
        SharedPreferences.Editor editor = prefsManager.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void saveDoubleorFloat2(String key, double value) {
        SharedPreferences.Editor editor = prefsManager.edit();
        editor.putFloat(key, (float) value);
        editor.apply();
    }

    public double getDouble(String key) {
        return prefsManager.getFloat(key, 0);
    }

    public String get(String key) {
        return prefsManager.contains(key)?prefsManager.getString(key, ""):"";
    }

    public boolean getBoolean(String key) {
        return prefsManager.getBoolean(key, false);
    }

    public void clearAllPrefs() {
        prefsManager.edit().clear().commit();
    }

    public boolean hasKey(String key) {
        return prefsManager.contains(key);
    }

    public void removeKey(String key) {
        SharedPreferences.Editor editor = prefsManager.edit();
        editor.remove(key);
        editor.apply();
    }
}
