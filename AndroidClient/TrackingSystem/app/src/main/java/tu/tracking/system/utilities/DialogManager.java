package tu.tracking.system.utilities;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;

import tu.tracking.system.R;

/**
 * Created by Liza on 20.6.2016 г..
 */
public class DialogManager {
    public static void makeAlert(Context context, String title, String message){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.mipmap.radar)
                .show();
    }

    public static void NoInternetOrServerAlert(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(Constants.TITLE_PROBLEM_OCCURRED)
                .setMessage(Constants.MESSAGE_PROBLEM_OCCURRED)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.mipmap.radar);
        AlertDialog alert = builder.create();
        if(!alert.isShowing()){
            alert.show();
        }
    }

    public static void identifyMe(Context contex){
        DialogManager.makeAlert(contex, "Identifier", ((TelephonyManager) contex.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
    }
}
