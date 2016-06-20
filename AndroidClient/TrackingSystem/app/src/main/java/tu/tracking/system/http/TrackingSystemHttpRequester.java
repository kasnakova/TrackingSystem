package tu.tracking.system.http;

import android.app.ProgressDialog;
import android.content.Context;

import tu.tracking.system.interfaces.IAsyncResponse;
import tu.tracking.system.interfaces.ITrackingSystemHttpResponse;
import tu.tracking.system.models.TrackingSystemUserModel;
import tu.tracking.system.utilities.AndroidLogger;

import static tu.tracking.system.http.TrackingSystemServices.*;

public class TrackingSystemHttpRequester implements IAsyncResponse {
    private final String TAG = "TrackSysHttpRequester";

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";

    private final String FORMAT_LOGIN = "grant_type=password&username=%s&password=%s";
    private final String FORMAT_REGISTER = "Email=%s&Password=%s&ConfirmPassword=%s";
    private final String FORMAT_REGISTER_TARGET_IDENTITY = "Identifier=%s&GCMKey=%s";
    private final String FORMAT_SEND_COORDINATES = "Latitude=%f&Longitude=%f&Identifier=%s";
    private final String FORMAT_DELETE_NOTE = "?id=%d";
    private final String FORMAT_GET_DATES_WITH_NOTES = "?month=%d&year=%d";
    private final String FORMAT_GET_DECRYPTED_NOTE_TEXT = "?id=%s&password=%s";

    private IAsyncResponse context = this;
    private ITrackingSystemHttpResponse delegate;
    private ProgressDialog progress;

    public TrackingSystemHttpRequester(ITrackingSystemHttpResponse delegate, Context contextForProgress) {
        this.delegate = delegate;
        if (contextForProgress != null) {
            this.progress = new ProgressDialog(contextForProgress);
            this.progress.setCancelable(true);
            this.progress.setCanceledOnTouchOutside(true);
        }
    }

    public void register(String email, String password) {
        try {
            progress.show();
            final String urlParameters = String.format(FORMAT_REGISTER, email, password, password);

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
            progress.show();
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
            progress.show();
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

    @Override
    public void processFinish(HttpResult data) {
        if (progress != null) {
            progress.dismiss();
        }

        delegate.trackingSystemProcessFinish(data);
    }
}
