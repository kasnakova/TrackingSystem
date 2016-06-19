/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tu.tracking.system.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import tu.tracking.system.R;
import tu.tracking.system.activities.MainActivity;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static int notificationId = 0;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        PushMessageType type = PushMessageType.valueOf(data.getString("type"));
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Message: " + message);

        switch (type) {
            case SetIsTargetActive:
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit().putBoolean(Constants.IS_DEVICE_ACTIVE, Boolean.valueOf(message)).apply();
                break;
            case TargetMovingWhenShouldNot:
                sendNotification(message);
                break;
            case TurnAlarmOn:
                turnAlarmOn();
                break;
            default:
                AndroidLogger.getInstance().logMessage(TAG, String.format("Unknown GCM Message. From: %s; Type: %s; Message: %s", from, type.toString(), message));
                break;
        }
    }

    private void turnAlarmOn() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                0);
        MediaPlayer thePlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        thePlayer.start();
//        Intent stopAlarmIntent = new Intent(this, MyActivity.class);
//        stopAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(stopAlarmIntent);
    }

    private void sendNotification(String name) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.radar)
                .setContentTitle(name)
                .setContentText("is moving")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
        notificationId++;
    }
}
