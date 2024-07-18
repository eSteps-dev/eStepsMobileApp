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

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.CartesianAxis;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.AccelerometerBmi160;
import com.mbientlab.metawear.module.AccelerometerBmi270;
import com.mbientlab.metawear.module.AccelerometerMma8452q;
import com.mbientlab.metawear.module.AccelerometerBosch;


import bolts.Continuation;
import bolts.Task;

public class MovementDetectionA extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private  String device;
    private AccelerometerMma8452q accMma8452q ;

    MovementDetectionA(ReactApplicationContext reactContext) {
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
    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        final String[] serialNumber = new String[1];

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Log.i("ENter", "hello");

                accMma8452q = board.getModule(AccelerometerMma8452q.class);

                accMma8452q.configure()
                        .odr(AccelerometerMma8452q.OutputDataRate.ODR_12_5_HZ)        // Set data rate to 12.5Hz
                        .range(AccelerometerMma8452q.FullScaleRange.FSR_8G)           // Set range to +/-8g
                        .commit();
                accMma8452q.freeFall().configure()
                        .threshold(0.333f)
                        .axes(CartesianAxis.values())
                        .commit();
                return accMma8452q.freeFall().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("MainActivity", data.value(AccelerometerMma8452q.Movement.class).toString());
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        accMma8452q.freeFall().start();
                        accMma8452q.start();
                        return null;
                    }
                });

            }
        });
    }
    @ReactMethod
    public void bindMovementService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void unbindMovementService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            accMma8452q.freeFall().stop();
            accMma8452q.stop();
            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    Log.i("MainActivityAccel", "Disconnected");
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
    @NonNull
    @Override
    public String getName() {
        return "MovementDetectionA";
    }
}
