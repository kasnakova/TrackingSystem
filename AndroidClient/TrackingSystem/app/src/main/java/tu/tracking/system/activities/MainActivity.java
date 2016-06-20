package tu.tracking.system.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.ITrackingSystemHttpResponse;
import tu.tracking.system.models.TrackingSystemUserModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.SpecialSoftwareManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ITrackingSystemHttpResponse {
    private static final String TAG = "TheMainActivity";
    private TrackingSystemHttpRequester httpRequester;

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action Replace with your own action Replace with your own action Replace with your own action Replace with your own action", Snackbar.LENGTH_LONG)
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

        httpRequester = new TrackingSystemHttpRequester(this, this);
        //Add Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startSpecialSoftwareService();

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

    private void startSpecialSoftwareService(){
        if(SpecialSoftwareManager.checkPlayServices(getApplicationContext())){
            boolean success = SpecialSoftwareManager.start(getApplicationContext());
            if(success) {
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

    public void checkIfLogged(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString(Constants.TOKEN, Constants.EMPTY_STRING);

        if(accessToken.equals(Constants.EMPTY_STRING)){
            loginOrRegister();
        } else {
            TrackingSystemUserModel.setToken(accessToken);
            TextView txtEmail = (TextView) findViewById(R.id.textViewEmail);
            String email = sharedPreferences.getString(Constants.EMAIL, "Welcome");
            txtEmail.setText(email);
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
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        httpRequester.logout();
                        loginOrRegister();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            // Handle the camera action
        } else if (id == R.id.nav_list) {

        } else if (id == R.id.nav_help) {

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {

    }
}
