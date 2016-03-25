package com.b09302083gmail.streamplayer;

import com.b09302083gmail.utils.QLog;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by JessieChen on 15/11/10.
 */
public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    private static String sAndroidId = "";

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAndroidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID);

        sContext = getApplicationContext();
        QLog.v(TAG, "Android UUID : " + sAndroidId);
    }

    public static String getAndroidId() {
        return sAndroidId;
    }

    public static Context getAppContext() {
        return sContext;
    }
}
