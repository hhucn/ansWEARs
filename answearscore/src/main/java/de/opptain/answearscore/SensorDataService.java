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

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The task of the SensorDataService is to listen to any sensor event determined by the user.
 * Requested sensor types are included in the intent starting this service.
 * The permission {@code android.permission.BODY_SENSORS} is required.
 */
public class SensorDataService extends Service implements SensorEventListener {

    private static final String TAG = "Sensor-Service";
    private SensorManager mSensorManager;
    private DataTransmitter mDataTransmitter;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDataTransmitter = new DataTransmitter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            HashSet<Integer> enabledSensors =
                    getRequestedSensors(intent.getStringExtra(Keys.KEY_SENSOR_INTENT_EXTRA));
            startSensorListener(enabledSensors);
        } catch (NullPointerException e) {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mDataTransmitter.syncDataItem(buildSensorDataItem(sensorEvent));
    }

    private PutDataMapRequest buildSensorDataItem(SensorEvent sensorEvent) {
        PutDataMapRequest sensorData = PutDataMapRequest.create(Paths.PATH_SENSORSERVICE);
        DataMap sensorDataMap = sensorData.getDataMap();
        sensorDataMap.putInt(Keys.KEY_SENSORS_TYPE, sensorEvent.sensor.getType());
        sensorDataMap.putFloatArray(Keys.KEY_SENSORS_VALUES, sensorEvent.values);
        sensorDataMap.putInt(Keys.KEY_SENSORS_ACCURACY, sensorEvent.accuracy);
        sensorDataMap.putLong(Keys.KEY_TIMESTAMP, sensorEvent.timestamp);
        return sensorData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Will be processed on the mobile device
    }

    private void startSensorListener(HashSet<Integer> enabledSensors) {

        Sensor accelerometer, temperature, gravity, gyroscope, light, linearAcceleration,
                magneticField, pressure, proximity, humidity, rotationVector, gameRotationVector,
                stepCounter, heartRate;

        if (mSensorManager != null) {
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            temperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            light = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            linearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            pressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            proximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            humidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            gameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            heartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            if (isRequested(accelerometer, Sensor.TYPE_ACCELEROMETER, enabledSensors)) {
                mSensorManager.registerListener(this,
                        accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(temperature, Sensor.TYPE_AMBIENT_TEMPERATURE, enabledSensors)) {
                mSensorManager.registerListener(this,
                        temperature, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(gravity, Sensor.TYPE_GRAVITY, enabledSensors)) {
                mSensorManager.registerListener(this,
                        gravity, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(gyroscope, Sensor.TYPE_GYROSCOPE, enabledSensors)) {
                mSensorManager.registerListener(this,
                        gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(light, Sensor.TYPE_LIGHT, enabledSensors)) {
                mSensorManager.registerListener(this,
                        light, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(linearAcceleration, Sensor.TYPE_LINEAR_ACCELERATION, enabledSensors)) {
                mSensorManager.registerListener(this,
                        linearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(magneticField, Sensor.TYPE_MAGNETIC_FIELD, enabledSensors)) {
                mSensorManager.registerListener(this,
                        magneticField, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(pressure, Sensor.TYPE_PRESSURE, enabledSensors)) {
                mSensorManager.registerListener(this,
                        pressure, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(proximity, Sensor.TYPE_PROXIMITY, enabledSensors)) {
                mSensorManager.registerListener(this,
                        proximity, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(humidity, Sensor.TYPE_RELATIVE_HUMIDITY, enabledSensors)) {
                mSensorManager.registerListener(this,
                        humidity, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(rotationVector, Sensor.TYPE_ROTATION_VECTOR, enabledSensors)) {
                mSensorManager.registerListener(this,
                        rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(gameRotationVector, Sensor.TYPE_GAME_ROTATION_VECTOR, enabledSensors)) {
                mSensorManager.registerListener(this,
                        gameRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (isRequested(stepCounter, Sensor.TYPE_STEP_COUNTER, enabledSensors)) {
                mSensorManager.registerListener(this,
                        stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (isRequested(heartRate, Sensor.TYPE_HEART_RATE, enabledSensors)) {
                mSensorManager.registerListener(this,
                        heartRate, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    private boolean isRequested(Sensor sensor, Integer sensorType, HashSet<Integer> enabledSensors) {
        if (sensor != null) {
            if (enabledSensors.contains(sensorType)) {
                return true;
            } else {
                Log.d(TAG, String.format(Locale.getDefault(),
                        "[%d] Sensor not requested", sensorType));
                return false;
            }
        }
        Log.d(TAG, String.format(Locale.getDefault(), "[%d] Sensor not available", sensorType));
        return false;
    }

    private HashSet<Integer> getRequestedSensors(String sensors) {
        HashSet<Integer> sensorSet = new HashSet<>();
        for (String sensorID : sensors.split("\\;")) {
            sensorSet.add(Integer.valueOf(sensorID));
        }
        return sensorSet;
    }
}
