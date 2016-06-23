package tu.tracking.system.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import tu.tracking.system.services.SpecialSoftwareService;

/**
 * Created by Liza on 19.6.2016 г..
 */
public class SpecialSoftwareManager {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "SpecialSoftwareManager";

    public static boolean start(Context context){
        boolean isServiceAlreadyStarted = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.IS_SPECIAL_SOFTWARE_SERVICE_STARTED, false);
        boolean shouldDeviceSendCoordinates = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.IS_DEVICE_ACTIVE, false);
        boolean isDeviceRegistered = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.REGISTRATION_COMPLETE, false);
        boolean playServices = checkPlayServices(context);
        if (playServices && !isServiceAlreadyStarted && (shouldDeviceSendCoordinates || !isDeviceRegistered)) {
            Intent intent = new Intent(context, SpecialSoftwareService.class);
            context.startService(intent);
            return true;
        }

        return false;
    }

    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity)context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                AndroidLogger.getInstance().logMessage(TAG, "This device is not supported");
            }
            return false;
        }

        return true;
    }
}
