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
package de.opptain.answears;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.opptain.constants.Capabilities;
import de.opptain.constants.Paths;

/**
 * The RequestTransmitter is responsible for creating the message requesting the desired service.
 */

public class RequestTransmitter {

    private static final String TAG = "Message_Manager";
    private Context mContext;

    RequestTransmitter(Context context) {
        mContext = context;
    }

    /**
     * Send a message that returns bond and available Bluetooth devices.
     */
    public void sendBluetoothScanRequest() {
        sendMessage(Paths.PATH_REMOTE_BLUETOOTH_SCAN, null);
    }

    /**
     * Send a message that returns the current location.
     */
    public void sendLocationRequest() {
        sendMessage(Paths.PATH_REMOTE_LOCATION_SCAN, null);
    }

    /**
     * Send a message that returns available WiFi networks.
     */
    public void sendWifiScanRequest() {
        sendMessage(Paths.PATH_REMOTE_WIFI_SCAN, null);
    }

    /**
     * Send a message that indicates to connect to the provided WiFi network
     *
     * @param networkName Network name.
     * @param password    Passphrase of the network.
     */
    public void sendWifiConnectRequest(String networkName, String password) {
        sendMessage(Paths.PATH_REMOTE_WIFI_CONNECT,
                networkName + Paths.PATH_REMOTE_WIFI_CONNECT_SEPARATOR + password);
    }

    /**
     * Send a message that indicates to disconnect from the currently active WiFi Network.
     * This is only possible, if it has been added by this application previously.
     */
    public void sendWifiDisconnectRequest() {
        sendMessage(Paths.PATH_REMOTE_WIFI_DISCONNECT, null);
    }

    /**
     * Send a message that returns the current WiFi state.
     */
    public void sendWifiRefreshRequest() {
        sendMessage(Paths.PATH_REMOTE_WIFI_REFRESH, null);
    }

    /**
     * Send a message that indicates to start the sensor service.
     *
     * @param enabledSensors String of sensors types that should be tracked.
     *                       Example: "1;2;3; ..."
     */
    public void sendSensorServiceEnableRequest(String enabledSensors) {
        Log.i(TAG, enabledSensors);
        sendMessage(Paths.PATH_REMOTE_SENSORSERVICE_ON, enabledSensors);
    }

    /**
     * Send a message to stop the sensor service.
     */
    public void sendSensorServiceDisableRequest() {
        sendMessage(Paths.PATH_REMOTE_SENSORSERVICE_OFF, null);
    }

    /**
     * Send a message to start the audio recording.
     */
    public void sendRecordStartRequest() {
        sendMessage(Paths.PATH_REMOTE_AUDIORECORD_START, null);
    }

    /**
     * Send a message to stop the audio recording.
     */
    public void sendRecordStopRequest() {
        sendMessage(Paths.PATH_REMOTE_AUDIORECORD_STOP, null);
    }

    /**
     * Send a message to activate the Touch listener.
     */
    public void sendControlElementActivateRequest() {
        sendMessage(Paths.PATH_REMOTE_CONTROLELEMENT_TOUCH, null);
    }

    /**
     * Send a message that indicates to finish the Wear application.
     */
    public void sendTerminateRequest() {
        sendMessage(Paths.PATH_REMOTE_APPLICATION_TERMINATE, null);
    }

    private void sendMessage(final String path, final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Set<Node> reachableNodes = getReachableNodes(Capabilities.CAPABILITY_DATA);
                if (reachableNodes != null && !reachableNodes.isEmpty()) {
                    for (Node node : reachableNodes) {
                        Task<Integer> sendTask =
                                Wearable.getMessageClient(mContext).sendMessage(
                                        node.getId(), path, dataToByte(data)
                                );
                        sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                Log.d(TAG, "Message was transmitted");
                            }
                        });

                        sendTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Failed to transmit message");
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "No connected nodes found");
                }
            }
        }).start();
    }

    private Set<Node> getReachableNodes(String capability) {
        try {
            CapabilityInfo capabilityInfo = Tasks.await(
                    Wearable.getCapabilityClient(mContext).getCapability(
                            capability, CapabilityClient.FILTER_REACHABLE));
            return capabilityInfo.getNodes();
        } catch (ExecutionException e) {
            Log.d(TAG, "Failed to reach capable nodes");
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            Log.d(TAG, "Failed to reach capable nodes");
            Log.e(TAG, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return Collections.emptySet();
    }

    private byte[] dataToByte(String data) {
        if (data != null) {
            return data.getBytes();
        }
        return new byte[0];
    }
}
