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
package de.opptain.constants;

public class Paths {
    // Paths for sync events
    public static final String PATH_BLUETOOTH_AVAILABLE = "/Network/Bluetooth/Available";
    public static final String PATH_BLUETOOTH_BONDED = "/Network/Bluetooth/Bounded";
    public static final String PATH_LOCATION_POSITION = "/Location/Position";
    public static final String PATH_LOCATION_SATELLITE = "/Location/Satellites";
    public static final String PATH_WIFI_SCAN = "/Network/Wifi";
    public static final String PATH_SENSORSERVICE = "/Sensors";
    public static final String PATH_WIFISTATE = "/Network/Wifi/Change";
    public static final String PATH_AUDIO = "/Audio/Sample";
    public static final String PATH_CONTROLELEMENT_TOUCH = "/Controlelement/Touch";
    public static final String LOCAL_PATH_AUDIO = "user_sample";

    // Paths for remote control
    public static final String PATH_REMOTE_WIFI_SCAN = "/Network/Wifi/Scan";
    public static final String PATH_REMOTE_BLUETOOTH_SCAN = "/Network/Bluetooth/Scan";
    public static final String PATH_REMOTE_LOCATION_SCAN = "/Location/Scan";
    public static final String PATH_REMOTE_SENSOR_SEPARATOR = ";";
    public static final String PATH_REMOTE_SENSORSERVICE_ON = "/Sensorservice/On";
    public static final String PATH_REMOTE_SENSORSERVICE_OFF = "/Sensorservice/Off";
    public static final String PATH_REMOTE_WIFI_DISCONNECT = "/Network/Wifi/Disconnect";
    public static final String PATH_REMOTE_WIFI_REFRESH = "/Network/Wifi/Refresh";
    public static final String PATH_REMOTE_WIFI_CONNECT = "/Network/Wifi/Connect";
    public static final String PATH_REMOTE_WIFI_CONNECT_SEPARATOR = ";";
    public static final String PATH_REMOTE_AUDIORECORD_START = "/Audio/Record/Start";
    public static final String PATH_REMOTE_AUDIORECORD_STOP = "/Audio/Record/Stop";
    public static final String PATH_REMOTE_CONTROLELEMENT_TOUCH = "/Controlelement/Touch";
    public static final String PATH_REMOTE_APPLICATION_TERMINATE = "/Terminate/All";

    private Paths() {
        throw new IllegalStateException("Utility class");
    }
}
