package com.doepiccoding.viewmodel;

import android.app.Application;

public class DeltaModelApplication extends Application {

    private static DeltaModelApplication _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
    }

    public static DeltaModelApplication getInstance() {
        return _instance;
    }
}
