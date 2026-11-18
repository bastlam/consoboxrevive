package com.consobox;

import android.app.Application;
import android.content.Context;
import com.consobox.service.Notifications;

public class ConsoBoxApp extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        Notifications.createChannels(this);
    }

    public static Context get() {
        return sContext;
    }
}
