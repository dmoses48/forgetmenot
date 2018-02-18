package com.dmsiconsulting.forgetmenot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static android.bluetooth.le.ScanSettings.CALLBACK_TYPE_FIRST_MATCH;
import static android.bluetooth.le.ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT;

public class BTLEScanner {

    public void scanForDevice(String deviceAddress, long msToSearch, Consumer<Boolean> foundDevice) {
        Consumer<Boolean> foundDeviceCallOnce = new Consumer<Boolean>() {
            AtomicBoolean ranAlready = new AtomicBoolean(false);
            @Override
            public void accept(Boolean aBoolean) {
                if (!ranAlready.getAndSet(true))
                    foundDevice.accept(aBoolean);
            }
        };
        BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                foundDeviceCallOnce.accept(true);
                Log.d("BTScan", result.getDevice().getName() + ":" + result.getDevice().getAddress() + "--" + result.getRssi());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d("BTScan", "FAIL:" + errorCode);
            }
        };
        Handler h = new Handler();
        h.postDelayed(() -> {
            bluetoothLeScanner.stopScan(callback);
            foundDeviceCallOnce.accept(false);
        }, msToSearch);
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setDeviceAddress(deviceAddress).build());

        bluetoothLeScanner.startScan(scanFilters,
                new ScanSettings.Builder().setNumOfMatches(MATCH_NUM_ONE_ADVERTISEMENT).build(),
                callback);
    }
}
