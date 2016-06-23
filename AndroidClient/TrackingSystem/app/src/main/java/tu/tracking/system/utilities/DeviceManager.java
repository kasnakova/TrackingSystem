package tu.tracking.system.utilities;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Liza on 21.6.2016 г..
 */
public class DeviceManager {

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
