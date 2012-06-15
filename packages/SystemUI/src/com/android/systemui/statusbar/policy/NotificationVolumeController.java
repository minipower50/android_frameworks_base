/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.Settings;

public class NotificationVolumeController implements ToggleSlider.Listener {
    private static final int STREAM = AudioManager.STREAM_NOTIFICATION;

    private AudioManager mAudioManager;
    private Context mContext;
    private ToggleSlider mControl;

    public NotificationVolumeController(Context context, ToggleSlider control) {
        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter);

        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mContext = context;
        mControl = control;

        boolean mute = mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
        int volume = mute ? mAudioManager.getLastAudibleStreamVolume(STREAM) :
                mAudioManager.getStreamVolume(STREAM);
        control.setMax(mAudioManager.getStreamMaxVolume(STREAM));
        control.setValue(volume);
        control.setChecked(mute);
        control.setOnChangedListener(this);
    }

    public void onChanged(ToggleSlider view, boolean tracking, boolean mute, int level) {
        if (!tracking) {
            if (mute) {
                boolean vibeInSilent = (1 == Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VIBRATE_IN_SILENT, 1));
                mAudioManager.setRingerMode(
                        vibeInSilent ? AudioManager.RINGER_MODE_VIBRATE
                                     : AudioManager.RINGER_MODE_SILENT);
                mControl.setChecked(true);
            } else {
                int flags = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VOLUME_CHANGE_BEEP, 1) == 1 ?
                        AudioManager.FLAG_PLAY_SOUND : 0;
                mAudioManager.setStreamVolume(STREAM, level, flags);
                if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                mControl.setChecked(mAudioManager.getRingerMode() !=
                        AudioManager.RINGER_MODE_NORMAL);
            }
        }
    };
}
