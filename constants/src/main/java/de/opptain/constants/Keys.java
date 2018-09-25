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

public class Keys {
    public static final String KEY_TIMESTAMP = "TIMESTAMP";
    public static final String KEY_BLUETOOTH_NAME = "HUB_NAME";
    public static final String KEY_BLUETOOTH_MAC = "HUB_MAC";
    public static final String KEY_BLUETOOTH_FOUND_DEVICE = "FOUND";
    public static final String KEY_LOCATION_SATELLITES_TOTAL = "SATELLITES_TOTAL";
    public static final String KEY_LOCATION_SATELLITES_USED = "SATELLITES_USED";
    public static final String KEY_LOCATION_ADDRESS = "ADDRESS";
    public static final String KEY_LOCATION_LONGITUDE = "LONGITUDE";
    public static final String KEY_LOCATION_LATITUDE = "LATITUDE";
    public static final String KEY_LOCATION_LOCALTIME = "LOCALTIME";
    public static final String KEY_WIFI_SSID = "SSID";
    public static final String KEY_WIFI_IP_ADDRESS = "IP_ADDRESS";
    public static final String KEY_WIFI_BSSID = "BSSID";
    public static final String KEY_WIFI_FOUND = "FOUND";
    public static final String KEY_WIFI_CONNECTED = "WIFI_CONNECTED";
    public static final String KEY_SENSOR_INTENT_EXTRA = "SENSORS";
    public static final String KEY_SENSORS_TYPE = "SENSORTYPE";
    public static final String KEY_SENSORS_ACCURACY = "ACCURACY";
    public static final String KEY_SENSORS_VALUES = "VALUES";
    public static final String KEY_AUDIO_SAMPLE = "AUDIO";

    private Keys() {
        throw new IllegalStateException("Utility class");
    }
}
