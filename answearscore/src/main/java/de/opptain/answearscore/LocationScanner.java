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

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The LocationScanner is responsible for returning the current location of the device.
 * The Permission {@code android.permission.ACCESS_COARSE_LOCATION} is required.
 * The Permission {@code android.permission.ACCESS_FINE_LOCATION} is required.
 */
public class LocationScanner implements LocationListener, GpsStatus.Listener {
    private static final String TAG = "Location-Scanner";
    private Context mContext;
    private DataTransmitter mDataTransmitter;
    private LocationManager mLocationManager;

    LocationScanner(Context context, DataTransmitter dataTransmitter) {
        mContext = context;
        mDataTransmitter = dataTransmitter;
        mLocationManager = (LocationManager) mContext.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Initiate a location scan or return last known location if it is not older than 1 minute
     */
    @SuppressLint("MissingPermission")
    public void startLocationScan() {
        String provider = mLocationManager.getBestProvider(new Criteria(), false);
        if (provider != null) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            mLocationManager.addGpsStatusListener(this);
            if (location != null && locationIsUpToDate(location.getTime())) {
                onLocationChanged(location);
            } else {
                mLocationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        } else {
            Log.e(TAG, "No Provider was found. This device may not have its own GPS-Module");
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onGpsStatusChanged(int i) {
        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
        if (gpsStatus != null) {
            int satellites = getSize(gpsStatus.getSatellites());
            mDataTransmitter.syncDataItem(buildSatelliteDataItem(satellites));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        String locationName = getLocationName(latitude, longitude);
        String time = getLocalTime(location.getTime());
        int satellites = location.getExtras().getInt("satellites");
        mDataTransmitter.syncDataItem(
                buildLocationDataItem(longitude, latitude, locationName, time, satellites));
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // Implementation will follow in future versions
    }

    @Override
    public void onProviderEnabled(String s) {
        // Implementation will follow in future versions
    }

    @Override
    public void onProviderDisabled(String s) {
        // Implementation will follow in future versions
    }

    private PutDataMapRequest buildSatelliteDataItem(int satellites) {
        PutDataMapRequest satelliteData = PutDataMapRequest.create(Paths.PATH_LOCATION_SATELLITE);
        DataMap satelliteDataMap = satelliteData.getDataMap();
        satelliteDataMap.putInt(Keys.KEY_LOCATION_SATELLITES_TOTAL, satellites);
        satelliteDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        return satelliteData;
    }

    private PutDataMapRequest buildLocationDataItem(double longitude, double latitude,
                                                    String locationName, String time, int satellites) {
        PutDataMapRequest locationData = PutDataMapRequest.create(Paths.PATH_LOCATION_POSITION);
        DataMap locationDataMap = locationData.getDataMap();
        locationDataMap.putDouble(Keys.KEY_LOCATION_LONGITUDE, longitude);
        locationDataMap.putDouble(Keys.KEY_LOCATION_LATITUDE, latitude);
        locationDataMap.putString(Keys.KEY_LOCATION_ADDRESS, locationName);
        locationDataMap.putString(Keys.KEY_LOCATION_LOCALTIME, time);
        locationDataMap.putInt(Keys.KEY_LOCATION_SATELLITES_USED, satellites);
        locationDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        return locationData;
    }

    private boolean locationIsUpToDate(long locationTime) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - locationTime;

        //Older than 1 minute, return false
        return timeDifference <= 60 * 1000;
    }

    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        StringBuilder locationName = new StringBuilder();
        try {
            List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
            if (address != null && !address.isEmpty()) {
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i = 0; i < maxLines; i++) {
                    locationName.append(address.get(0).getAddressLine(i));
                    locationName.append("\n");
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.d(TAG, "Failed to lookup address");
        }
        return locationName.toString();
    }

    private String getLocalTime(long locationTime) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone timeZone = TimeZone.getTimeZone(String.valueOf(Locale.getDefault()));
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date(locationTime));
    }

    private int getSize(Iterable<GpsSatellite> iterable) {
        int satellites = 0;
        for (GpsSatellite ignored : iterable) {
            satellites++;
        }
        return satellites;
    }
}
