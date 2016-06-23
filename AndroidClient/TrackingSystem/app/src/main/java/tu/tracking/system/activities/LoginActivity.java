package tu.tracking.system.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TrackingSystemUserModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DeviceManager;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.FeedbackManager;
import tu.tracking.system.utilities.JsonManager;
import tu.tracking.system.utilities.ProgressBarManager;

import static tu.tracking.system.http.TrackingSystemServices.URL_LOGIN;
import static tu.tracking.system.http.TrackingSystemServices.URL_REGISTER;

public class LoginActivity extends AppCompatActivity implements TrackingSystemHttpResponse {
    private final String TAG = "LoginActivity";

    private Activity context = this;
    private TrackingSystemHttpRequester trackingSystemHttpRequester;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.identifyMe(context);
            }
        });
        final LinearLayout linearLayoutLoginButton = (LinearLayout)  findViewById(R.id.linearLayoutLoginButton);
        linearLayoutLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        this.trackingSystemHttpRequester = new TrackingSystemHttpRequester(this);
        this.editTextEmail = (EditText) findViewById(R.id.editTextTargetType);
        this.editTextPassword = (EditText) findViewById(R.id.editTextTargetName);
        this.editTextConfirmPassword = (EditText) findViewById(R.id.editTextIdentifier);
        editTextConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        linearLayoutLoginButton.callOnClick();
                        return true;
                    default:
                        break;
                }

                return  false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);
        menu.findItem(R.id.action_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("MenuKor", "setOnMenuItemClickListener");
                onHelpMenuItemClicked();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            Log.d("MenuKor", "onOptionsItemSelected");
            onHelpMenuItemClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void login(){
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);
        String email = this.editTextEmail.getText().toString();
        String password = this.editTextPassword.getText().toString();
        String confirmPassword = this.editTextConfirmPassword.getText().toString();

        if(email == null || email.equals(Constants.EMPTY_STRING)){
            editTextEmail.setError(Constants.MESSAGE_EMAIL_CANNOT_BE_EMPTY);
            return;
        }

        if(password == null || password.equals(Constants.EMPTY_STRING)){
            editTextPassword.setError(Constants.MESSAGE_PASSWORD_CANNOT_BE_EMPTY);
            return;
        }

        Pattern regex = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex .matcher(email);
        if(!matcher.find()){
            editTextEmail.setError(Constants.MESSAGE_INVALID_EMAIL);
            return;
        }

        if(password.length() < Constants.MIN_PASSWORD_LENGTH){
            editTextPassword.setError(String.format(Constants.MESSAGE_PASSWORD_LENGTH, Constants.MIN_PASSWORD_LENGTH));
            return;
        }

        if(confirmPassword != null && !confirmPassword.equals(Constants.EMPTY_STRING)){
            if(password.equals(confirmPassword)){
                ProgressBarManager.showProgressBar(this);
                trackingSystemHttpRequester.register(email, password, DeviceManager.getDeviceId(context));
            } else {
                editTextConfirmPassword.setError(Constants.MESSAGE_PASSWORD_CONFIRM_NOT_MATCH);
            }
        } else {
            ProgressBarManager.showProgressBar(this);
            trackingSystemHttpRequester.login(email, password);
        }
    }

    private void onHelpMenuItemClicked(){
        Intent intentHelp = new Intent(this, HelpActivity.class);
        startActivity(intentHelp);
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        ProgressBarManager.hideProgressBar();
        try {
            if(result != null) {
                JSONObject obj = JsonManager.makeJson(result.getData());
                switch (result.getService()) {
                    case URL_LOGIN:
                        if (result.getSuccess()) {
                            String accessToken = obj.getString(Constants.JSON_ACCESS_TOKEN);
                            String email = obj.getString(Constants.JSON_EMAIL);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putString(Constants.TOKEN, accessToken).commit();
                            sharedPreferences.edit().putString(Constants.EMAIL, email).commit();
                            TrackingSystemUserModel.setToken(accessToken);
                            AndroidLogger.getInstance().logMessage(TAG, "Access token obtained");
                            Intent resultIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(resultIntent);
                            finish();
                        } else {
                            DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_WITH_LOGIN, obj.getString(Constants.JSON_ERROR_DESCRIPTION));
                            AndroidLogger.getInstance().logMessage(TAG, "Problem logging in: " + obj.getString(Constants.JSON_ERROR_DESCRIPTION));
                        }
                        break;
                    case URL_REGISTER:
                        if (result.getSuccess()) {
                            RelativeLayout loginView = (RelativeLayout) findViewById(R.id.loginView);
                            FeedbackManager.makeSnack(loginView, "Successful registration. You may help now.");
                            editTextConfirmPassword.setText(null);
                        } else {
                            DialogManager.makeAlert(this, "Problem registering", obj.getString(Constants.JSON_MESSAGE));
                            AndroidLogger.getInstance().logMessage(TAG, "Problem with registering.");
                        }
                        break;
                    default:
                        break;
                }
            } else {
                DialogManager.NoInternetOrServerAlert(this);
                AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
            }
        } catch (JSONException ex){
            AndroidLogger.getInstance().logError(TAG, ex);
        }
    }
}
