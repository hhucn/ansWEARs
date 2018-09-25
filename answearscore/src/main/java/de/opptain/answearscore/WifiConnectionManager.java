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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The WifiConnectionManager is responsible for managing the essential Wifi functionality.
 * The Permission {@code android.permission.ACCESS_WIFI_STATE} is required.
 * The Permission {@code android.permission.CHANGE_WIFI_STATE} is required.
 * The Permission {@code android.permission.ACCESS_NETWORK_STATE} is required.
 */
public class WifiConnectionManager {
    private static final String TAG = "Wifi-Manager";
    private Context mContext;
    private DataTransmitter mDataTransmitter;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;

    WifiConnectionManager(Context context, DataTransmitter dataTransmitter) {
        mContext = context;
        mDataTransmitter = dataTransmitter;
        mConnectivityManager = (ConnectivityManager) mContext.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Return currently available Wifi access points
     */
    public void startWifiScan() {
        startScanResultReceiver();
        mWifiManager.setWifiEnabled(true);
        mWifiManager.startScan();
    }

    /**
     * Return current wifi connection state
     */
    public void getWifiStatus() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                String networkName = wifiInfo.getSSID();
                String ipAddress = convertIpAddress(wifiInfo.getIpAddress());
                mDataTransmitter.syncDataItem(buildStateDataItem(true, networkName, ipAddress));
            } else {
                mDataTransmitter.syncDataItem(buildStateDataItem(false, null, null));
            }
        }
    }

    /**
     * Connects to the given WiFi-Network
     *
     * @param networkName Name of the network
     * @param password    Password of the network
     */
    public void connect(String networkName, String password) {
        WifiConfiguration wifiConfiguration = generateWifiConfiguration(networkName, password);
        if (!activateWifiConfiguration(wifiConfiguration)) {
            Log.e(TAG, "The Wifi-Configuration could not be add to your networks.");
        }
    }

    /**
     * Disconnect from the currently connected WiFi-Network by deleting every WifiConfiguration.
     * Deletion is only possible on self created configurations using the connect() method.
     */
    public void disconnect() {
        boolean oldWifiEnabledState = mWifiManager.isWifiEnabled();
        if (!oldWifiEnabledState) {
            mWifiManager.setWifiEnabled(true);
            mWifiManager.disconnect();
        }
        for (WifiConfiguration wifiConfiguration : mWifiManager.getConfiguredNetworks()) {
            String networkSSID = wifiConfiguration.SSID;
            int networkID = wifiConfiguration.networkId;
            if (!mWifiManager.removeNetwork(networkID)) {
                Log.d(TAG, "Failed Removing: " + networkSSID);
            }
        }

        if (!oldWifiEnabledState) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    private WifiConfiguration generateWifiConfiguration(String networkName, String password) {
        if (networkName != null && networkName.length() != 0) {
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\"" + networkName + "\"";
            if (password != null && password.length() != 0) {
                wifiConfiguration.preSharedKey = "\"" + password + "\"";
                wifiConfiguration.allowedKeyManagement.set(1); //1 = WPA_PSK
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            } else {
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
            return wifiConfiguration;
        }
        return null;
    }

    private boolean activateWifiConfiguration(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration != null) {
            int networkID = mWifiManager.addNetwork(wifiConfiguration);
            if (networkID != 1) {
                mWifiManager.setWifiEnabled(true);
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(networkID, true);
                mWifiManager.reconnect();
                return true;
            }
        }
        return false;
    }

    private void startScanResultReceiver() {
        IntentFilter scanResultAvailableActionFilter = new IntentFilter();
        scanResultAvailableActionFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mScanResultsReceiver, scanResultAvailableActionFilter);
    }

    private PutDataMapRequest buildScanDataItem() {
        List<ScanResult> networks = mWifiManager.getScanResults();
        PutDataMapRequest wifiData = PutDataMapRequest.create(Paths.PATH_WIFI_SCAN);
        DataMap wifiDataMap = wifiData.getDataMap();
        if (networks != null && !networks.isEmpty()) {
            wifiDataMap.putBoolean(Keys.KEY_WIFI_FOUND, true);
            wifiDataMap.putStringArrayList(Keys.KEY_WIFI_SSID, getNetworkSSID(networks));
            wifiDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        } else {
            wifiDataMap.putBoolean(Keys.KEY_WIFI_FOUND, false);
            wifiDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        }
        return wifiData;
    }

    private PutDataMapRequest buildStateDataItem(Boolean isConnected, String ssid, String ipAddress) {
        PutDataMapRequest wifiStateData = PutDataMapRequest.create(Paths.PATH_WIFISTATE);
        DataMap wifiStateMap = wifiStateData.getDataMap();
        wifiStateMap.putBoolean(Keys.KEY_WIFI_CONNECTED, isConnected);
        wifiStateMap.putString(Keys.KEY_WIFI_SSID, ssid);
        wifiStateMap.putString(Keys.KEY_WIFI_IP_ADDRESS, ipAddress);
        wifiStateMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());

        return wifiStateData;
    }

    private ArrayList<String> getNetworkSSID(List<ScanResult> networks) {
        ArrayList<String> networkSSID = new ArrayList<>();
        for (ScanResult network : networks) {
            networkSSID.add(network.SSID);
        }
        return networkSSID;
    }

    private String convertIpAddress(int ipAddress) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    private BroadcastReceiver mScanResultsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDataTransmitter.syncDataItem(buildScanDataItem());
            context.unregisterReceiver(mScanResultsReceiver);
        }
    };
}
