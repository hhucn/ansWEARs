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

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Locale;

import de.opptain.constants.Paths;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        DataManager.OnRequestResultListener {

    private RequestTransmitter mRequestTransmitter;
    private DataManager mDataManager;
    private AudioPlayerManager mAudioPlayerManager;

    private TextView mAccelerometerX, mAccelerometerY, mAccelerometerZ;
    private TextView mTemperature;
    private TextView mGravityX, mGravityY, mGravityZ;
    private TextView mGyroscopeX, mGyroscopeY, mGyroscopeZ;
    private TextView mLight;
    private TextView mLinearAccelerationX, mLinearAccelerationY, mLinearAccelerationZ;
    private TextView mMagneticFieldX, mMagneticFieldY, mMagneticFieldZ;
    private TextView mPressure;
    private TextView mProximity;
    private TextView mHumidity;
    private TextView mRotationX, mRotationY, mRotationZ;
    private TextView mGamingRotationX, mGamingRotationY, mGamingRotationZ;
    private TextView mSteps;
    private TextView mHeartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRequestTransmitter = new RequestTransmitter(this);
        mDataManager = new DataManager(this, this);
        initializeSensorTextViews();
        startListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(mDataManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAudioPlayerManager.stop();
        Wearable.getDataClient(this).removeListener(mDataManager);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        switch (viewID) {
            case R.id.controlelement_button:
                changeControlElementTextView(true);
                mRequestTransmitter.sendControlElementActivateRequest();
                break;
            case R.id.locationscan_button:
                mRequestTransmitter.sendLocationRequest();
                break;
            case R.id.wificonnect_button:
                getWifiUserInput();
                break;
            case R.id.wifidisconnect_button:
                mRequestTransmitter.sendWifiDisconnectRequest();
                break;
            case R.id.wifirefresh_button:
                mRequestTransmitter.sendWifiRefreshRequest();
                break;
            case R.id.wifiscan_button:
                mRequestTransmitter.sendWifiScanRequest();
                break;
            case R.id.bluetoothscan_button:
                mRequestTransmitter.sendBluetoothScanRequest();
                break;
            case R.id.sensorstart_button:
                prepareSensorServiceRequest();
                break;
            case R.id.sensorstop_button:
                mRequestTransmitter.sendSensorServiceDisableRequest();
                setRequestServiceAvailable(true);
                break;
            case R.id.startrecord_button:
                mRequestTransmitter.sendRecordStartRequest();
                break;
            case R.id.stoprecord_button:
                mRequestTransmitter.sendRecordStopRequest();
                break;
            case R.id.playrecord_button:
                playWearableRecord();
                break;
            case R.id.terminate_button:
                mRequestTransmitter.sendTerminateRequest();
                break;
            default:
                break;
        }
    }

    @Override
    public void onControlElementEvent() {
        Toast.makeText(getApplicationContext(),
                getString(R.string.control_toast_text),
                Toast.LENGTH_SHORT
        ).show();
        changeControlElementTextView(false);
    }

    @Override
    public void onLocationFoundResult(
            Double longitude, Double latitude, String name, String time, int satellites) {
        TextView locationTextView = findViewById(R.id.location);
        locationTextView.setText(
                getString(R.string.location_found, name, longitude, latitude, satellites, time));
    }

    @Override
    public void onSatellitesFoundResult(int satellites) {
        TextView satelliteTextView = findViewById(R.id.satellites);
        satelliteTextView.setText(getString(R.string.location_satellites_found, satellites));
    }

    @Override
    public void onWifiStateResult(boolean isConnected, String ssid, String ipAddress) {
        TextView wifiStatusTextView = findViewById(R.id.wifistatus);
        if (isConnected) {
            wifiStatusTextView.setText(getString(R.string.wifi_connected, ssid, ipAddress));
        } else {
            wifiStatusTextView.setText(getString(R.string.wifi_no_connection));
        }
    }

    @Override
    public void onWifiScanResult(boolean wifiFound, ArrayList<String> networkSSID) {
        TextView wifiTextView = findViewById(R.id.wifi);
        if (wifiFound) {
            StringBuilder builder = new StringBuilder(50);
            for (String network : networkSSID) {
                builder.append(network).append("\n");
            }
            wifiTextView.setText(builder.toString());
        } else {
            wifiTextView.setText(getString(R.string.wifi_no_networks));
        }
    }

    @Override
    public void onBluetoothBondedResult(boolean bondFound, ArrayList<String> deviceName,
                                        ArrayList<String> deviceMAC) {
        TextView bluetoothBondedTextView = findViewById(R.id.bluetoothBounded);
        StringBuilder builder = new StringBuilder(50);
        if (bondFound) {
            builder.append(getString(R.string.bluetooth_bond_found));
            for (String name : deviceName) {
                builder.append(name).append("\n");
            }
        } else {
            builder.append(getString(R.string.bluetooth_bond_not_found));
        }
        bluetoothBondedTextView.setText(builder.toString());
    }

    @Override
    public void onBluetoothAvailableResult(boolean bondFound, ArrayList<String> deviceName,
                                           ArrayList<String> deviceMAC) {
        TextView bluetoothBondedTextView = findViewById(R.id.bluetoothAvailable);
        StringBuilder builder = new StringBuilder(50);
        if (bondFound) {
            builder.append(getString(R.string.bluetooth_available_found));
            for (String name : deviceName) {
                builder.append(name).append("\n");
            }
        } else {
            builder.append(getString(R.string.bluetooth_available_not_found));
        }
        bluetoothBondedTextView.setText(builder.toString());
    }

    @Override
    public void onSensorEvent(int type, float[] values, int accuracy) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mAccelerometerY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mAccelerometerZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mTemperature.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_GRAVITY:
                mGravityX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mGravityY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mGravityZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyroscopeX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mGyroscopeY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mGyroscopeZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_LIGHT:
                mLight.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                mLinearAccelerationX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mLinearAccelerationY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mLinearAccelerationZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticFieldX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mMagneticFieldY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mMagneticFieldZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_PRESSURE:
                mPressure.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_PROXIMITY:
                mProximity.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                mHumidity.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                mRotationX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mRotationY.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mRotationZ.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                mGamingRotationX.setText(String.format(Locale.ENGLISH, "%.2f", values[0]));
                mGamingRotationZ.setText(String.format(Locale.ENGLISH, "%.2f", values[1]));
                mGamingRotationY.setText(String.format(Locale.ENGLISH, "%.2f", values[2]));
                break;
            case Sensor.TYPE_STEP_COUNTER:
                mSteps.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            case Sensor.TYPE_HEART_RATE:
                mHeartRate.setText(String.format(Locale.getDefault(), "%f", values[0]));
                break;
            default:
                break;
        }
    }

    @Override
    public void onAudioEvent(boolean audioAvailable) {
        TextView audioStatusTextView = findViewById(R.id.audioStatus);
        if (audioAvailable) {
            audioStatusTextView.setText(getString(R.string.audio_sample_received));
        } else {
            audioStatusTextView.setText(getString(R.string.audio_sample_failed));
        }
    }

    private void changeControlElementTextView(boolean online) {
        TextView controlTextView = findViewById(R.id.controlElementTextView);
        if (online) {
            controlTextView.setText(getString(R.string.control_listener_text_on));
        } else {
            controlTextView.setText(getString(R.string.control_listener_text));
        }
    }

    private void getWifiUserInput() {
        LinearLayout inputMask = new LinearLayout(getApplicationContext());
        inputMask.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder wifiAlertDialog = new AlertDialog.Builder(this);
        wifiAlertDialog.setTitle(getString(R.string.wifi_connect_dialog_title));
        final EditText ssidInput = new EditText(this);
        final EditText passwordInput = new EditText(this);
        ssidInput.setHint(getString(R.string.wifi_connect_dialog_hint_name));
        passwordInput.setHint(getString(R.string.wifi_connect_dialog_hint_password));
        ssidInput.setInputType(InputType.TYPE_CLASS_TEXT);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputMask.addView(ssidInput);
        inputMask.addView(passwordInput);
        wifiAlertDialog.setView(inputMask);

        wifiAlertDialog.setPositiveButton(getString(R.string.wifi_connect_dialog_button_add),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ssid;
                        String password;
                        if (!ssidInput.getText().toString().equals("")) {
                            ssid = ssidInput.getText().toString();
                            password = passwordInput.getText().toString();
                            mRequestTransmitter.sendWifiConnectRequest(ssid, password);
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getString(R.string.wifi_connect_dialog_missing_name),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
        wifiAlertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        wifiAlertDialog.show();
    }

    private void prepareSensorServiceRequest() {
        String enabledSensors = getEnabledSensorSwitches();
        setRequestServiceAvailable(false);
        mRequestTransmitter.sendSensorServiceEnableRequest(enabledSensors);
    }

    private void setRequestServiceAvailable(boolean isAvailable) {
        Button enableButton = findViewById(R.id.sensorstart_button);
        Button disableButton = findViewById(R.id.sensorstop_button);
        if (isAvailable) {
            enableButton.setClickable(true);
            disableButton.setClickable(false);
        } else {
            enableButton.setClickable(false);
            disableButton.setClickable(true);
        }
    }

    private String getEnabledSensorSwitches() {
        StringBuilder enabledSensorBuilder = new StringBuilder();
        if (((Switch) findViewById(R.id.accelerometerSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_ACCELEROMETER)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.temperatureSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_AMBIENT_TEMPERATURE)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.gravitySwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_GRAVITY)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.gyroscopeSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_GYROSCOPE)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.lightSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_LIGHT)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.accelerationSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_LINEAR_ACCELERATION)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.magneticSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_MAGNETIC_FIELD)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.pressureSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_PRESSURE)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.proximitySwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_PROXIMITY)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.humiditySwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_RELATIVE_HUMIDITY)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.rotationSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_ROTATION_VECTOR)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.grotationSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_GAME_ROTATION_VECTOR)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.stepsSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_STEP_COUNTER)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        if (((Switch) findViewById(R.id.heartRateSwitch)).isChecked()) {
            enabledSensorBuilder
                    .append(Sensor.TYPE_HEART_RATE)
                    .append(Paths.PATH_REMOTE_SENSOR_SEPARATOR);
        }
        return enabledSensorBuilder.toString();
    }

    private void playWearableRecord() {
        mAudioPlayerManager = new AudioPlayerManager(this);
        mAudioPlayerManager.play();
    }

    private void initializeSensorTextViews() {
        mAccelerometerX = findViewById(R.id.accelerometerx);
        mAccelerometerY = findViewById(R.id.accelerometery);
        mAccelerometerZ = findViewById(R.id.accelerometerz);
        mTemperature = findViewById(R.id.temperature);
        mGravityX = findViewById(R.id.gravityx);
        mGravityY = findViewById(R.id.gravityy);
        mGravityZ = findViewById(R.id.gravityz);
        mGyroscopeX = findViewById(R.id.gyroscopex);
        mGyroscopeY = findViewById(R.id.gyroscopey);
        mGyroscopeZ = findViewById(R.id.gyroscopez);
        mLight = findViewById(R.id.light);
        mLinearAccelerationX = findViewById(R.id.accelerationx);
        mLinearAccelerationY = findViewById(R.id.accelerationy);
        mLinearAccelerationZ = findViewById(R.id.accelerationz);
        mMagneticFieldX = findViewById(R.id.magneticx);
        mMagneticFieldY = findViewById(R.id.magneticy);
        mMagneticFieldZ = findViewById(R.id.magneticz);
        mPressure = findViewById(R.id.pressure);
        mProximity = findViewById(R.id.proximity);
        mHumidity = findViewById(R.id.humidity);
        mRotationX = findViewById(R.id.rotationx);
        mRotationY = findViewById(R.id.rotationy);
        mRotationZ = findViewById(R.id.rotationz);
        mGamingRotationX = findViewById(R.id.grotationx);
        mGamingRotationY = findViewById(R.id.grotationy);
        mGamingRotationZ = findViewById(R.id.grotationz);
        mSteps = findViewById(R.id.steps);
        mHeartRate = findViewById(R.id.heartRate);
    }

    private void startListeners() {
        (findViewById(R.id.controlelement_button)).setOnClickListener(this);
        (findViewById(R.id.locationscan_button)).setOnClickListener(this);
        (findViewById(R.id.wificonnect_button)).setOnClickListener(this);
        (findViewById(R.id.wifidisconnect_button)).setOnClickListener(this);
        (findViewById(R.id.wifirefresh_button)).setOnClickListener(this);
        (findViewById(R.id.wifiscan_button)).setOnClickListener(this);
        (findViewById(R.id.bluetoothscan_button)).setOnClickListener(this);
        (findViewById(R.id.sensorstart_button)).setOnClickListener(this);
        (findViewById(R.id.sensorstop_button)).setOnClickListener(this);
        (findViewById(R.id.startrecord_button)).setOnClickListener(this);
        (findViewById(R.id.stoprecord_button)).setOnClickListener(this);
        (findViewById(R.id.playrecord_button)).setOnClickListener(this);
        (findViewById(R.id.terminate_button)).setOnClickListener(this);
    }
}
