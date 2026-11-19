package com.consobox;

import android.app.Application;
import android.content.Context;

public class ConsoBoxApp extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context get() {
        return sContext;
    }
}
