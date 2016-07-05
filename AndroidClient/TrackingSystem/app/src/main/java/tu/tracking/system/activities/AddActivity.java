package tu.tracking.system.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.FeedbackManager;
import tu.tracking.system.utilities.JsonManager;
import tu.tracking.system.utilities.ProgressBarManager;

import static tu.tracking.system.http.TrackingSystemServices.URL_ADD_TARGET;

public class AddActivity extends AppCompatActivity implements TrackingSystemHttpResponse {
    private final String TAG = "AddActivity";

    private Activity context = this;
    private TrackingSystemHttpRequester httpRequester;

    private EditText editTextType;
    private EditText editTextName;
    private EditText editTextIdentifier;
    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Target");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.identifyMe(context);
            }
        });

        this.httpRequester = new TrackingSystemHttpRequester(this);
        this.editTextType = (EditText) findViewById(R.id.editTextTargetType);
        this.editTextName = (EditText) findViewById(R.id.editTextTargetName);
        this.editTextIdentifier = (EditText) findViewById(R.id.editTextIdentifier);
        this.buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();
            }
        });
        editTextIdentifier.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        buttonAdd.callOnClick();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            onHelpMenuItemClicked();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void add(){
        editTextType.setError(null);
        editTextName.setError(null);
        editTextIdentifier.setError(null);
        String type = this.editTextType.getText().toString();
        String name = this.editTextName.getText().toString();
        String identifier = this.editTextIdentifier.getText().toString();

        if(type == null || type.equals(Constants.EMPTY_STRING)){
            editTextType.setError("Type can't be empty!");
            return;
        }

        if(name == null || name.equals(Constants.EMPTY_STRING)){
            editTextName.setError("Name can't be empty!");
            return;
        }

        if(identifier == null || identifier.equals(Constants.EMPTY_STRING)){
            editTextIdentifier.setError("Identifier can't be empty!");
            return;
        }

        if(type.length() < Constants.MIN_ADD_TARGET_FIELDS_LENGTH || type.length() > Constants.MAX_ADD_TARGET_FIELDS_LENGTH){
            editTextName.setError(String.format("Type must be between %d and %d symbols",
                    Constants.MIN_ADD_TARGET_FIELDS_LENGTH,
                    Constants.MAX_ADD_TARGET_FIELDS_LENGTH));
            return;
        }

        if(name.length() < Constants.MIN_ADD_TARGET_FIELDS_LENGTH || name.length() > Constants.MAX_ADD_TARGET_FIELDS_LENGTH){
            editTextName.setError(String.format("Name must be between %d and %d symbols",
                    Constants.MIN_ADD_TARGET_FIELDS_LENGTH,
                    Constants.MAX_ADD_TARGET_FIELDS_LENGTH));
            return;
        }

        if(identifier.length() < Constants.MIN_IDENTIFIER_LENGTH || identifier.length() > Constants.MAX_IDENTIFIER_LENGTH){
            editTextName.setError(String.format("Identifier must be between %d and %d symbols",
                    Constants.MIN_IDENTIFIER_LENGTH,
                    Constants.MAX_IDENTIFIER_LENGTH));
            return;
        }

        ProgressBarManager.showProgressBar(this);
        httpRequester.addTarget(type, name, identifier);
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
                    case URL_ADD_TARGET:
                        if (result.getSuccess()) {
                            RelativeLayout addView = (RelativeLayout) findViewById(R.id.loginView);
                            FeedbackManager.makeToast(this, "Successfully added target");
                            finish();
                        } else {
                            DialogManager.makeAlert(this, "Problem adding target", obj.getString(Constants.JSON_MESSAGE));
                            AndroidLogger.getInstance().logMessage(TAG, "Problem adding target: " + obj.getString(Constants.JSON_MESSAGE));
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

