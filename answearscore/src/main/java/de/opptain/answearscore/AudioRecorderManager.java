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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.opptain.constants.Keys;
import de.opptain.constants.Paths;

/**
 * The AudioRecorderManager is responsible for recording the audio sample.
 * The permission {@code android.permission.RECORD_AUDIO} is required.
 */
public class AudioRecorderManager {
    private static final String TAG = "Audio-Manager";

    private Context mContext;
    private DataTransmitter mDataTransmitter;
    private AudioRecorder mRecorder;

    AudioRecorderManager(Context context, DataTransmitter dataTransmitter) {
        mContext = context;
        mDataTransmitter = dataTransmitter;
    }

    /**
     * Starts audio recording. The audio sample is saved locally.
     */
    public void startRecording() {
        mRecorder = new AudioRecorder();
        mRecorder.execute();
        Log.i(TAG, "Recording started!");
    }

    /**
     * Stops the recording process.
     */
    public void stopRecording() {
        if (mRecorder != null) {
            mRecorder.cancel(true);
            Log.i(TAG, "Recording stopped!");
        }
    }

    private PutDataMapRequest buildAudioDataItem() {
        byte[] audio = readAudioData();
        if (audio != null && audio.length > 0) {
            PutDataMapRequest audioData = PutDataMapRequest.create(Paths.PATH_AUDIO);
            DataMap audioDataMap = audioData.getDataMap();
            audioDataMap.putAsset(Keys.KEY_AUDIO_SAMPLE, Asset.createFromBytes(audio));
            audioDataMap.putLong(Keys.KEY_TIMESTAMP, System.currentTimeMillis());
            return audioData;
        }
        return null;
    }

    private byte[] readAudioData() {
        try (InputStream audioInputStream = mContext.openFileInput(Paths.LOCAL_PATH_AUDIO)) {
            int length;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream audioSample = new ByteArrayOutputStream();
            while ((length = audioInputStream.read(buffer)) != -1) {
                audioSample.write(buffer, 0, length);
            }
            return audioSample.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Log.d(TAG, "Failed reading file");
        }
        return new byte[0];
    }

    private class AudioRecorder extends AsyncTask<Void, Void, Void> {
        private int encoding = AudioFormat.ENCODING_PCM_16BIT;
        private int sampleRate = 44100;
        private int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT;
        private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, encoding);

        @Override
        protected Void doInBackground(Void... voids) {
            try (FileOutputStream fileOutputStream =
                         mContext.openFileOutput(Paths.LOCAL_PATH_AUDIO, Context.MODE_PRIVATE);
                 DataOutputStream outputStream =
                         new DataOutputStream(new BufferedOutputStream(fileOutputStream))) {

                AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, channelConfiguration,
                        encoding, bufferSize);
                short[] buffer = new short[bufferSize];

                recorder.startRecording();
                while (!isCancelled()) {
                    int cycles = recorder.read(buffer, 0, bufferSize);
                    for (int index = 0; index < cycles; index++) {
                        outputStream.writeShort(buffer[index]);
                    }
                }
                recorder.stop();
                mDataTransmitter.syncDataItem(buildAudioDataItem());
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
                Log.d(TAG, "File not found");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Log.d(TAG, "Failed reading file");
            }
            return null;
        }
    }
}