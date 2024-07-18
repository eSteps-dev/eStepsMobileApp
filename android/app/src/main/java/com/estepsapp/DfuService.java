package com.estepsapp;


import no.nordicsemi.android.dfu.DfuBaseService;

import android.app.Activity;

import androidx.annotation.Nullable;

public class DfuService extends DfuBaseService {


    @Nullable
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return null;
    }

    @Override
    protected boolean isDebug() {
        // Here return true if you want the service to print more logs in LogCat.
        // Library's BuildConfig in current version of Android Studio is always set to DEBUG=false, so
        // make sure you return true or your.app.BuildConfig.DEBUG here.
        return BuildConfig.DEBUG;
    }

}
