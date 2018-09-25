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

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.WindowManager;

import com.google.android.gms.wearable.Wearable;

import de.opptain.answearscore.RequestManager;

public class MainActivity extends WearableActivity {

    private RequestManager mRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRequestManager = new RequestManager(this, findViewById(R.id.content));
        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(mRequestManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRequestManager.stopAudioRecording();
        Wearable.getMessageClient(this).removeListener(mRequestManager);
    }
}
