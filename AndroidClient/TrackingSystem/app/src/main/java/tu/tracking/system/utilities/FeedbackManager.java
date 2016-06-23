package tu.tracking.system.utilities;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tu.tracking.system.R;

/**
 * Created by Liza on 22.6.2016 г..
 */
public class FeedbackManager {
    public static void makeToast(Context context, String message){
        makeToast(context, message, Toast.LENGTH_LONG);
    }

    public static void makeToast(Context context, String message, int length){
        //TODO use this everywhere
        Toast toast = Toast.makeText(context, message, length);
        View view = toast.getView();
        view.setBackgroundResource(R.color.dark_blue);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        /*here you can do anything with text*/
        toast.show();
    }

    public static void makeSnack(View view, String mesasge){
        Snackbar snackbar = Snackbar.make(view, mesasge, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View snkView = snackbar.getView();
        snkView.setBackgroundResource(R.color.dark_blue);
        snackbar.show();
    }
}
