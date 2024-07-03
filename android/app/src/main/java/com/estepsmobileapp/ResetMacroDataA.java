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

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Debug;
import com.mbientlab.metawear.module.Macro;

import bolts.Continuation;
import bolts.Task;

public class ResetMacroDataA extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private  String device;

    ResetMacroDataA(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        ResetData(device);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    @ReactMethod
    public void resetCalibration(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @ReactMethod
    public void unbindResetDataMacroService() {
        Context context = reactContext.getApplicationContext();
        if(context != null) {
            context.unbindService(this);
        }
        if (board != null) {
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

    private  void ResetData(final String MW_MAC_ADDRESS){
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
                    final Macro macro = board.getModule(Macro.class);
                    macro.eraseAll();
                    board.getModule(Debug.class).resetAfterGc();
                    board.getModule(Debug.class).disconnectAsync();
                    sendDataToRN("Done");
                    Log.i("Done", "Done");
                }
                return null;
            }
        });



    }
    @ReactMethod
    public void sendDataToRN(String result) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RestMacroDataAResult", result);
    }

    @NonNull
    @Override
    public String getName() {
        return "ResetMacroDataA";
    }
}


