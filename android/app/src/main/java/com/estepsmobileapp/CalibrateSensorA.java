package com.estepsmobileapp;
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
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;

import com.mbientlab.metawear.module.Macro;
import com.mbientlab.metawear.module.SensorFusionBosch;


import bolts.CancellationTokenSource;
import bolts.Continuation;
import bolts.Task;
public class CalibrateSensorA extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private  String device;
    private SensorFusionBosch sensorFusion;

    CalibrateSensorA(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        calibrateData(device);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    @ReactMethod
    public void bindCalibrateService(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void unbindCalibrateService() {
        Context context = reactContext.getApplicationContext();
        if(context != null) {
            context.unbindService(this);
        }

        if (board != null) {
            if(sensorFusion != null)
            {sensorFusion.resetOrientation();}
            board.disconnectAsync();
            board.tearDown();
            board = null;

        }

    }


    @ReactMethod
    public void addListener(String eventName) {
    }

    @ReactMethod
    public void removeListeners(Integer count) {
    }

    private  void calibrateData(final String MW_MAC_ADDRESS){
        final BluetoothManager btManager=
                (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if(board != null) {
                    sensorFusion = board.getModule(SensorFusionBosch.class);
                    final Macro macro = board.getModule(Macro.class);

                    if (sensorFusion != null) {
                        sensorFusion.configure()
                                .mode(SensorFusionBosch.Mode.NDOF)
                                .accRange(SensorFusionBosch.AccRange.AR_16G)
                                .gyroRange(SensorFusionBosch.GyroRange.GR_2000DPS)
                                .commit();
                        final CancellationTokenSource cts = new CancellationTokenSource();
                        sensorFusion.start();
                        sensorFusion.calibrate(cts.getToken(), state ->
                                {
                                    String accel = String.valueOf(state.accelerometer);
                                    String gyro = String.valueOf(state.gyroscope);
                                    String magne = String.valueOf(state.magnetometer);
                                    sendDataToRN(accel, gyro, magne);
                                })
                                .onSuccessTask(task2 -> {
                                    sensorFusion.stop();
                                    if (task2.isFaulted()) {
                                        Log.i("MainActivity", "Calibration failed");
                                        // Handle the failure case here
                                    } else {
                                        SensorFusionBosch.CalibrationData calibrationState = task2.getResult();
                                        sensorFusion.readCalibrationStateAsync().onSuccessTask(task3 -> {
                                            Log.i("TASK3", task3.getResult().toString());
                                            return  null;
                                        });
                                        macro.startRecord(true);
                                        sensorFusion.writeCalibrationData(calibrationState);
                                        macro.endRecordAsync().continueWith(new Continuation<Byte, Void>() {
                                            @Override
                                            public Void then(Task<Byte> task) throws Exception {
                                                Log.i("MainActivity", "Macro ID = " + task.getResult());
                                                return null;
                                            }
                                        });

                                    }
                                    return null;
                                });
                    }
                }
                return null;


            }
        });



    }
    @ReactMethod
    public void sendDataToRN(String accel, String gyro, String magne) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("calibrationDataA", createEventData(accel, gyro, magne));
    }

    private WritableMap createEventData(String accel, String gyro, String magne) {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("accel", accel);
        eventData.putString("gyro", gyro);
        eventData.putString("magne", magne);
        return eventData;
    }
    @NonNull
    @Override
    public String getName() {
        return "CalibrateSensorA";
    }
}

