package com.estepsmobileapp;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlePackage implements ReactPackage {

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();


        modules.add(new GyroConnectionA(reactContext));
        modules.add(new GyroConnectionB(reactContext));


        modules.add(new AccelConnectionA(reactContext));
        modules.add(new AccelConnectionB(reactContext));

        modules.add(new TempConnectionA(reactContext));
        modules.add(new TempConnectionB(reactContext));

        modules.add(new CalibrateSensorB(reactContext));
        modules.add(new CalibrateSensorA(reactContext));

        modules.add(new ResetMacroDataA(reactContext));
        modules.add(new ResetMacroDataB(reactContext));

        modules.add(new upgradeFirmwareVersion(reactContext));

        return modules;
    }

}
