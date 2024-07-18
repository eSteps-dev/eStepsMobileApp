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
import com.mbientlab.metawear.module.BarometerBosch;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Timer;

import bolts.Continuation;
import bolts.Task;

public class DeviceInfoB extends ReactContextBaseJavaModule implements ServiceConnection {


    private final ReactApplicationContext reactContext;
    private String device;
    private MetaWearBoard board;
    private BtleService.LocalBinder serviceBinder;

    public DeviceInfoB(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;


    }

    @ReactMethod
    public void bindInfoDevice(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    @ReactMethod
    public void unbindInfoDevice() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
            board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    Log.i("MainActivity", "Disconnected");
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

    }
    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
        final String[] serialNumber = new String[1];

// Create a MetaWear board object for the Bluetooth Device
        board = serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                Timer timerModule = board.getModule(Timer.class);
                board.readDeviceInformationAsync()
                        .continueWith(new Continuation<DeviceInformation, Void>() {
                            @Override
                            public Void then(Task<DeviceInformation> task) throws Exception {
                                String serialNumber = task.getResult().serialNumber;
                                String firmwareRevision = task.getResult().firmwareRevision;
                                String hardwareRevision = task.getResult().hardwareRevision;
                                String manufacturer = task.getResult().manufacturer;
                                String modelNumber = task.getResult().modelNumber;
                                sendDataToRN(serialNumber, firmwareRevision, hardwareRevision, manufacturer, modelNumber);
                                return null;
                            }
                        });
                return task;
            };

        });
    }
    @ReactMethod
    public void sendDataToRN(String serialNumber, String firmwareRevision, String hardwareRevision, String manufacturer, String modelNumber) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("DeviceInfoB", createEventData(serialNumber, firmwareRevision, hardwareRevision, manufacturer, modelNumber));
    }

    private WritableMap createEventData(String serialNumber, String firmwareRevision, String hardwareRevision,String manufacturer, String modelNumber) {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("serialNumber", serialNumber);
        eventData.putString("firmwareRevision", firmwareRevision);
        eventData.putString("hardwareRevision", hardwareRevision);
        eventData.putString("manufacturer", manufacturer);
        eventData.putString("modelNumber", modelNumber);
        return eventData;
    }
    @NonNull
    @Override
    public String getName() {
        return "DeviceInfoB";
    }
}


