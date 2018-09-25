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

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class WifiHotspotManager {

    private static final int WIFI_AP_STATE_DISABLING = 10;
    private static final int WIFI_AP_STATE_DISABLED = 11;
    private static final int WIFI_AP_STATE_ENABLING = 12;
    private static final int WIFI_AP_STATE_ENABLED = 13;
    private static final int WIFI_AP_STATE_FAILED = 14;

    private static final String TAG = "Hotspot-Manager";
    private Context mContext;
    private WifiManager mWifiManager;

    public WifiHotspotManager(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void openHotspot() {
        try {
            mWifiManager.setWifiEnabled(false);
            Method method = mWifiManager.getClass().getMethod
                    ("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean invokeStatus =
                    (boolean) method.invoke(mWifiManager, getWifiConfiguration(), true);
            Log.d(TAG, "Open-AP status: " + invokeStatus);
        } catch (Exception e) {
            Log.d(TAG, "Failed to open Hotspot");
            Log.e(TAG, e.getMessage());
        }
    }

    public void closeHotspot() {
        try {
            Method method = mWifiManager.getClass().getMethod
                    ("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean invokeStatus =
                    (boolean) method.invoke(mWifiManager, null, false);
            mWifiManager.setWifiEnabled(true);
            Log.d(TAG, "Shut-Down AP status: " + invokeStatus);
        } catch (Exception e) {
            Log.d(TAG, "Failed to close Hotspot");
            Log.e(TAG, e.getMessage());
        }
    }

    public void getHotspotState() {
        try {
            Method method = mWifiManager.getClass().getMethod
                    ("getWifiApState");
            int state = (int) method.invoke(mWifiManager);
            Log.d(TAG, "AP_STATE: " + getHotspotStateAsString(state));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private WifiConfiguration getWifiConfiguration() {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "oppWear";
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        Log.d(TAG, "Hidden:" + wifiConfiguration.hiddenSSID);
        return wifiConfiguration;
    }

    private String getHotspotStateAsString(int state) {
        switch (state) {
            case WIFI_AP_STATE_DISABLING:
                return "Disabling";
            case WIFI_AP_STATE_DISABLED:
                return "Disabled";
            case WIFI_AP_STATE_ENABLING:
                return "Enabling";
            case WIFI_AP_STATE_ENABLED:
                return "Enabled";
            case WIFI_AP_STATE_FAILED:
                return "Failed";
            default:
                return "Unknown";
        }
    }
}
