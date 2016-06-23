package tu.tracking.system.utilities;

/**
 * Created by Liza on 18.6.2016 г..
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import tu.tracking.system.models.PositionModel;
import tu.tracking.system.models.TargetModel;

public class JsonManager {
    private static final String TAG = "Json";

    public static JSONObject makeJson(String data) {
        try {
            JSONObject obj = new JSONObject(data);
            return obj;
        } catch (JSONException e) {
            AndroidLogger.getInstance().logError(TAG, e);
            return null;
        }
    }

    public static ArrayList<PositionModel> makePositionsFromJson(String data, boolean isHistory) {
        ArrayList<PositionModel> positions = new ArrayList<PositionModel>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject positionObj = JsonManager.makeJson(jsonArray.get(i).toString());
                double latitude = positionObj.getDouble("Latitude");
                double longitude = positionObj.getDouble("Longitude");
                String label = positionObj.getString(isHistory ? "Time" : "TargetName");
                PositionModel position = new PositionModel(latitude, longitude, label);
                positions.add(position);
            }
        } catch (JSONException e) {
            AndroidLogger.getInstance().logError(TAG, e);
        }

        return positions;
    }

    public static ArrayList<TargetModel> makeTargetsFromJson(String data) {
        ArrayList<TargetModel> targets = new ArrayList<TargetModel>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject targetObj = JsonManager.makeJson(jsonArray.get(i).toString());
                int id = targetObj.getInt("Id");
                String type = targetObj.getString("Type");
                String name = targetObj.getString("Name");
                boolean isActive = targetObj.getBoolean("Active");
                boolean shouldNotMove = targetObj.getBoolean("ShouldNotMove");
                GregorianCalendar shouldNotMoveUntil = DateManager.getGregorianCalendarFromString(targetObj.getString("ShouldNotMoveUntil"));
                TargetModel position = new TargetModel(id, type, name, isActive, shouldNotMove, shouldNotMoveUntil);
                targets.add(position);
            }
        } catch (JSONException e) {
            AndroidLogger.getInstance().logError(TAG, e);
        }

        return targets;
    }
}
