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
import com.mbientlab.metawear.module.AccelerometerBmi160;
import com.mbientlab.metawear.module.AccelerometerBmi160.StepDetectorMode;
import com.mbientlab.metawear.module.AccelerometerBosch.AccRange;

import bolts.Continuation;
import bolts.Task;

public class StepDetectionA extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private  String device;
    private AccelerometerBmi160 accBmi160;
    StepDetectionA(ReactApplicationContext reactContext) {
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
    public void bindStepsService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void unbindStepsService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            accBmi160.stepDetector().stop();
            accBmi160.stop();
            board.tearDown();

            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    Log.i("MainActivityAccel", "Disconnected");
                    return null;
                }
            });
            board = null;
        }

    }
    @ReactMethod
    public void addListener(String eventState) {
        if ( board != null && accBmi160 != null) {
            accBmi160.stepDetector().start();
            accBmi160.start();

        }
    }
    @ReactMethod
    public void removeListeners(String eventState) {
    }


    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        board= serviceBinder.getMetaWearBoard(remoteDevice);


        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {

                accBmi160 = board.getModule(AccelerometerBmi160.class);
                Log.i("stepDetector", String.valueOf(accBmi160));
            // Configuration the algorithm to run as a detector
            // using normal detection mode

                accBmi160.stepDetector().configure()
                        .mode(StepDetectorMode.NORMAL)
                        .commit();
                return accBmi160.stepDetector().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("MainActivity", "Took a step");
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
    @ReactMethod
    public void sendDataToRN(Integer step) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("steps", createEventData(step));
    }



    private WritableMap createEventData(Integer step ) {
        WritableMap eventData = Arguments.createMap();
        eventData.putInt("step", step);

        return eventData;
    }
    @NonNull
    @Override
    public String getName() {
        return "StepDetectionA";
    }
}
