package tu.tracking.system.http;

import java.util.GregorianCalendar;

import tu.tracking.system.interfaces.AsyncResponse;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TrackingSystemUserModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.DateManager;

import static tu.tracking.system.http.TrackingSystemServices.URL_ADD_TARGET;
import static tu.tracking.system.http.TrackingSystemServices.URL_DELETE_TARGET;
import static tu.tracking.system.http.TrackingSystemServices.URL_GET_HISTORY_OF_POSITIONS;
import static tu.tracking.system.http.TrackingSystemServices.URL_GET_TARGETS;
import static tu.tracking.system.http.TrackingSystemServices.URL_GET_TARGETS_POSITION;
import static tu.tracking.system.http.TrackingSystemServices.URL_LOGIN;
import static tu.tracking.system.http.TrackingSystemServices.URL_LOGOUT;
import static tu.tracking.system.http.TrackingSystemServices.URL_REGISTER;
import static tu.tracking.system.http.TrackingSystemServices.URL_REGISTER_TARGET_IDENTITY;
import static tu.tracking.system.http.TrackingSystemServices.URL_SEND_COORDINATES;
import static tu.tracking.system.http.TrackingSystemServices.URL_SET_IS_TARGET_ACTIVE;
import static tu.tracking.system.http.TrackingSystemServices.URL_SET_SHOULD_TARGET_MOVE;
import static tu.tracking.system.http.TrackingSystemServices.URL_TURN_ALARM_ON;

public class TrackingSystemHttpRequester implements AsyncResponse {
    private final String TAG = "TrackSysHttpRequester";

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";

    private final String FORMAT_LOGIN = "grant_type=password&username=%s&password=%s";
    private final String FORMAT_REGISTER = "Email=%s&Password=%s&ConfirmPassword=%s&Identifier=%s";
    private final String FORMAT_REGISTER_TARGET_IDENTITY = "Identifier=%s&GCMKey=%s";
    private final String FORMAT_SEND_COORDINATES = "Latitude=%f&Longitude=%f&Identifier=%s";
    private final String FORMAT_ADD_TARGET = "Type=%s&Name=%s&Identifier=%s";
    private final String FORMAT_DELETE_TARGET = "?id=%d";
    private final String FORMAT_GET_HISTORY_OF_POSITIONS = "?targetId=%d&date=%s";
    private final String FORMAT_SET_IS_TARGET_ACTIVE = "?id=%d&isActive=%s";
    private final String FORMAT_SET_SHOULD_TARGET_MOVE = "?id=%d&shouldNotMove=%s&shouldNotMoveUntil=%s";
    private final String FORMAT_TURN_ALARM_ON = "?id=%d";

    private AsyncResponse context = this;
    private TrackingSystemHttpResponse delegate;

    public TrackingSystemHttpRequester(TrackingSystemHttpResponse delegate) {
        this.delegate = delegate;
    }

    public void register(String email, String password, String identifier) {
        try {
            final String urlParameters = String.format(FORMAT_REGISTER, email, password, password, identifier);

            new HttpRequester(context)
                    .execute(
                            URL_REGISTER,
                            METHOD_POST,
                            urlParameters);
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void login(String email, String password) {
        try {
            final String urlParameters = String.format(FORMAT_LOGIN, email, password);

            new HttpRequester(context)
                    .execute(
                            URL_LOGIN,
                            METHOD_POST,
                            urlParameters);
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void logout() {
        try {
            new HttpRequester(context)
                    .execute(
                            URL_LOGOUT,
                            METHOD_POST,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void registerTargetIdentity(String deviceId, String GCMKey) {
        try {
            final String urlParameters = String.format(FORMAT_REGISTER_TARGET_IDENTITY, deviceId, GCMKey);

            new HttpRequester(context)
                    .execute(
                            URL_REGISTER_TARGET_IDENTITY,
                            METHOD_POST,
                            urlParameters);
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void sendCoordinates(String deviceId, double latitude, double longitude) {
        try {
            final String urlParameters = String.format(FORMAT_SEND_COORDINATES, latitude, longitude, deviceId);
            new HttpRequester(context)
                    .execute(
                            URL_SEND_COORDINATES,
                            METHOD_POST,
                            urlParameters);
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void getTargetsPosition() {
        try {
            new HttpRequester(context)
                    .execute(
                            URL_GET_TARGETS_POSITION,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void addTarget(String type, String name, String identifier) {
        try {
            final String urlParameters = String.format(FORMAT_ADD_TARGET, type, name, identifier);
            new HttpRequester(context)
                    .execute(
                            URL_ADD_TARGET,
                            METHOD_POST,
                            urlParameters,
                            TrackingSystemUserModel.getToken());
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void getTargets() {
        try {
            new HttpRequester(context)
                    .execute(
                            URL_GET_TARGETS,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch (Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void deleteTarget(int id){
        try{
            String url = URL_DELETE_TARGET + String.format(FORMAT_DELETE_TARGET, id);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_DELETE,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch(Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void getHistoryOfPositions(int targetId, GregorianCalendar calendar){
        try{
            String date = DateManager.getDateStringFromCalendar(calendar);
            String url = URL_GET_HISTORY_OF_POSITIONS + String.format(FORMAT_GET_HISTORY_OF_POSITIONS, targetId, date);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch(Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void setIsTargetActive(int id, boolean isActive){
        try{
            String url = URL_SET_IS_TARGET_ACTIVE + String.format(FORMAT_SET_IS_TARGET_ACTIVE, id, Boolean.toString(isActive));
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch(Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void setShouldTargetMove(int id, boolean shouldNotMove, GregorianCalendar shouldNotMoveUntil){
        try{
            String date = DateManager.getDateTimeStringFromCalendar(shouldNotMoveUntil);
            String url = URL_SET_SHOULD_TARGET_MOVE + String.format(FORMAT_SET_SHOULD_TARGET_MOVE,
                                                        id, Boolean.toString(shouldNotMove), date);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch(Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    public void turnAlarmOn(int id){
        try{
            String url = URL_TURN_ALARM_ON + String.format(FORMAT_TURN_ALARM_ON, id);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            TrackingSystemUserModel.getToken());
        } catch(Exception ex) {
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }

    @Override
    public void processFinish(HttpResult data) {
        delegate.trackingSystemProcessFinish(data);
    }
}
