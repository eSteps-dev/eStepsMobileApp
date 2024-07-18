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
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Accelerometer;


import bolts.Continuation;
import bolts.Task;

public class AccelConnectionB extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private Accelerometer accelerometer;
    private String device;

    AccelConnectionB(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        retrieveBoard(device);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
    @ReactMethod
    public void bindAccelService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void unbindAccelService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            accelerometer.acceleration().stop();
            accelerometer.stop();
            board.tearDown();
            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    return null;
                }
            });
            board = null;

        }
    }
    @ReactMethod
    public void addListener(String eventState) {
    }
    @ReactMethod
    public void removeListeners(String eventState) {
    }
    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        final String[] serialNumber = new String[1];

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                board.readDeviceInformationAsync().continueWith((Continuation<DeviceInformation, Void>) task1 -> {
                    serialNumber[0] = task1.getResult().serialNumber;
                    return null;
                });
                accelerometer = board.getModule(Accelerometer.class);
                accelerometer.configure()
                        .odr(25f)       // Set sampling frequency to 25Hz, or closest valid ODR
                        .commit();
                return accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object ... env) {
                                String timestamp = data.formattedTimestamp();
                                double x = data.value(Acceleration.class).x();
                                double y = data.value(Acceleration.class).y();
                                double z = data.value(Acceleration.class).z();
                                sendDataToRN(x, y, z, serialNumber[0], timestamp);

                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        if ( board != null && accelerometer != null) {
                            accelerometer.acceleration().start();
                            accelerometer.start();
                        }
                        return null;
                    }
                });

            }
        });
    }
    @ReactMethod
    public void sendDataToRN(double x, double y, double z, String serialNumber, String timestamp) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("accelDataB", createEventData(x, y, z, serialNumber, timestamp));
    }

    private WritableMap createEventData(double x, double y, double z, String serialNumber, String timestamp) {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("serialNumber", serialNumber);
        eventData.putString("timestamp", timestamp);
        eventData.putDouble("x", x);
        eventData.putDouble("y", y);
        eventData.putDouble("z", z);
        return eventData;
    }
    @NonNull
    @Override
    public String getName() {
        return "AccelConnectionB";
    }
}

