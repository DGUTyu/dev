package com.example.dev.app;

import android.app.Application;

import com.example.dev.utils.PreferenceUtil;

/**
 * @author
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtil.init(this);
    }
}
