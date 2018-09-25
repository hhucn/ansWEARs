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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The DataManager is responsible for listing to incoming synchronization events in the Wear Network.
 */
public class DataManager implements DataClient.OnDataChangedListener {

    private static final String TAG = "Data-Manager";
    private Context mContext;
    private OnRequestResultListener mSubscriber;

    DataManager(OnRequestResultListener subscriber, Context context) {
        mContext = context;
        mSubscriber = subscriber;
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                DataMap dataItemMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                Log.d(TAG, "Event" + dataItem.getUri().getPath());

                switch (dataItem.getUri().getPath()) {
                    case Paths.PATH_CONTROLELEMENT_TOUCH:
                        mSubscriber.onControlElementEvent();
                        break;
                    case Paths.PATH_LOCATION_POSITION:
                        unzipLocationMap(dataItemMap);
                        break;
                    case Paths.PATH_LOCATION_SATELLITE:
                        unzipSatelliteMap(dataItemMap);
                        break;
                    case Paths.PATH_WIFISTATE:
                        unzipWifiStateResult(dataItemMap);
                        break;
                    case Paths.PATH_WIFI_SCAN:
                        unzipWifiScanResult(dataItemMap);
                        break;
                    case Paths.PATH_BLUETOOTH_BONDED:
                        unzipBluetoothBondedResult(dataItemMap);
                        break;
                    case Paths.PATH_BLUETOOTH_AVAILABLE:
                        unzipBluetoothAvailableResult(dataItemMap);
                        break;
                    case Paths.PATH_SENSORSERVICE:
                        unzipSensorEvent(dataItemMap);
                        break;
                    case Paths.PATH_AUDIO:
                        unzipAudioSample(dataItemMap);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void unzipLocationMap(DataMap dataItemMap) {
        Double longitude = dataItemMap.getDouble(Keys.KEY_LOCATION_LONGITUDE);
        Double latitude = dataItemMap.getDouble(Keys.KEY_LOCATION_LATITUDE);
        String locationName = dataItemMap.getString(Keys.KEY_LOCATION_ADDRESS);
        String time = dataItemMap.getString(Keys.KEY_LOCATION_LOCALTIME);
        Integer satellites = dataItemMap.getInt(Keys.KEY_LOCATION_SATELLITES_USED);
        mSubscriber.onLocationFoundResult(longitude, latitude, locationName, time, satellites);
    }

    private void unzipSatelliteMap(DataMap dataItemMap) {
        int satellites = dataItemMap.getInt(Keys.KEY_LOCATION_SATELLITES_TOTAL);
        mSubscriber.onSatellitesFoundResult(satellites);
    }

    private void unzipWifiStateResult(DataMap dataItemMap) {
        boolean isConnected = dataItemMap.getBoolean(Keys.KEY_WIFI_CONNECTED);
        String ssid = dataItemMap.getString(Keys.KEY_WIFI_SSID);
        String ipAddress = dataItemMap.getString(Keys.KEY_WIFI_IP_ADDRESS);
        mSubscriber.onWifiStateResult(isConnected, ssid, ipAddress);
    }

    private void unzipWifiScanResult(DataMap dataItemMap) {
        boolean wifiFound = dataItemMap.getBoolean(Keys.KEY_WIFI_FOUND);
        ArrayList<String> networkSSID = dataItemMap.getStringArrayList(Keys.KEY_WIFI_SSID);
        mSubscriber.onWifiScanResult(wifiFound, networkSSID);
    }

    private void unzipBluetoothBondedResult(DataMap dataItemMap) {
        boolean deviceFound = dataItemMap.getBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE);
        ArrayList<String> deviceName = dataItemMap.getStringArrayList(Keys.KEY_BLUETOOTH_NAME);
        ArrayList<String> deviceMAC = dataItemMap.getStringArrayList(Keys.KEY_BLUETOOTH_MAC);
        mSubscriber.onBluetoothBondedResult(deviceFound, deviceName, deviceMAC);
    }

    private void unzipBluetoothAvailableResult(DataMap dataItemMap) {
        boolean deviceFound = dataItemMap.getBoolean(Keys.KEY_BLUETOOTH_FOUND_DEVICE);
        ArrayList<String> deviceName = dataItemMap.getStringArrayList(Keys.KEY_BLUETOOTH_NAME);
        ArrayList<String> deviceMAC = dataItemMap.getStringArrayList(Keys.KEY_BLUETOOTH_MAC);
        mSubscriber.onBluetoothAvailableResult(deviceFound, deviceName, deviceMAC);
    }

    private void unzipSensorEvent(DataMap dataItemMap) {
        int type = dataItemMap.getInt(Keys.KEY_SENSORS_TYPE);
        float[] values = dataItemMap.getFloatArray(Keys.KEY_SENSORS_VALUES);
        int accuracy = dataItemMap.getInt(Keys.KEY_SENSORS_ACCURACY);
        mSubscriber.onSensorEvent(type, values, accuracy);
    }

    private void unzipAudioSample(DataMap dataItemMap) {
        Asset audioSampleAsset = dataItemMap.getAsset(Keys.KEY_AUDIO_SAMPLE);
        new AudioAssetDownloader().execute(audioSampleAsset);
    }

    private class AudioAssetDownloader extends AsyncTask<Asset, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Asset... assets) {
            if (assets != null && assets[0] != null) {
                try (InputStream inputStream = Tasks.await(Wearable.getDataClient(mContext)
                        .getFdForAsset(assets[0])).getInputStream()) {
                    if (inputStream != null && writeAudioSample(downloadAudioData(inputStream))) {
                        return true;
                    }
                } catch (ExecutionException | IOException e) {
                    Log.e(TAG, e.getMessage());
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            Log.e(TAG, "Write/Read Failed!");
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mSubscriber.onAudioEvent(result);
        }

        private byte[] downloadAudioData(InputStream inputStream) {
            try (ByteArrayOutputStream audioOutputStream = new ByteArrayOutputStream()) {
                int length;
                byte[] buffer = new byte[1024];
                while ((length = inputStream.read(buffer)) != -1) {
                    audioOutputStream.write(buffer, 0, length);
                }
                return audioOutputStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return new byte[0];
        }

        private boolean writeAudioSample(byte[] audio) {
            if (audio != null && audio.length > 0) {
                try (FileOutputStream fileOutputStream =
                             mContext.openFileOutput(Paths.LOCAL_PATH_AUDIO, Context.MODE_PRIVATE);
                     DataOutputStream dataOutputStream =
                             new DataOutputStream(new BufferedOutputStream(fileOutputStream))) {

                    dataOutputStream.write(audio);
                    dataOutputStream.flush();
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return false;
        }
    }

    /**
     * Implementing the OnRequestResultListener interface ensured getting the received data
     */
    interface OnRequestResultListener {
        /**
         * Reports an incoming touch event
         */
        void onControlElementEvent();

        /**
         * Reports the result of the previous location scan
         *
         * @param longitude  Longitude of current position
         * @param latitude   Latitude of current position
         * @param name       Address of the current position
         * @param time       Time when the position was fixed
         * @param satellites Number of satellites used for determining the current position
         */
        void onLocationFoundResult(Double longitude, Double latitude,
                                   String name, String time, int satellites);

        /**
         * Reports a change of currently available satellites
         *
         * @param satellites number of available satellites
         */
        void onSatellitesFoundResult(int satellites);

        /**
         * Reports the currently active WiFi connection
         *
         * @param isConnected True if there is any active WiFi connection, false otherwise
         * @param ssid        Network name connected to
         * @param ipAddress   IP-Address granted in the current network
         */
        void onWifiStateResult(boolean isConnected, String ssid, String ipAddress);

        /**
         * Reports all available WiFi networks found with the last WiFi scan
         *
         * @param wifiFound   True if any network was found, false otherwise
         * @param networkSSID Names of the networks found
         */
        void onWifiScanResult(boolean wifiFound, ArrayList<String> networkSSID);

        /**
         * Reports all bonded Bluetooth devices
         *
         * @param bondFound  True if any device is bond, false otherwise
         * @param deviceName Names of the devices
         * @param deviceMac  MACs of the devices
         */
        void onBluetoothBondedResult(boolean bondFound, ArrayList<String> deviceName,
                                     ArrayList<String> deviceMac);

        /**
         * Reports all available Bluetooth devices
         *
         * @param bondFound  True if any device is found, false otherwise
         * @param deviceName Names of the devices
         * @param deviceMac  MACs of the devices
         */
        void onBluetoothAvailableResult(boolean bondFound, ArrayList<String> deviceName,
                                        ArrayList<String> deviceMac);

        /**
         * Reports an sensor event
         *
         * @param type     Type of the sensor changed
         * @param values   Data describing the sensor event
         * @param accuracy Reliability of the values received
         * @see android.hardware.SensorEvent
         */
        void onSensorEvent(int type, float[] values, int accuracy);

        /**
         * Reports an incoming audio sample
         *
         * @param audioAvailable True if record could be downloaded successfully, false otherwise
         */
        void onAudioEvent(boolean audioAvailable);
    }
}
