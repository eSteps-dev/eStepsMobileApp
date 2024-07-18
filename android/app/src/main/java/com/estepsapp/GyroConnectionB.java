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
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.Gyro;

import bolts.Continuation;
import bolts.Task;

public class GyroConnectionB extends ReactContextBaseJavaModule implements ServiceConnection {

    private ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private Gyro gyro ;
    private String device;
    GyroConnectionB(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        retrieveBoard(device);
    }
    @ReactMethod
    public void bindGyroService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    @ReactMethod
    public void unbindGyroService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            gyro.angularVelocity().stop();
            gyro.stop();
            board.tearDown();

            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    Log.i("MainActivity", "Disconnected");
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

        board = serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                board.readDeviceInformationAsync().continueWith((Continuation<DeviceInformation, Void>) task1 -> {
                    serialNumber[0] = task1.getResult().serialNumber;
                    return null;
                });
                gyro = board.getModule(Gyro.class);
                gyro.configure().odr(Gyro.OutputDataRate.ODR_25_HZ).commit();
                return gyro.angularVelocity().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                String timestamp = data.formattedTimestamp();
                                double x = data.value(AngularVelocity.class).x();
                                double y = data.value(AngularVelocity.class).y();
                                double z = data.value(AngularVelocity.class).z();
                                sendDataToRN(x, y, z, serialNumber[0], timestamp);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        if ( board != null && gyro != null) {
                            gyro.angularVelocity().start();
                            gyro.start();
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
                .emit("gyroDataB", createEventData(x, y, z, serialNumber, timestamp));
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
    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @NonNull
    @Override
    public String getName() {
        return "GyroConnectionB";
    }
}
