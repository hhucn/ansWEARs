/*
 * Copyright 2018 Bashkim Berzati
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.opptain.answearscore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.util.ArrayList;
import java.util.Set;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The BluetoothScanner is responsible for returning available and bond Bluetooth-Hubs.
 * The Permission {@code android.permission.BLUETOOTH_ADMIN} is required.
 */
public class BluetoothScanner {
    private static final String TAG = "Bluetooth-Scanner";

    private Context mContext;
    private DataTransmitter mDataTransmitter;
    private BluetoothAdapter mBluetoothAdapter;

    BluetoothScanner(Context context, DataTransmitter dataTransmitter) {
        mContext = context;
        mDataTransmitter = dataTransmitter;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startScanResultReceiver();
    }

    /**
     * Return available as well as already bond Bluetooth devices
     */
    public void startBluetoothScan() {
        mBluetoothAdapter.startDiscovery();
        mDataTransmitter.syncDataItem(buildBluetoothBondedDataItem());
    }

    private void startScanResultReceiver() {
        IntentFilter bluetoothActionFilter = new IntentFilter();
        bluetoothActionFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothActionFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothActionFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.registerReceiver(mScanResultsReceiver, bluetoothActionFilter);
    }

    private PutDataMapRequest buildBluetoothBondedDataItem() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        PutDataMapRequest bluetoothData = PutDataMapRequest.create(Paths.PATH_BLUETOOTH_BONDED);
        DataMap bluetoothDataMap = bluetoothData.getDataMap();

        ArrayList<String> deviceName = new ArrayList<>(10);
        ArrayList<String> deviceMAC = new ArrayList<>(10);
        if (bondedDevices != null && !bondedDevices.isEmpty()) {
            bluetoothDataMap.putBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE, true);
            for (BluetoothDevice device : bondedDevices) {
                deviceName.add(device.getName());
                deviceMAC.add(device.getAddress());
            }
            bluetoothDataMap.putStringArrayList(Keys.KEY_BLUETOOTH_NAME, deviceName);
            bluetoothDataMap.putStringArrayList(Keys.KEY_BLUETOOTH_MAC, deviceMAC);
        } else {
            bluetoothDataMap.putBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE, false);
        }
        bluetoothDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        return bluetoothData;
    }

    private PutDataMapRequest buildBluetoothAvailableDataItem(ArrayList<String> name,
                                                              ArrayList<String> mac) {
        PutDataMapRequest bluetoothData = PutDataMapRequest.create(Paths.PATH_BLUETOOTH_AVAILABLE);
        DataMap bluetoothDataMap = bluetoothData.getDataMap();
        if (name.isEmpty() || mac.isEmpty()) {
            bluetoothDataMap.putBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE, false);
        } else {
            bluetoothDataMap.putBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE, true);
            bluetoothDataMap.putStringArrayList(Keys.KEY_BLUETOOTH_NAME, name);
            bluetoothDataMap.putStringArrayList(Keys.KEY_BLUETOOTH_MAC, mac);
        }
        bluetoothDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        return bluetoothData;
    }

    private BroadcastReceiver mScanResultsReceiver = new BroadcastReceiver() {
        ArrayList<String> mDeviceName = new ArrayList<>();
        ArrayList<String> mDeviceMac = new ArrayList<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        Log.d(TAG, "Scanning for available Bluetooth Devices");
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        addFoundBluetoothDevice((BluetoothDevice)
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        mDataTransmitter
                                .syncDataItem(buildBluetoothAvailableDataItem(mDeviceName, mDeviceMac));
                        mContext.unregisterReceiver(mScanResultsReceiver);
                        break;
                    default:
                        break;
                }
            }
        }

        private void addFoundBluetoothDevice(BluetoothDevice device) {
            if (device != null && device.getName() != null && device.getAddress() != null) {
                mDeviceName.add(device.getName());
                mDeviceMac.add(device.getAddress());
            }
        }
    };
}