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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.util.Iterator;
import java.util.List;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The RequestManager is responsible for receiving mobile requests.
 * Based on the request, the desired service will be started.
 */

public class RequestManager implements MessageClient.OnMessageReceivedListener {
    private static final String TAG = "Message_Manager";
    private Context mContext;
    private View mView;
    private DataTransmitter mDataTransmitter;
    private PermissionManager mPermissionManager;
    private AudioRecorderManager mAudioRecorderManager;

    public RequestManager(Context context, View view) {
        mContext = context;
        mDataTransmitter = new DataTransmitter(mContext);
        mPermissionManager = new PermissionManager(mContext);
        if (view != null) {
            mView = view;
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String data;
        String path = messageEvent.getPath();
        Log.d(TAG, path);

        switch (path) {
            case Paths.PATH_REMOTE_WIFI_SCAN:
                respondToWifiScanRequest();
                break;
            case Paths.PATH_REMOTE_BLUETOOTH_SCAN:
                respondToBluetoothScanRequest();
                break;
            case Paths.PATH_REMOTE_SENSORSERVICE_ON:
                data = new String(messageEvent.getData());
                respondToStartSensorServiceRequest(data);
                break;
            case Paths.PATH_REMOTE_SENSORSERVICE_OFF:
                respondToStopSensorServiceRequest();
                break;
            case Paths.PATH_REMOTE_LOCATION_SCAN:
                respondToLocationScanRequest();
                break;
            case Paths.PATH_REMOTE_WIFI_CONNECT:
                data = new String(messageEvent.getData());
                respondToWifiConnectRequest(data);
                break;
            case Paths.PATH_REMOTE_WIFI_REFRESH:
                respondToWifiRefreshRequest();
                break;
            case Paths.PATH_REMOTE_WIFI_DISCONNECT:
                respondToWifiDisconnectRequest();
                break;
            case Paths.PATH_REMOTE_AUDIORECORD_START:
                respondToStartRecordRequest();
                break;
            case Paths.PATH_REMOTE_AUDIORECORD_STOP:
                respondToStopRecordRequest();
                break;
            case Paths.PATH_REMOTE_CONTROLELEMENT_TOUCH:
                activateTouchListener();
                break;
            case Paths.PATH_REMOTE_APPLICATION_TERMINATE:
                respondToTerminateRequest();
                break;
            default:
                break;
        }
    }

    /**
     * Will stop any active audio recording.
     */
    public void stopAudioRecording() {
        if (mAudioRecorderManager != null) {
            mAudioRecorderManager.stopRecording();
        }
    }

    private void respondToWifiScanRequest() {
        if (mPermissionManager.checkWifiPermission()) {
            new WifiConnectionManager(mContext, mDataTransmitter).startWifiScan();
        }
    }

    private void respondToBluetoothScanRequest() {
        BluetoothScanner bluetoothScanner = new BluetoothScanner(mContext, mDataTransmitter);
        bluetoothScanner.startBluetoothScan();
    }

    private void respondToStartSensorServiceRequest(String data) {
        if (mPermissionManager.checkBodySensorPermission()) {
            Intent sensorServiceIntent = new Intent(mContext, SensorDataService.class)
                    .putExtra(Keys.KEY_SENSOR_INTENT_EXTRA, data);
            mContext.startService(sensorServiceIntent);
        }
    }

    private void respondToStopSensorServiceRequest() {
        killSensorService();
    }

    private void respondToLocationScanRequest() {
        if (mPermissionManager.checkLocationRequirements()) {
            new LocationScanner(mContext, mDataTransmitter).startLocationScan();
        }
    }

    private void respondToWifiConnectRequest(String data) {
        String networkName = getNetworkName(data);
        String password = getNetworkPassword(data);
        new WifiConnectionManager(mContext, mDataTransmitter).connect(networkName, password);
    }

    private void respondToWifiRefreshRequest() {
        new WifiConnectionManager(mContext, mDataTransmitter).getWifiStatus();
    }

    private void respondToWifiDisconnectRequest() {
        new WifiConnectionManager(mContext, mDataTransmitter).disconnect();
    }

    private void respondToStartRecordRequest() {
        if (mPermissionManager.checkRecordPermission()) {
            mAudioRecorderManager = new AudioRecorderManager(mContext, mDataTransmitter);
            mAudioRecorderManager.startRecording();
        }
    }

    private void respondToStopRecordRequest() {
        if (mAudioRecorderManager != null) {
            mAudioRecorderManager.stopRecording();
        }
    }

    private void activateTouchListener() {
        mView.setOnTouchListener(new ControlElementListener(mDataTransmitter));
    }

    private void respondToTerminateRequest() {
        killSensorService();
        ((Activity) mContext).finish();
    }

    private void killSensorService() {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo =
                    activityManager.getRunningAppProcesses();
            if (runningAppProcessInfo != null) {
                Iterator<ActivityManager.RunningAppProcessInfo> iterator =
                        runningAppProcessInfo.iterator();
                String myProcess = mContext.getPackageName() + ":sensor_process";

                while (iterator.hasNext()) {
                    ActivityManager.RunningAppProcessInfo next = iterator.next();

                    if (next.processName.equals(myProcess)) {
                        android.os.Process.killProcess(next.pid);
                        break;
                    }
                }
            }
        }
    }

    private String getNetworkName(String data) {
        try {
            return data.split("\\" + Paths.PATH_REMOTE_WIFI_CONNECT_SEPARATOR)[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String getNetworkPassword(String data) {
        try {
            return data.split("\\" + Paths.PATH_REMOTE_WIFI_CONNECT_SEPARATOR)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
