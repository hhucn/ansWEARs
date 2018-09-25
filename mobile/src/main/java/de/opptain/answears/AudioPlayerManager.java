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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import de.opptain.constants.Paths;

/**
 * The AudioPlayerManager manages the replay of the recorded audio sample.
 */
public class AudioPlayerManager {

    private static final String TAG = "AudioPlayer";
    private Context mContext;
    private AudioPlayer mAudioPlayer;

    AudioPlayerManager(Context context) {
        mContext = context;
    }

    /**
     * Starts the playback of the recording
     */
    public void play() {
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.execute();
    }

    /**
     * Stops the playback of the recording
     */
    public void stop() {
        mAudioPlayer.cancel(true);
    }

    private class AudioPlayer extends AsyncTask<Void, Void, Void> {
        private int encoding = AudioFormat.ENCODING_PCM_16BIT;
        private int sampleRate = 44100;
        private int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT;
        private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, encoding);

        @Override
        protected Void doInBackground(Void... voids) {
            try (FileInputStream fileInputStream =
                         mContext.openFileInput(Paths.LOCAL_PATH_AUDIO);
                 DataInputStream dataInputStream =
                         new DataInputStream(new BufferedInputStream(fileInputStream))) {

                AudioTrack track = new AudioTrack(
                        AudioManager.STREAM_MUSIC, sampleRate,
                        channelConfiguration, encoding,
                        bufferSize, AudioTrack.MODE_STREAM
                );
                track.play();

                short[] audioBuffer = new short[bufferSize];
                while (dataInputStream.available() > 0 && !isCancelled()) {
                    int shortsRead = 0;
                    while (dataInputStream.available() > 0 && shortsRead < audioBuffer.length) {
                        audioBuffer[shortsRead] = dataInputStream.readShort();
                        shortsRead++;
                    }
                    track.write(audioBuffer, 0, audioBuffer.length);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }
}
