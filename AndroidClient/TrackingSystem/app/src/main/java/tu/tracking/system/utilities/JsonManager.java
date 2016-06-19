package tu.tracking.system.utilities;

/**
 * Created by Liza on 18.6.2016 г..
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class JsonManager {
    private static final String TAG = "Json";

    public static JSONObject makeJson(String data){
        try {
            JSONObject obj = new JSONObject(data);
            return obj;
        } catch (JSONException e) {
            AndroidLogger.getInstance().logError(TAG, e);
            return null;
        }
    }

    public static ArrayList<GregorianCalendar> makeGregorianCalendarArrayFromData(String data){
        ArrayList<GregorianCalendar>  dates = new ArrayList<GregorianCalendar>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++){
                GregorianCalendar calendar = DateManager.getGregorianCalendarFromString(jsonArray.get(i).toString());
                dates.add(calendar);
            }
        } catch (JSONException e) {
            AndroidLogger.getInstance().logError(TAG, e);
        }

        return dates;
    }
}
