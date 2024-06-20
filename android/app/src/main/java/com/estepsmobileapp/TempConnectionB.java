package com.estepsmobileapp;
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
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.BarometerBosch;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Temperature.SensorType;
import com.mbientlab.metawear.module.Temperature.ExternalThermistor;

import bolts.Continuation;
import bolts.Task;

public class TempConnectionB extends ReactContextBaseJavaModule implements ServiceConnection {
    private Temperature temperature;
    private Temperature.Sensor tempSensor;
    private ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private boolean listenersRegistered;
    private String device;
    public TempConnectionB(ReactApplicationContext reactContext) {
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
            board.disconnectAsync();
            board = null;

        }
        listenersRegistered = false;
    }
    @ReactMethod
    public void addListener(String eventState) {
        if (!listenersRegistered && board != null && tempSensor != null) {
            tempSensor.read();
            listenersRegistered = true;
        }
    }
    @ReactMethod
    public void removeListeners(String eventState) {
        if (listenersRegistered && board != null && temperature != null) {
            listenersRegistered = false;
        }
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("device", device);
        retrieveBoard(device);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @NonNull
    @Override
    public String getName() {
        return "TempConnectionB";
    }

    private void retrieveBoard(final String MW_MAC_ADDRESS) {
        final BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);
// only for RPro, CPro, Env, and Motion boards

// Create a MetaWear board object for the Bluetooth Device
        board = serviceBinder.getMetaWearBoard(remoteDevice);

        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                temperature = board.getModule(Temperature.class);
                board.getModule(BarometerBosch.class).start();
                tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];
                // Read data from pin 0, pulldown resistor is on pin 1, active low
                ((ExternalThermistor) temperature.findSensors(SensorType.EXT_THERMISTOR)[0])
                        .configure((byte) 0, (byte) 1, false);

                return tempSensor.addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object ... env) {
                                double temp = data.value(Float.class).doubleValue();
                                emitTempData(temp);
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        tempSensor.read();
                        return null;
                    }
                });
            }
        });
    }
    @SuppressLint("DefaultLocale")
    private void emitTempData(double temp) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("tempDataB", String.format("%f", temp));
    }
}

