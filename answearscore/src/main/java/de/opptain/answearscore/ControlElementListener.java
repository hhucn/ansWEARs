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

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The ControlElementListener is responsible for activating a Touch-Listener on the View
 * of the launching Context.
 */
public class ControlElementListener implements View.OnTouchListener {

    private DataTransmitter mDataTransmitter;

    ControlElementListener(DataTransmitter dataTransmitter) {
        mDataTransmitter = dataTransmitter;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mDataTransmitter.syncDataItem(buildTouchDataItem());
        Log.d("Control-Element", "TOUCHY-TOUCHY");
        view.setOnTouchListener(null);
        return false;
    }

    private PutDataMapRequest buildTouchDataItem() {
        PutDataMapRequest touchData = PutDataMapRequest.create(Paths.PATH_CONTROLELEMENT_TOUCH);
        DataMap touchDataMap = touchData.getDataMap();
        touchDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
        return touchData;
    }
}
