package tu.tracking.system.http;

/**
 * Created by Liza on 18.6.2016 г..
 */

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tu.tracking.system.interfaces.AsyncResponse;
import tu.tracking.system.utilities.AndroidLogger;

public class HttpRequester extends AsyncTask<String, Void, HttpResult> {
    private final String USER_AGENT = "Mozilla/5.0";
    private final String TAG = "HttpRequester";
    private final int CONNECTION_TIMEOUT = 10000;
    private AsyncResponse delegate;
    private HttpURLConnection connection;

    public HttpRequester(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected HttpResult doInBackground(String... params) {
        String url = params[0];
        try {
            String requestMethod = params[1];
            URL objUrl = new URL(url);
            connection = (HttpURLConnection) objUrl.openConnection();

            connection.setRequestMethod(requestMethod);
            //connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);

            if (params.length > 3) {
                connection.setRequestProperty("Authorization", "Bearer " + params[3]);
            }

            if (requestMethod == "POST") {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setUseCaches(true);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                String urlParameters = params[2];
                if (!(urlParameters.equals(""))) {
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                }

                AndroidLogger.getInstance().logMessage(TAG, "urlParameters " + urlParameters);
            }

            AndroidLogger.getInstance().logMessage(TAG, "Sending http request to " + url);
            int responseCode = connection.getResponseCode();
            AndroidLogger.getInstance().logMessage(TAG, "Response code " + responseCode);

            boolean success = false;
            BufferedReader in = null;
            if (responseCode == 200) {
                in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                success = true;
            } else {
                in = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return new HttpResult(success, url, response.toString());
        } catch (Exception e) {
            try {
                AndroidLogger.getInstance().logMessage(TAG, "Trying again with url: " + url);
                int responseCode = connection.getResponseCode();
                AndroidLogger.getInstance().logMessage(TAG, "Response code from try block " + responseCode);
                if (responseCode == 401) {
                    return new HttpResult(false, url, e.getMessage());
                }
            } catch (Exception ex) {
                AndroidLogger.getInstance().logError(TAG, ex);
            }

            AndroidLogger.getInstance().logError(TAG, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(HttpResult data) {
        delegate.processFinish(data);
    }
}
