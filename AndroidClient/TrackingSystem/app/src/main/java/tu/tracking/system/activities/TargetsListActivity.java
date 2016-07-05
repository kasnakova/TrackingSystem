/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tu.tracking.system.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.GregorianCalendar;
import java.util.List;

import tu.tracking.system.R;
import tu.tracking.system.adapters.TargetAdapter;
import tu.tracking.system.dialogs.ShouldNotMoveDialog;
import tu.tracking.system.gestures.OnTargetTouchListener;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.http.TrackingSystemServices;
import tu.tracking.system.interfaces.ShouldNotMoveDialogListener;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TargetModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.FeedbackManager;
import tu.tracking.system.utilities.JsonManager;
import tu.tracking.system.utilities.ProgressBarManager;

public class TargetsListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TrackingSystemHttpResponse, ShouldNotMoveDialogListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "TheMainActivity";

    private ListView listViewTargets;
    private TargetsListActivity context = this;
    private TargetAdapter adapter;
    private TrackingSystemHttpRequester httpRequester;
    private List<TargetModel> targets;
    private TargetModel targetToDelete;
    private TargetModel targetForAlarm;
    private TargetModel targetShouldNotMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets_list);

        //Toolbar and navigation drawer set up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Targets");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listViewTargets = (ListView) findViewById(R.id.listViewTargets);
        httpRequester = new TrackingSystemHttpRequester(this);
        httpRequester.getTargets();
        ProgressBarManager.showProgressBar(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ProgressBarManager.showProgressBar(this);
        httpRequester.getTargets();
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
        getMenuInflater().inflate(R.menu.add, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            finish();
        } else if (id == R.id.nav_list) {
            ProgressBarManager.showProgressBar(this);
            httpRequester.getTargets();
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_identify) {
            DialogManager.identifyMe(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    private void deleteTarget() {
        final int id = targetToDelete.getId();
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage(String.format("Do you want to delete '%s' from your list of targets?", targetToDelete.getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBarManager.showProgressBar(context);
                        httpRequester.deleteTarget(id);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
    }

    private void turnAlarmOn() {
        final int id = targetForAlarm.getId();
        new AlertDialog.Builder(context)
                .setTitle("Alarm")
                .setMessage(String.format("Do you want to sound an alarm on %s?", targetForAlarm.getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBarManager.showProgressBar(context);
                        httpRequester.turnAlarmOn(id);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
    }

    private void populateNoteListView() {
        adapter = new TargetAdapter(context,
                R.layout.listview_target_cell, targets);
        final ShouldNotMoveDialogListener listener = this;
        listViewTargets.setAdapter(adapter);

        listViewTargets.setOnTouchListener(new OnTargetTouchListener(this) {
            public void onSwipeRight(MotionEvent e) {
                targetToDelete = getTargetFromList(e);
                if (targetToDelete != null) {
                    deleteTarget();
                }
            }

            @Override
            public void onClick(MotionEvent e) {
                Intent intent = new Intent(context, MainActivity.class);
                TargetModel target = getTargetFromList(e);
                if (target != null) {
                    intent.putExtra("id", target.getId());
                    intent.putExtra("name", target.getName());
                    startActivity(intent);
                }
            }

            @Override
            public void onDoubleTapEv(MotionEvent e) {
                targetForAlarm = getTargetFromList(e);
                if(targetForAlarm != null){
                    turnAlarmOn();
                }
            }

            @Override
            public void onLongPressEvent(MotionEvent e) {
                targetShouldNotMove = getTargetFromList(e);
                if(targetShouldNotMove.getIsActive()) {
                    if (targetShouldNotMove != null) {
                        ShouldNotMoveDialog dialog = new ShouldNotMoveDialog(context, listener);
                        dialog.show("Tracking", targetShouldNotMove);
                    }
                } else {
                    DialogManager.makeAlert(context, "Target not active", "To specify if the target shouldn't move, please make it active first");
                }
            }

            @Override
            public void onSwipeLeft(MotionEvent e) {
                targetToDelete = getTargetFromList(e);
                if (targetToDelete != null) {
                    deleteTarget();
                }
            }

            @Override
            public void onSwipeTop() {

            }

            @Override
            public void onSwipeBottom() {

            }
        });

//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                targetForAlarm = getTargetFromList(e);
//                turnAlarmOn();
//                return true;
//            }
//        });
    }

    private TargetModel getTargetFromList(MotionEvent e) {
        ListAdapter listAdapter = listViewTargets.getAdapter();
        float x = e.getX();
        float y = e.getY();
        float rx = e.getRawX();
        float ry = e.getRawY();
        int index = listViewTargets.pointToPosition(Math.round(x), Math.round(y));
        if (index < 0 || index >= targets.size()) {
            return null;
        }

        return (TargetModel) listAdapter.getItem(index);
    }

    @Override
    public void onShouldNotMoveDialogDone(boolean shouldNotMove, GregorianCalendar shouldNotMoveUntil) {
        targetShouldNotMove.setShouldNotMove(shouldNotMove);
        targetShouldNotMove.setShouldNotMoveUntil(shouldNotMoveUntil);
        adapter.notifyDataSetChanged();
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
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        AndroidLogger.getInstance().logMessage(TAG, "User logged out");
                    } else {
                        DialogManager.makeAlert(this, Constants.TITLE_PROBLEM_OCCURRED, "Sorry, we couldn't log you out.");
                    }
                    break;
                case TrackingSystemServices.URL_GET_TARGETS:
                    if (result.getSuccess()) {
                        targets = JsonManager.makeTargetsFromJson(result.getData());
                        if (targets.size() == 0) {
                            FeedbackManager.makeToast(this, "You have no targets.", Toast.LENGTH_SHORT);
                        }

                        populateNoteListView();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, "Sorry, but we couldn't retrieve your targets");
                    }
                    break;
                case TrackingSystemServices.URL_DELETE_TARGET:
                    if (result.getSuccess()) {
                        targets.remove(targetToDelete);
                        adapter.notifyDataSetChanged();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_PROBLEM_DELETING_TARGET);
                    }
                    break;
                case TrackingSystemServices.URL_TURN_ALARM_ON:
                    if (result.getSuccess()) {
                        FeedbackManager.makeToast(this, String.format("Alarm on %s turned on", targetForAlarm.getName()), Toast.LENGTH_SHORT);
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, String.format("Sorry, we couldn't turn the alarm on %s", targetForAlarm.getName()));
                    }
                    break;
                default:
                    break;
            }
        } else {
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
            DialogManager.NoInternetOrServerAlert(context);
        }
    }
}
