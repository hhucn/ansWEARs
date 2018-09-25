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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * The PermissionManager is responsible for checking and requesting needed permissions.
 */
public class PermissionManager {
    private static final String TAG = "Permission-Manager";
    private Context mContext;

    PermissionManager(Context context) {
        mContext = context;
    }

    /**
     * Check if the permissions needed regarding a WiFi scan are given.
     * Request otherwise.
     *
     * @return true if the permission is granted, false otherwise
     */
    public boolean checkWifiPermission() {
        if (PackageManager.PERMISSION_DENIED == ContextCompat
                .checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{"android.permission.ACCESS_COARSE_LOCATION"},
                    30000);
        } else {
            Log.d(TAG, "Location-Permission granted");
            return true;
        }
        return false;
    }

    /**
     * Check if the permissions needed regarding a location scan are given.
     * Request otherwise.
     *
     * @return true if the permission is granted, false otherwise
     */
    public boolean checkLocationRequirements() {
        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) ||
                PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                    30001);
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{"android.permission.ACCESS_COARSE_LOCATION"},
                    30000);
        } else {
            Log.d(TAG, "Location-Permission granted");
            return true;
        }
        return false;
    }

    /**
     * Check if the permissions needed to use the microphone are given.
     * Request otherwise.
     *
     * @return true if the permission is set, false otherwise
     */
    public boolean checkRecordPermission() {
        if (PackageManager.PERMISSION_DENIED == ContextCompat
                .checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{"android.permission.RECORD_AUDIO"},
                    30002);
        } else {
            Log.d(TAG, "Record-Permission granted");
            return true;
        }
        return false;
    }

    /**
     * Check if the permissions needed to use the body sensors is given.
     * Request otherwise.
     *
     * @return true if the permission is set, false otherwise
     */
    public boolean checkBodySensorPermission() {
        if (PackageManager.PERMISSION_DENIED == ContextCompat
                .checkSelfPermission(mContext, Manifest.permission.BODY_SENSORS)) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{"android.permission.BODY_SENSORS"},
                    30003);
        } else {
            Log.d(TAG, "Record-Permission granted");
            return true;
        }
        return false;
    }
}
