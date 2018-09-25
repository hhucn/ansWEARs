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
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * The DataTransmitters' tasks is to synchronize DataItems within the Wear Network.
 */
public class DataTransmitter {

    private static final String TAG = "Data-Transmitter";
    private Context mContext;
    private DataClient mDataClient;

    DataTransmitter(Context context) {
        mContext = context;
        mDataClient = Wearable.getDataClient(mContext);
    }

    /**
     * Synchronize given DataItem with all network participants
     *
     * @param dataItem Contains the results from the demanded service
     */
    public void syncDataItem(PutDataMapRequest dataItem) {
        try {
            if (dataItem != null) {
                dataItem.setUrgent();
                Task<DataItem> putDataTask = mDataClient.putDataItem(dataItem.asPutDataRequest());
                putDataTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Data item successfully transmitted");
                    }
                });

                putDataTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to transmit data item");
                    }
                });
            } else {
                Log.d(TAG, "Null-DataItem received!");
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "Sync-Event failed!");
            Log.e(TAG, e.getMessage());
        }
    }
}
