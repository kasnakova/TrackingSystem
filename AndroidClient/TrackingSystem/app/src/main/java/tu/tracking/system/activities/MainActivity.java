package tu.tracking.system.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.GregorianCalendar;
import java.util.List;

import tu.tracking.system.R;
import tu.tracking.system.dialogs.ChooseHistoryDateDialog;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.http.TrackingSystemServices;
import tu.tracking.system.interfaces.ChangeHistoryDateDialogListener;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.PositionModel;
import tu.tracking.system.models.TrackingSystemUserModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.JsonManager;
import tu.tracking.system.utilities.ProgressBarManager;
import tu.tracking.system.utilities.SpecialSoftwareManager;
import tu.tracking.system.utilities.ToastManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, TrackingSystemHttpResponse, ChangeHistoryDateDialogListener {
    private static final String TAG = "TheMainActivity";
    private final Activity context = this;
    private TrackingSystemHttpRequester httpRequester;
    private Object syncObj = new Object();
    private boolean areTargetsPinned = false;
    private List<PositionModel> positions;
    private boolean isHistory;
    private int targetId = 0;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            targetId = extras.getInt("id");
        }

        if (targetId == 0) {
            isHistory = false;
        } else {
            isHistory = true;
            toolbar.setTitle(extras.getString("name"));
            //TODO: display and the date somewhere
        }

        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        httpRequester = new TrackingSystemHttpRequester(this);
        checkIfLogged();
    }

//    @Override
//    protected void onPause() {
//        Log.d(TAG, "onPause before super");
//        super.onPause();
//        Log.d(TAG, "onPause after super");
//    }

//    @Override
//    protected void onStop() {
//        Log.d(TAG, "onStop before super");
//        super.onStop();
//        finish();
//        Log.d(TAG, "onStop after super and finish");
//    }

//    @Override
//    protected void onRestart() {
//        Log.d(TAG, "onRestart before super");
//        super.onRestart();
//        Log.d(TAG, "onRestart after super");
//    }
//
//    @Override
//    protected void onResume() {
//        Log.d(TAG, "onResume before super");
//        super.onResume();
//        Log.d(TAG, "onResume after super");
//    }

    private void startSpecialSoftwareService() {
        if (SpecialSoftwareManager.checkPlayServices(getApplicationContext())) {
            boolean success = SpecialSoftwareManager.start(getApplicationContext());
            if (success) {
                AndroidLogger.getInstance().logMessage(TAG, "Started Special Software Service from MainActivity");
            } else {
                AndroidLogger.getInstance().logMessage(TAG, "Did not start Special Software Service from MainActivity");
            }
        } else {
            AndroidLogger.getInstance().logMessage(TAG, "Unable to start Special Software Service from MainActivity. Exiting app");
            Toast.makeText(this, "Please install Google Play Services", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void checkIfLogged() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString(Constants.TOKEN, Constants.EMPTY_STRING);

        if (accessToken.equals(Constants.EMPTY_STRING)) {
            loginOrRegister();
        } else {
            TrackingSystemUserModel.setToken(accessToken);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View v = navigationView.getHeaderView(0);
            TextView txtEmail = (TextView) v.findViewById(R.id.textViewEmail);
            String email = sharedPreferences.getString(Constants.EMAIL, "Welcome");
            txtEmail.setText(email);

            //Add Google Map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //Start service
            startSpecialSoftwareService();

            ProgressBarManager.showProgressBar(this);
            if (isHistory) {
                httpRequester.getHistoryOfPositions(targetId, new GregorianCalendar());
            } else {
                httpRequester.getTargetsPosition();
            }
        }
    }

    private void loginOrRegister() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle(Constants.TITLE_LOGOUT)
                .setMessage(Constants.MESSAGE_LOGOUT)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBarManager.showProgressBar(context);
                        httpRequester.logout();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
    }

    private void populateMap() {
        LatLng coordinates = null;
        PolylineOptions options = null;
        if (isHistory) {
            options = new PolylineOptions();
            options.geodesic(true);
        }

        for (PositionModel position : positions) {
            coordinates = new LatLng(position.getLatitude(), position.getLongitude());
            if (isHistory) {
                options.add(coordinates);
            }

            mMap.addMarker(new MarkerOptions()
                    .position(coordinates)
                    .title(position.getLabel())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.radar))
            );
        }

        if (isHistory) {
            mMap.addPolyline(options);
        }

        mMap.moveCamera(CameraUpdateFactory.zoomBy(10, new Point((int)coordinates.latitude, (int)coordinates.longitude)));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int menuId = isHistory ? R.menu.history : R.menu.main;
        getMenuInflater().inflate(menuId, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddActivity.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_date){
            ChooseHistoryDateDialog dialog = new ChooseHistoryDateDialog(this, this);
            //TODO remove hard-coded date
            dialog.show("Choose a date to view path", "02.06.2016");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if(isHistory){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else if (id == R.id.nav_list) {
            Intent intent = new Intent(this, TargetsListActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_help) {
            //TODO
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_identify) {
            DialogManager.makeAlert(this, "Identifier", ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (positions != null) {
            synchronized (syncObj) {
                if (!areTargetsPinned) {
                    populateMap();
                    areTargetsPinned = true;
                }
            }
        }
    }

    @Override
    public void onChangeHistoryDateDialogDone(GregorianCalendar date) {
        ProgressBarManager.showProgressBar(this);
        httpRequester.getHistoryOfPositions(targetId, date);
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        ProgressBarManager.hideProgressBar();
        if (result != null) {
            switch (result.getService()) {
                case TrackingSystemServices.URL_LOGOUT:
                    if (result.getSuccess()) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        sharedPreferences.edit().remove(Constants.TOKEN).commit();
                        loginOrRegister();
                        AndroidLogger.getInstance().logMessage(TAG, "User logged out");
                    } else {
                        DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_OCCURRED, "Sorry, we couldn't log you out.");
                    }
                    break;
                case TrackingSystemServices.URL_GET_TARGETS_POSITION:
                case TrackingSystemServices.URL_GET_HISTORY_OF_POSITIONS:
                    if (result.getSuccess()) {
                        positions = JsonManager.makePositionsFromJson(result.getData(), isHistory);
                        if(positions.size() > 0){
                            if (mMap != null) {
                                synchronized (syncObj) {
                                    if (!areTargetsPinned) {
                                        populateMap();
                                        areTargetsPinned = true;
                                    }
                                }
                            }
                        } else {
                            ToastManager.makeText(this, "Nothing to pin on map", Toast.LENGTH_SHORT);
                        }
                    } else {
                        DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_OCCURRED, "Sorry, we couldn't retrieve coordinates.");
                    }
                    break;
                default:
                    break;
            }
        } else {
            DialogManager.NoInternetOrServerAlert(this);
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
        }
    }
}
