package com.ccs.gencorelite.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceConfig {
    public static final String REFERENCE = "reference";
    public static final String TITLE = "title";
    public static final String VERSION = "version";
    public static final String PACKAGE = "package";

    public static final String DEBUGMODE = "debug_mode";


    public static void registerPref(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void setTitle(Context context, String value) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TITLE, value);
        editor.apply();
    }

    public static String getTitle(Context context) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        return pref.getString(TITLE, "");
    }
    public static void setVersion(Context context, String value) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(VERSION, value);
        editor.apply();
    }

    public static String getVersion(Context context) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        return pref.getString(VERSION, "");
    }
    public static void setPackage(Context context, String value) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PACKAGE, value);
        editor.apply();
    }

    public static String getPackage(Context context) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        return pref.getString(PACKAGE, "");
    }
    public static void setDebugmode(Context context, int value) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(DEBUGMODE, value);
        editor.apply();
    }
//0 - вимкнений, 1 - увімкнений
    public static int getDebugmode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);
        return pref.getInt(DEBUGMODE, 0);
    }
}