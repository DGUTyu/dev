package com.example.dev.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Description：SharedPreferences的管理类
 */
public class PreferenceUtil {

    private static SharedPreferences mSharedPreferences = null;

    private static Editor mEditor = null;

    public static void init(Context context) {
        if (null == mSharedPreferences) {
            mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static void removeKey(String key) {
        mEditor = mSharedPreferences.edit();
        mEditor.remove(key);
        mEditor.commit();
    }

    public static void removeAll() {
        mEditor = mSharedPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public static void commitString(String key, String value) {
        mEditor = mSharedPreferences.edit();

        try {
            mEditor.putString(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mEditor.commit();
    }

    public static String getString(String key, String faillValue) {
        String value = mSharedPreferences.getString(key, faillValue);

        return value;

    }

    public static void commitInt(String key, int value) {
        mEditor = mSharedPreferences.edit();

        mEditor.putInt(key, value);

        mEditor.commit();
    }

    public static int getInt(String key, int failValue) {
        int value = mSharedPreferences.getInt(key, failValue);

        return value;
    }

    public static void commitLong(String key, long value) {
        mEditor = mSharedPreferences.edit();

        mEditor.putLong(key, value);

        mEditor.commit();
    }

    public static long getLong(String key, long failValue) {

        long value = mSharedPreferences.getLong(key, failValue);

        return value;

    }

    public static void commitBoolean(String key, boolean value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(key, value);

        mEditor.commit();
    }

    public static Boolean getBoolean(String key, boolean failValue) {
        return mSharedPreferences.getBoolean(key, failValue);
    }


    public static void commitFloat(String key, float value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putFloat(key, value);
        mEditor.commit();
    }

    public static float getFloat(String key, float failValue) {
        return mSharedPreferences.getFloat(key, failValue);
    }
}
