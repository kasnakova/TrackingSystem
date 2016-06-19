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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.ITrackingSystemHttpResponse;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;

public class SpecialSoftwareIntentService extends Service implements ITrackingSystemHttpResponse,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "SpecSoftIntentService";
    private static final String[] TOPICS = {"global"};

    private TrackingSystemHttpRequester httpRequester = new TrackingSystemHttpRequester(this, null);

    GoogleApiClient mGoogleApiClient;
    //private PowerManager.WakeLock wakeLock;
    // private LocationManager locationManager;

    protected void registerGCM() {
        final Service thisService = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(thisService);
                Log.i(TAG, "onHandleIntent");
                try {
                    // [START register_for_gcm]
                    // Initially this call goes out to the network to retrieve the token, subsequent calls
                    // are local.
                    // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
                    // See https://developers.google.com/cloud-messaging/android/start for details on this file.

                    boolean isDeviceRegistered = sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);
                    if (!isDeviceRegistered) {
                        // [START get_token]
                        InstanceID instanceID = InstanceID.getInstance(thisService);
                        String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        // [END get_token]
                        Log.i(TAG, "GCM Registration Token: " + token);

                        sendRegistrationToServer(token);
                    } else {
                        Log.d(TAG, "Device already registered.");
                        AndroidLogger.getInstance().logMessage(TAG, "Device already registered.");
                    }
                    // [END register_for_gcm]
                } catch (Exception e) {
                    Log.d(TAG, "Failed to complete token refresh", e);
                    AndroidLogger.getInstance().logMessage(TAG, "Failed to complete token refresh");
                    sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
                }
            }
        });

        thread.start();
        // Notify UI that registration has completed, so the progress indicator can be hidden.
//        Intent registrationComplete = new Intent(Constants.REGISTRATION_COMPLETE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        httpRequester.registerTargetIdentity(getDeviceId(), token);
    }

    private String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.IS_SPECIAL_SOFTWARE_SERVICE_STARTED, true).apply();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        registerGCM();
//        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
//        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(getApplicationContext(), "On start",
                Toast.LENGTH_LONG).show();
//        locationManager = (LocationManager) getApplicationContext()
//                .getSystemService(Context.LOCATION_SERVICE);
        return START_STICKY;
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d(TAG, "All location settings are satisfied");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        //     try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
//                            status.startResolutionForResult(
//                                    SpecialSoftwareIntentService.this,
//                                    REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
                        Log.d(TAG, "All location settings are NOT satisfied");
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.d(TAG, "All location settings are NOT satisfied. Nothing we can do");
                        break;
                }
            }
        });

        return mLocationRequest;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // wakeLock.release();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.IS_SPECIAL_SOFTWARE_SERVICE_STARTED, false).apply();
        mGoogleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (result != null) {
            if (result.getSuccess()) {
                sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
                AndroidLogger.getInstance().logMessage(TAG, "Registered target identity");
            } else {
                sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
                AndroidLogger.getInstance().logMessage(TAG, "Problem registering target identity");
            }

            AndroidLogger.getInstance().logMessage(TAG, "Http Message: " + result.getData());
        } else {
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected to Google Play");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isActive = true;//sharedPreferences.getBoolean(Constants.IS_DEVICE_ACTIVE, false);
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || !isActive) {
            AndroidLogger.getInstance().logMessage(TAG, "No GPS permissions or device is set to be inactive");
            stopSelf();
        }

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                MIN_TIME_LOCATION_MANAGER, MIN_DISTANCE_LOCATION_MANAGER, listener);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), this);
        Log.d(TAG, "Google Location Services started");
        AndroidLogger.getInstance().logMessage(TAG, "Google Location Services started");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Location Changed");

        if (location == null) {
            return;
        }

        AndroidLogger.getInstance().logMessage(TAG, "Current location: " + location.getLatitude() + ";" + location.getLongitude());
        boolean isConnectedToInternet = isConnectingToInternet(getApplicationContext());
        AndroidLogger.getInstance().logMessage(TAG, "Is connected to net: " + isConnectedToInternet);
        if (isConnectedToInternet) {
            AndroidLogger.getInstance().logMessage(TAG, "Latitude is " + location.getLatitude());
            httpRequester.sendCoordinates(getDeviceId(), location.getLatitude(), location.getLongitude());
        }
    }
}
