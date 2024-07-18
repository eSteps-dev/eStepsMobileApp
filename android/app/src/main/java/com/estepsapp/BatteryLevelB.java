package com.estepsapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DeviceInformation;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Settings;
import com.mbientlab.metawear.module.Timer;
import bolts.Continuation;
import bolts.Task;

public class BatteryLevelB extends ReactContextBaseJavaModule implements ServiceConnection {


    private final ReactApplicationContext reactContext;
    private String device;
    private MetaWearBoard board;
    private BtleService.LocalBinder serviceBinder;

    public BatteryLevelB(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;


    }

    @ReactMethod
    public void bindLevelBatteryService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    @ReactMethod
    public void unbindLevelBatteryService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    return null;
                }
            });
            board.tearDown();
            board = null;

        }
    }
    @ReactMethod
    public void addListener(String eventState) {

    }


    @ReactMethod
    public void removeListeners(String eventState) {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        retrieveBoard(device);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        unbindLevelBatteryService();
    }

    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        final String[] serialNumber = new String[1];

        board = serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Timer timerModule = board.getModule(Timer.class);
                Settings settingsBattery = board.getModule(Settings.class);
                board.readDeviceInformationAsync().continueWith((Continuation<DeviceInformation, Void>) task1 -> {
                    serialNumber[0] = task1.getResult().serialNumber;
                    return null;
                });
                settingsBattery.battery().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                double level = data.value(Settings.BatteryState.class).charge;
                                double voltage = data.value(Settings.BatteryState.class).voltage;
                                sendDataToRN(level, voltage, serialNumber[0]);

                            }
                        });
                    }
                }).continueWithTask(new Continuation<Route, Task<Timer.ScheduledTask>>() {
                    @Override
                    public Task<Timer.ScheduledTask> then(Task<Route> task) throws Exception {
                        return timerModule.scheduleAsync(30000, false, () -> {
                            settingsBattery.battery().read();
                        });
                    };
                }).continueWithTask(task2 -> {
                    final long test_timer_id = task2.getResult().id();
                    Timer.ScheduledTask timer = timerModule.lookupScheduledTask(((byte) test_timer_id));
                    if(timer != null) {
                        timer.start();
                    }
                    return  null;
                });
                return task;
            };

        });
    }
    @ReactMethod
    public void sendDataToRN(double level, double voltage, String serialNumber) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("batteryLevelB", createEventData(level, voltage, serialNumber));
    }

    private WritableMap createEventData(double level, double voltage,  String serialNumber) {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("serialNumber", serialNumber);
        eventData.putDouble("level", level);
        eventData.putDouble("voltage", voltage);
        return eventData;
    }
    @NonNull
    @Override
    public String getName() {
        return "BatteryLevelB";
    }
}


