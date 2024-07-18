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
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Debug;

import java.io.File;
import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;

public class upgradeFirmwareVersion extends ReactContextBaseJavaModule implements ServiceConnection {
    private final ReactApplicationContext reactContext;
    private TaskCompletionSource<Void> dfuTaskSource;
    private MetaWearBoard board;
    private BtleService.LocalBinder serviceBinder;
    private String device;

    upgradeFirmwareVersion(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void bindUpgradeFirmware(String device) {
        this.device = device;
        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(context, BtleService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        Log.i("device", device);
    }
    private final DfuProgressListener dfuProgressListener= new DfuProgressListenerAdapter() {
        @Override
        public void onDfuCompleted(String deviceAddress) {
            dfuTaskSource.setResult(null);
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            dfuTaskSource.setCancelled();
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            dfuTaskSource.setError(new RuntimeException("DFU error: " + message));
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        final BluetoothManager btManager=
                (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(device);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);

        updateFirmware(reactContext,DfuService.class);


    }
    private void updateFirmware(final Context context, final Class<? extends DfuBaseService> dfuServiceClass) {
        Capture<List<File>> files = new Capture<>();
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
             return  null;
            }
        });
    }
    @NonNull
    @Override
    public String getName() {
        return "upgradeFirmwareVersion";
    }
    @ReactMethod
    public  void unbindUpgradeFirmware () {
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

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
