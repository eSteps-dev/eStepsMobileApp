package com.estepsmobileapp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mbientlab.metawear.Data;
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
    private boolean listenersRegistered;
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
            board.disconnectAsync();
            board = null;

        }
        listenersRegistered = false;
    }
    @ReactMethod
    public void addListener(String eventState) {
        if (!listenersRegistered && board != null && accelerometer != null) {
            accelerometer.acceleration().start();
            accelerometer.start();
            listenersRegistered = true;
        }
    }
    @ReactMethod
    public void removeListeners(String eventState) {
        if (listenersRegistered && board != null && accelerometer != null) {
            accelerometer.acceleration().stop();
            accelerometer.stop();
            listenersRegistered = false;
        }
    }
    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
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
                                double x = data.value(Acceleration.class).x();
                                double y = data.value(Acceleration.class).y();
                                double z = data.value(Acceleration.class).z();
                                emitAccelData(x, y, z);

                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        addListener("true");
                        return null;
                    }
                });

            }
        });
    }
    private void emitAccelData(double x, double y, double z) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("accelDataB", String.format("%f/ %f/ %f", x, y, z));
    }
    @NonNull
    @Override
    public String getName() {
        return "AccelConnectionB";
    }
}

