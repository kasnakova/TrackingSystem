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
import android.util.Log;

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
import tu.tracking.system.utilities.DeviceManager;

import static tu.tracking.system.http.TrackingSystemServices.URL_REGISTER_TARGET_IDENTITY;

public class SpecialSoftwareIntentService extends Service implements ITrackingSystemHttpResponse,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "SpecSoftIntentService";
    private static final String[] TOPICS = {"global"};

    private TrackingSystemHttpRequester httpRequester = new TrackingSystemHttpRequester(this);

    GoogleApiClient mGoogleApiClient;
    //private PowerManager.WakeLock wakeLock;
    // private LocationManager locationManager;

    protected void registerGCM() {
        final Service thisService = this;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // [START register_for_gcm]
                    // Initially this call goes out to the network to retrieve the token, subsequent calls
                    // are local.
                    // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
                    // See https://developers.google.com/cloud-messaging/android/start for details on this file.

                    // [START get_token]
                    InstanceID instanceID = InstanceID.getInstance(thisService);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]
                    Log.i(TAG, "GCM Registration Token: " + token);
                    AndroidLogger.getInstance().logMessage(TAG, "GCM Registration Token: " + token);
                    sendRegistrationToServer(token);

                    // [END register_for_gcm]
                } catch (Exception e) {
                    Log.d(TAG, "Failed to complete token refresh", e);
                    AndroidLogger.getInstance().logMessage(TAG, "Failed to complete token refresh");
                    sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
                }
            }
        });

        thread.start();
    }

    private void sendRegistrationToServer(String token) {
        httpRequester.registerTargetIdentity(DeviceManager.getDeviceId(getApplicationContext()), token);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.IS_SPECIAL_SOFTWARE_SERVICE_STARTED, true).apply();

        boolean isDeviceRegistered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);
        if (!isDeviceRegistered) {
            registerGCM();
        } else {
            Log.d(TAG, "Device already registered.");
            AndroidLogger.getInstance().logMessage(TAG, "Device already registered.");
        }

        boolean shoulSendCoord = shouldDeviceSendCoordinates();
        Log.d(TAG, "shoulSendCoord: " + shoulSendCoord);
        if (shoulSendCoord) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "Fuck this shit");
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        AndroidLogger.getInstance().logMessage(TAG, "Special Software Service started.");
        return START_STICKY;
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL_LOCATION_REQUEST);
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
                        AndroidLogger.getInstance().logMessage(TAG, "All location settings are satisfied");
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
                        AndroidLogger.getInstance().logMessage(TAG, "All location settings are NOT satisfied");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        AndroidLogger.getInstance().logMessage(TAG, "All location settings are NOT satisfied. Nothing we can do");
                        break;
                }
            }
        });

        return mLocationRequest;
    }

    private boolean shouldDeviceSendCoordinates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(Constants.IS_DEVICE_ACTIVE, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FUCK YOU", "Before PreferenceManager");
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.IS_SPECIAL_SOFTWARE_SERVICE_STARTED, false).apply();
        Log.d("FUCK YOU", "After PreferenceManager");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        Log.d("FUCK YOU", "After mGoogleApiClient");
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
            switch (result.getService()) {
                case URL_REGISTER_TARGET_IDENTITY:
                    if (result.getSuccess()) {
                        sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
                        AndroidLogger.getInstance().logMessage(TAG, "Registered target identity");
                    } else {
                        sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
                        AndroidLogger.getInstance().logMessage(TAG, "Problem registering target identity");
                    }
                    break;
            }

            AndroidLogger.getInstance().logMessage(TAG, "Http Message: " + result.getData());
        } else {
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            AndroidLogger.getInstance().logMessage(TAG, "No GPS permissions or device is set to be inactive");
            stopSelf();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), this);
        Log.d(TAG, "Google Location Services started");
        AndroidLogger.getInstance().logMessage(TAG, "Google Location Services started");
    }

    @Override
    public void onConnectionSuspended(int i) {
        AndroidLogger.getInstance().logMessage(TAG, "Connection with Google Location was suspended. i = " + i);
        ;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AndroidLogger.getInstance().logMessage(TAG, "Connection with Google Location failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Location Changed");

        if (!shouldDeviceSendCoordinates()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            stopSelf();
            return;
        }

        if (location == null) {
            return;
        }

        AndroidLogger.getInstance().logMessage(TAG, "Current location: " + location.getLatitude() + ";" + location.getLongitude());
        boolean isConnectedToInternet = isConnectingToInternet(getApplicationContext());
        AndroidLogger.getInstance().logMessage(TAG, "Is connected to net: " + isConnectedToInternet);
        if (isConnectedToInternet) {
            httpRequester.sendCoordinates(DeviceManager.getDeviceId(getApplicationContext()), location.getLatitude(), location.getLongitude());
        }
    }
}
