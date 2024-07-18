package com.estepsapp;

import android.annotation.SuppressLint;
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
import com.mbientlab.metawear.module.Temperature.SensorType;
import com.mbientlab.metawear.module.Temperature.ExternalThermistor;
import com.mbientlab.metawear.module.Timer;


import bolts.Continuation;
import bolts.Task;

public class TempConnectionA extends ReactContextBaseJavaModule implements ServiceConnection {
    private Temperature temperature;
    private Temperature.Sensor tempSensor;
    private ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private String device;

    public TempConnectionA(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void bindTempService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }
    @ReactMethod
    public void unbindTempService() {
        Context context = reactContext.getApplicationContext();
        context.unbindService(this);
        if (board != null) {
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
        if (board != null && tempSensor != null) {
            tempSensor.read();
        }
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

    @NonNull
    @Override
    public String getName() {
        return "TempConnectionA";
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
                temperature = board.getModule(Temperature.class);
                board.getModule(BarometerBosch.class).start();
                tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];
                // Read data from pin 0, pulldown resistor is on pin 1, active low
                ((ExternalThermistor) temperature.findSensors(SensorType.EXT_THERMISTOR)[0])
                        .configure((byte) 0, (byte) 1, false);
                Timer timerModule = board.getModule(Timer.class);
                return tempSensor.addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object ... env) {
                                String timestamp = data.formattedTimestamp();
                                double temp = data.value(Float.class).doubleValue();
                                sendDataToRN(temp, serialNumber[0], timestamp);
                            }
                        });
                    }
                }).continueWithTask(new Continuation<Route, Task<Timer.ScheduledTask>>() {
                    @Override
                    public Task<Timer.ScheduledTask> then(Task<Route> task) throws Exception {
                      return timerModule.scheduleAsync(30000, false, () -> {
                         tempSensor.read();
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
    }
        });
    }
    @ReactMethod
    public void sendDataToRN(double temp, String serialNumber, String timestamp) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("tempDataA", createEventData(temp, serialNumber, timestamp));
    }

    private WritableMap createEventData(double temp, String serialNumber, String timestamp) {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("serialNumber", serialNumber);
        eventData.putString("timestamp", timestamp);
        eventData.putDouble("temp", temp);
        return eventData;
    }

}


