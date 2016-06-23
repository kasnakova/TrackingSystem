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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;

import tu.tracking.system.R;
import tu.tracking.system.activities.MainActivity;
import tu.tracking.system.activities.StopAlarmActivity;
import tu.tracking.system.utilities.AlarmManager;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.SpecialSoftwareManager;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static int notificationId = 0;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        PushMessageType type = PushMessageType.valueOf(data.getString("type"));
        String message = data.getString("message");
        AndroidLogger.getInstance().logMessage(TAG, "From: " + from);
        AndroidLogger.getInstance().logMessage(TAG, "Type: " + type);
        AndroidLogger.getInstance().logMessage(TAG, "Message: " + message);
        String logMessage = "";
        switch (type) {
            case SetIsTargetActive:
                logMessage= "Device is set to be active: " + message;
                setIsTargetActive(Boolean.valueOf(message));
                break;
            case TargetMovingWhenShouldNot:
                logMessage= "Device should not move: " + message;
                sendNotification(message);
                break;
            case TurnAlarmOn:
                logMessage= "Alarm turned on by: " + message;
                turnAlarmOn(message);
                break;
            case ChangeLocationInterval:
                logMessage= "Change location interval to: " + message;
                changeLocationInterval(Long.parseLong(message));
                break;
            default:
                AndroidLogger.getInstance().logMessage(TAG, String.format("Unknown GCM Message. From: %s; Type: %s; Message: %s", from, type.toString(), message));
                break;
        }

        AndroidLogger.getInstance().logMessage(TAG, logMessage);
    }

    private void setIsTargetActive(boolean isActive){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit().putBoolean(Constants.IS_DEVICE_ACTIVE, isActive).apply();
        if(isActive){
            AndroidLogger.getInstance().logMessage(TAG,
                    SpecialSoftwareManager.start(getApplicationContext()) ?
                    "Success in starting Special Software Service from MyGcmListenerService" :
                    "Failed to start Special Software Service from MyGcmListenerService" );
        }
    }

    private void turnAlarmOn(String userName) {
        AlarmManager.start(getApplicationContext());
        Intent stopAlarmIntent = new Intent(this, StopAlarmActivity.class);
        stopAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stopAlarmIntent.putExtra("userName", userName);
        startActivity(stopAlarmIntent);
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

    private void changeLocationInterval(long interval){
        long fastestInterval = interval - (long) (0.2 * interval);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putLong(Constants.LOCATION_INTERVAL, interval).apply();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putLong(Constants.LOCATION_FASTEST_INTERVAL, fastestInterval).apply();
        Intent intent = new Intent(Constants.CHANGE_LOCATION_INTERVAL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
